package com.bookpulse;

import com.bookpulse.dto.BorrowerDto;
import com.bookpulse.dto.BorrowerHealthDto;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.BorrowTransaction;
import com.bookpulse.entity.Borrower;
import com.bookpulse.entity.TransactionStatus;
import com.bookpulse.exception.DuplicateResourceException;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.BorrowerRepository;
import com.bookpulse.service.BorrowerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    private Borrower borrower;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        borrower = new Borrower(1L, "Likitha Nambari", "likitha@example.com", "+91 9876543210");
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Clean Code");
    }

    @Test
    @DisplayName("Should create borrower successfully")
    void shouldCreateBorrowerSuccessfully() {
        BorrowerDto dto = new BorrowerDto(null, "Likitha Nambari", "likitha@example.com", "+91 9876543210");

        when(borrowerRepository.existsByEmailIgnoreCase("likitha@example.com")).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);
        when(borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(1L)).thenReturn(List.of());

        BorrowerDto created = borrowerService.createBorrower(dto);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Likitha Nambari");
        assertThat(created.getEmail()).isEqualTo("likitha@example.com");
    }

    @Test
    @DisplayName("Should reject duplicate borrower email")
    void shouldRejectDuplicateEmail() {
        BorrowerDto dto = new BorrowerDto(null, "Duplicate User", "likitha@example.com", "1234567890");

        when(borrowerRepository.existsByEmailIgnoreCase("likitha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> borrowerService.createBorrower(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should compute GOOD borrowing health when 0 overdue books")
    void shouldCalculateGoodBorrowingHealth() {
        BorrowTransaction returnedTrans = new BorrowTransaction(sampleBook, borrower, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(6));
        returnedTrans.setStatus(TransactionStatus.RETURNED);

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(1L)).thenReturn(List.of(returnedTrans));

        BorrowerDto result = borrowerService.getBorrowerById(1L);

        assertThat(result.getHealth()).isNotNull();
        assertThat(result.getHealth().getStatus()).isEqualTo("GOOD");
        assertThat(result.getHealth().getOverdueCount()).isEqualTo(0);
        assertThat(result.getHealth().getHealthPercentage()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should compute WARNING borrowing health when 1 overdue book")
    void shouldCalculateWarningBorrowingHealth() {
        BorrowTransaction overdueTrans = new BorrowTransaction(sampleBook, borrower, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(6));
        overdueTrans.setStatus(TransactionStatus.OVERDUE);

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(1L)).thenReturn(List.of(overdueTrans));

        BorrowerDto result = borrowerService.getBorrowerById(1L);

        assertThat(result.getHealth()).isNotNull();
        assertThat(result.getHealth().getStatus()).isEqualTo("WARNING");
        assertThat(result.getHealth().getOverdueCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should compute RISK borrowing health when multiple overdue books")
    void shouldCalculateRiskBorrowingHealth() {
        BorrowTransaction overdue1 = new BorrowTransaction(sampleBook, borrower, LocalDateTime.now().minusDays(25), LocalDateTime.now().minusDays(11));
        overdue1.setStatus(TransactionStatus.OVERDUE);

        BorrowTransaction overdue2 = new BorrowTransaction(sampleBook, borrower, LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(16));
        overdue2.setStatus(TransactionStatus.OVERDUE);

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(1L)).thenReturn(List.of(overdue1, overdue2));

        BorrowerDto result = borrowerService.getBorrowerById(1L);

        assertThat(result.getHealth()).isNotNull();
        assertThat(result.getHealth().getStatus()).isEqualTo("RISK");
        assertThat(result.getHealth().getOverdueCount()).isEqualTo(2);
    }
}
