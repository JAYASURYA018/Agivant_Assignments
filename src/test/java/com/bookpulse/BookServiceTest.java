package com.bookpulse;

import com.bookpulse.dto.BookRequestDto;
import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.entity.Author;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.Category;
import com.bookpulse.exception.DuplicateResourceException;
import com.bookpulse.exception.InvalidOperationException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.AuthorRepository;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.CategoryRepository;
import com.bookpulse.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @InjectMocks
    private BookService bookService;

    private Category category;
    private Author author;
    private Book book;

    @BeforeEach
    void setUp() {
        category = new Category(1L, "Technology", "Tech books");
        author = new Author(1L, "Robert C. Martin", "Uncle Bob");
        book = new Book("Clean Code", "9780132350884", "Software craft", 2008, 5, 5, category);
        book.setId(1L);
        book.setAuthors(new HashSet<>(Collections.singletonList(author)));
    }

    @Test
    @DisplayName("Should create book successfully with valid inputs")
    void shouldCreateBookSuccessfully() {
        BookRequestDto request = new BookRequestDto();
        request.setTitle("Clean Code");
        request.setIsbn("9780132350884");
        request.setPublicationYear(2008);
        request.setTotalCopies(5);
        request.setAvailableCopies(5);
        request.setCategoryId(1L);
        request.setAuthorIds(List.of(1L));

        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponseDto response = bookService.createBook(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getIsbn()).isEqualTo("9780132350884");
        assertThat(response.getTotalCopies()).isEqualTo(5);
        assertThat(response.getAvailableCopies()).isEqualTo(5);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should reject book creation when ISBN already exists")
    void shouldRejectDuplicateIsbn() {
        BookRequestDto request = new BookRequestDto();
        request.setTitle("Duplicate Book");
        request.setIsbn("9780132350884");
        request.setTotalCopies(3);
        request.setCategoryId(1L);
        request.setAuthorIds(List.of(1L));

        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should reject book creation when available copies exceeds total copies")
    void shouldRejectAvailableCopiesExceedingTotal() {
        BookRequestDto request = new BookRequestDto();
        request.setTitle("Invalid Copies Book");
        request.setIsbn("9781234567890");
        request.setTotalCopies(3);
        request.setAvailableCopies(5); // Invalid: 5 > 3
        request.setCategoryId(1L);
        request.setAuthorIds(List.of(1L));

        when(bookRepository.existsByIsbn("9781234567890")).thenReturn(false);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cannot exceed total copies");
    }

    @Test
    @DisplayName("Should find book by ID successfully")
    void shouldFindBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponseDto response = bookService.getBookById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for invalid book ID")
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book with ID 999 was not found");
    }

    @Test
    @DisplayName("Should search books matching keyword query")
    void shouldSearchBooks() {
        when(bookRepository.searchBooks(eq("Clean"), eq("Clean"), isNull(), isNull())).thenReturn(List.of(book));

        List<BookResponseDto> results = bookService.searchBooks("Clean", null, null, "title");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }
}
