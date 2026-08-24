package com.bookpulse.controller;

import com.bookpulse.dto.*;
import com.bookpulse.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/most-borrowed")
    public ResponseEntity<ApiResponse<List<MostBorrowedBookDto>>> getMostBorrowedBooks(
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        List<MostBorrowedBookDto> mostBorrowed = analyticsService.getMostBorrowedBooks(limit);
        return ResponseEntity.ok(ApiResponse.success("Most borrowed books loaded", mostBorrowed));
    }

    @GetMapping("/category-stats")
    public ResponseEntity<ApiResponse<List<CategoryStatDto>>> getCategoryStats() {
        List<CategoryStatDto> stats = analyticsService.getCategoryBorrowStats();
        return ResponseEntity.ok(ApiResponse.success("Category distribution statistics loaded", stats));
    }

    @GetMapping("/low-availability")
    public ResponseEntity<ApiResponse<List<BookResponseDto>>> getLowAvailabilityBooks() {
        List<BookResponseDto> lowStock = analyticsService.getLowAvailabilityBooks();
        return ResponseEntity.ok(ApiResponse.success("Low availability alerts loaded", lowStock));
    }

    @GetMapping("/borrowing-trend")
    public ResponseEntity<ApiResponse<List<BorrowingTrendDto>>> getBorrowingTrends() {
        List<BorrowingTrendDto> trends = analyticsService.getBorrowingTrends();
        return ResponseEntity.ok(ApiResponse.success("Monthly borrowing trends loaded", trends));
    }

    @GetMapping("/top-borrowers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopBorrowers(
            @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        List<Map<String, Object>> topBorrowers = analyticsService.getTopBorrowersLeaderboard(limit);
        return ResponseEntity.ok(ApiResponse.success("Most active borrowers leaderboard loaded", topBorrowers));
    }
}
