package com.bookpulse.service;

import com.bookpulse.dto.*;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.BorrowTransaction;
import com.bookpulse.entity.TransactionStatus;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.BorrowerRepository;
import com.bookpulse.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowService borrowService;
    private final BookService bookService;

    public AnalyticsService(
            BookRepository bookRepository,
            BorrowerRepository borrowerRepository,
            BorrowTransactionRepository borrowTransactionRepository,
            CategoryRepository categoryRepository,
            BorrowService borrowService,
            BookService bookService) {
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
        this.borrowTransactionRepository = borrowTransactionRepository;
        this.categoryRepository = categoryRepository;
        this.borrowService = borrowService;
        this.bookService = bookService;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        long totalBooks = bookRepository.countTotalTitles();
        long totalCopies = bookRepository.countTotalCopies();
        long availableCopies = bookRepository.countAvailableCopies();
        long borrowedCopies = Math.max(0, totalCopies - availableCopies);
        
        LocalDateTime now = LocalDateTime.now();
        long overdueCount = borrowTransactionRepository.findOverdueTransactions(now).size() 
                + borrowTransactionRepository.countByStatus(TransactionStatus.OVERDUE);
        long totalBorrowers = borrowerRepository.count();

        stats.setTotalBooks(totalBooks);
        stats.setTotalCopies(totalCopies);
        stats.setAvailableCopies(availableCopies);
        stats.setBorrowedBooks(borrowedCopies);
        stats.setOverdueBooks(overdueCount);
        stats.setTotalBorrowers(totalBorrowers);

        // Most borrowed book
        List<MostBorrowedBookDto> mostBorrowed = getMostBorrowedBooks(1);
        if (!mostBorrowed.isEmpty()) {
            stats.setMostBorrowedBookTitle(mostBorrowed.get(0).getTitle());
            stats.setMostBorrowedBookCount(mostBorrowed.get(0).getBorrowCount());
        } else {
            stats.setMostBorrowedBookTitle("N/A");
            stats.setMostBorrowedBookCount(0);
        }

        // Most popular category
        List<CategoryStatDto> catStats = getCategoryBorrowStats();
        if (!catStats.isEmpty()) {
            stats.setMostPopularCategoryName(catStats.get(0).getCategoryName());
        } else {
            stats.setMostPopularCategoryName("N/A");
        }

        // Recent 5 transactions
        List<BorrowTransactionResponseDto> recent = borrowService.getAllTransactions("ALL").stream()
                .limit(6)
                .collect(Collectors.toList());
        stats.setRecentTransactions(recent);

        return stats;
    }

    public List<MostBorrowedBookDto> getMostBorrowedBooks(int limit) {
        List<Object[]> rawList = borrowTransactionRepository.findMostBorrowedBooksRaw();
        return rawList.stream()
                .limit(limit > 0 ? limit : 10)
                .map(row -> new MostBorrowedBookDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<CategoryStatDto> getCategoryBorrowStats() {
        List<Object[]> rawList = borrowTransactionRepository.findCategoryBorrowStatsRaw();
        long totalBorrowSum = rawList.stream()
                .mapToLong(row -> ((Number) row[2]).longValue())
                .sum();

        return rawList.stream()
                .map(row -> {
                    long count = ((Number) row[2]).longValue();
                    double percentage = totalBorrowSum > 0 ? Math.round((count * 100.0 / totalBorrowSum) * 10.0) / 10.0 : 0.0;
                    return new CategoryStatDto(
                            ((Number) row[0]).longValue(),
                            (String) row[1],
                            count,
                            percentage
                    );
                })
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> getLowAvailabilityBooks() {
        return bookRepository.findLowAvailabilityBooks().stream()
                .map(bookService::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<BorrowingTrendDto> getBorrowingTrends() {
        List<BorrowTransaction> transactions = borrowTransactionRepository.findAll();
        Map<String, Long> monthlyCounts = new LinkedHashMap<>();

        // Initialize last 6 months
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            monthlyCounts.put(month.format(monthFormatter), 0L);
        }

        for (BorrowTransaction t : transactions) {
            if (t.getBorrowedAt() != null) {
                String monthKey = t.getBorrowedAt().format(monthFormatter);
                if (monthlyCounts.containsKey(monthKey)) {
                    monthlyCounts.put(monthKey, monthlyCounts.get(monthKey) + 1);
                }
            }
        }

        return monthlyCounts.entrySet().stream()
                .map(e -> new BorrowingTrendDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopBorrowersLeaderboard(int limit) {
        List<Object[]> rawList = borrowTransactionRepository.findTopBorrowersRaw();
        return rawList.stream()
                .limit(limit > 0 ? limit : 5)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("borrowerId", ((Number) row[0]).longValue());
                    map.put("name", (String) row[1]);
                    map.put("email", (String) row[2]);
                    map.put("borrowCount", ((Number) row[3]).longValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
