package com.bookpulse;

import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.dto.BorrowingTrendDto;
import com.bookpulse.dto.CategoryStatDto;
import com.bookpulse.dto.DashboardStatsDto;
import com.bookpulse.dto.MostBorrowedBookDto;
import com.bookpulse.entity.*;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.BorrowerRepository;
import com.bookpulse.service.AnalyticsService;
import com.bookpulse.service.BookService;
import com.bookpulse.service.BorrowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowService borrowService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Book book1;
    private Book book2;
    private Category category;
    private Borrower borrower;
    private BorrowTransaction tx1;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Software Architecture");

        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Clean Architecture");
        book1.setCategory(category);
        book1.setTotalCopies(5);
        book1.setAvailableCopies(3);

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Designing Data-Intensive Applications");
        book2.setCategory(category);
        book2.setTotalCopies(4);
        book2.setAvailableCopies(1);

        borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Likitha Nambari");
        borrower.setEmail("likitha@example.com");

        tx1 = new BorrowTransaction();
        tx1.setId(101L);
        tx1.setBook(book1);
        tx1.setBorrower(borrower);
        tx1.setBorrowedAt(LocalDateTime.now().minusDays(5));
        tx1.setDueDate(LocalDateTime.now().plusDays(9));
        tx1.setStatus(TransactionStatus.BORROWED);
    }

    @Test
    @DisplayName("Should accurately aggregate Dashboard KPIs")
    void testGetDashboardStats() {
        when(bookRepository.countTotalTitles()).thenReturn(2L);
        when(bookRepository.countTotalCopies()).thenReturn(9L);
        when(bookRepository.countAvailableCopies()).thenReturn(4L);
        when(borrowTransactionRepository.findOverdueTransactions(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(borrowTransactionRepository.countByStatus(TransactionStatus.OVERDUE)).thenReturn(0L);
        when(borrowerRepository.count()).thenReturn(10L);
        when(borrowTransactionRepository.findMostBorrowedBooksRaw()).thenReturn(Collections.emptyList());
        when(borrowTransactionRepository.findCategoryBorrowStatsRaw()).thenReturn(Collections.emptyList());
        when(borrowService.getAllTransactions("ALL")).thenReturn(Collections.emptyList());

        DashboardStatsDto stats = analyticsService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(2L, stats.getTotalBooks());
        assertEquals(9L, stats.getTotalCopies());
        assertEquals(4L, stats.getAvailableCopies());
        assertEquals(5L, stats.getBorrowedBooks()); // 9 - 4
        assertEquals(0L, stats.getOverdueBooks());
        assertEquals(10L, stats.getTotalBorrowers());
    }

    @Test
    @DisplayName("Should return most borrowed books leaderboard sorted by count")
    void testGetMostBorrowedBooks() {
        Object[] row1 = new Object[]{1L, "Clean Architecture", "978-0134494164", "http://example.com/cover.jpg", 15L};
        Object[] row2 = new Object[]{2L, "Designing Data-Intensive Applications", "978-1449373320", "http://example.com/cover2.jpg", 8L};

        when(borrowTransactionRepository.findMostBorrowedBooksRaw()).thenReturn(Arrays.asList(row1, row2));

        List<MostBorrowedBookDto> mostBorrowed = analyticsService.getMostBorrowedBooks(5);

        assertNotNull(mostBorrowed);
        assertEquals(2, mostBorrowed.size());
        assertEquals("Clean Architecture", mostBorrowed.get(0).getTitle());
        assertEquals(15L, mostBorrowed.get(0).getBorrowCount());
    }

    @Test
    @DisplayName("Should compute category percentage distribution correctly")
    void testGetCategoryBorrowStats() {
        Object[] row1 = new Object[]{1L, "Software Architecture", 75L};
        Object[] row2 = new Object[]{2L, "Design Patterns", 25L};

        when(borrowTransactionRepository.findCategoryBorrowStatsRaw()).thenReturn(Arrays.asList(row1, row2));

        List<CategoryStatDto> stats = analyticsService.getCategoryBorrowStats();

        assertNotNull(stats);
        assertEquals(2, stats.size());
        assertEquals(75.0, stats.get(0).getPercentage());
        assertEquals(25.0, stats.get(1).getPercentage());
    }

    @Test
    @DisplayName("Should generate monthly borrowing trend buckets")
    void testGetBorrowingTrends() {
        when(borrowTransactionRepository.findAll()).thenReturn(Arrays.asList(tx1));

        List<BorrowingTrendDto> trends = analyticsService.getBorrowingTrends();

        assertNotNull(trends);
        assertEquals(6, trends.size());
        long totalRecorded = trends.stream().mapToLong(BorrowingTrendDto::getCount).sum();
        assertEquals(1L, totalRecorded);
    }

    @Test
    @DisplayName("Should identify low availability books")
    void testGetLowAvailabilityBooks() {
        when(bookRepository.findLowAvailabilityBooks()).thenReturn(Arrays.asList(book2));
        BookResponseDto dto = new BookResponseDto();
        dto.setId(2L);
        dto.setTitle("Designing Data-Intensive Applications");
        dto.setAvailableCopies(1);
        when(bookService.mapToResponseDto(book2)).thenReturn(dto);

        List<BookResponseDto> lowStock = analyticsService.getLowAvailabilityBooks();

        assertNotNull(lowStock);
        assertEquals(1, lowStock.size());
        assertEquals("Designing Data-Intensive Applications", lowStock.get(0).getTitle());
    }
}
