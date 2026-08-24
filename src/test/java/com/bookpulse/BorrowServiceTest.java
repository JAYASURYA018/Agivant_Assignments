package com.bookpulse;

import com.bookpulse.dto.BorrowRequestDto;
import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.ReturnRequestDto;
import com.bookpulse.entity.*;
import com.bookpulse.exception.InsufficientCopiesException;
import com.bookpulse.exception.InvalidOperationException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.BorrowerRepository;
import com.bookpulse.service.BorrowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @InjectMocks
    private BorrowService borrowService;

    private Book book;
    private Borrower borrower;
    private BorrowTransaction activeTransaction;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Technology", "Tech books");
        book = new Book("Effective Java", "9780134685991", "Java best practices", 2018, 5, 2, category);
        book.setId(1L);

        borrower = new Borrower(1L, "Likitha Nambari", "likitha@example.com", "+91 9876543210");

        activeTransaction = new BorrowTransaction(book, borrower, LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(11));
        activeTransaction.setId(10L);
        activeTransaction.setStatus(TransactionStatus.BORROWED);
    }

    @Test
    @DisplayName("Should successfully borrow book when availableCopies > 0")
    void shouldBorrowAvailableBook() {
        BorrowRequestDto request = new BorrowRequestDto(1L, 14);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findActiveTransaction(1L, 1L)).thenReturn(Optional.empty());
        when(borrowTransactionRepository.save(any(BorrowTransaction.class))).thenAnswer(invocation -> {
            BorrowTransaction t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        int initialAvailable = book.getAvailableCopies();
        BorrowTransactionResponseDto response = borrowService.borrowBook(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isEqualTo(1L);
        assertThat(response.getBorrowerId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("BORROWED");
        assertThat(book.getAvailableCopies()).isEqualTo(initialAvailable - 1);
        verify(bookRepository, times(1)).save(book);
        verify(borrowTransactionRepository, times(1)).save(any(BorrowTransaction.class));
    }

    @Test
    @DisplayName("Should reject borrowing when availableCopies is zero")
    void shouldRejectBorrowingUnavailableBook() {
        book.setAvailableCopies(0); // Zero stock
        BorrowRequestDto request = new BorrowRequestDto(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findActiveTransaction(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(1L, request))
                .isInstanceOf(InsufficientCopiesException.class)
                .hasMessageContaining("unavailable (0 available copies)");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should reject borrowing when borrower already holds an active loan of the book")
    void shouldRejectDuplicateActiveLoan() {
        BorrowRequestDto request = new BorrowRequestDto(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findActiveTransaction(1L, 1L)).thenReturn(Optional.of(activeTransaction));

        assertThatThrownBy(() -> borrowService.borrowBook(1L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already has an active copy");
    }

    @Test
    @DisplayName("Should successfully return borrowed book and increment available copies")
    void shouldReturnBorrowedBook() {
        book.setAvailableCopies(1); // 1 available out of 5
        ReturnRequestDto request = new ReturnRequestDto(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findActiveTransaction(1L, 1L)).thenReturn(Optional.of(activeTransaction));
        when(borrowTransactionRepository.save(any(BorrowTransaction.class))).thenReturn(activeTransaction);

        BorrowTransactionResponseDto response = borrowService.returnBook(1L, request);

        assertThat(response).isNotNull();
        assertThat(book.getAvailableCopies()).isEqualTo(2);
        assertThat(activeTransaction.getStatus()).isEqualTo(TransactionStatus.RETURNED);
        assertThat(activeTransaction.getReturnedAt()).isNotNull();
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    @DisplayName("Should reject returning when no active transaction exists for borrower")
    void shouldRejectReturnWithoutActiveTransaction() {
        ReturnRequestDto request = new ReturnRequestDto(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findActiveTransaction(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.returnBook(1L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("No active borrow record found");
    }

    @Test
    @DisplayName("Should reject returning if availableCopies would exceed totalCopies")
    void shouldRejectReturnExceedingTotalCopies() {
        book.setAvailableCopies(5); // already max (5/5)
        ReturnRequestDto request = new ReturnRequestDto(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findActiveTransaction(1L, 1L)).thenReturn(Optional.of(activeTransaction));

        assertThatThrownBy(() -> borrowService.returnBook(1L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cannot exceed total inventory copies");
    }
}
