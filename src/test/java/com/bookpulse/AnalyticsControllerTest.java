package com.bookpulse;

import com.bookpulse.controller.AnalyticsController;
import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.dto.BorrowingTrendDto;
import com.bookpulse.dto.CategoryStatDto;
import com.bookpulse.dto.MostBorrowedBookDto;
import com.bookpulse.exception.GlobalExceptionHandler;
import com.bookpulse.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/analytics/most-borrowed - Should return top borrowed books")
    void testGetMostBorrowed() throws Exception {
        MostBorrowedBookDto b = new MostBorrowedBookDto(1L, "Clean Code", "978-0132350884", "http://example.com/cover.jpg", 10L);
        when(analyticsService.getMostBorrowedBooks(5)).thenReturn(Arrays.asList(b));

        mockMvc.perform(get("/api/analytics/most-borrowed?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.data[0].borrowCount").value(10));
    }

    @Test
    @DisplayName("GET /api/analytics/category-stats - Should return category stats with percentages")
    void testGetCategoryStats() throws Exception {
        CategoryStatDto cat = new CategoryStatDto(1L, "Software Engineering", 20L, 50.0);
        when(analyticsService.getCategoryBorrowStats()).thenReturn(Arrays.asList(cat));

        mockMvc.perform(get("/api/analytics/category-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryName").value("Software Engineering"))
                .andExpect(jsonPath("$.data[0].percentage").value(50.0));
    }

    @Test
    @DisplayName("GET /api/analytics/borrowing-trend - Should return monthly trend data")
    void testGetBorrowingTrend() throws Exception {
        BorrowingTrendDto trend = new BorrowingTrendDto("Aug 2026", 14L);
        when(analyticsService.getBorrowingTrends()).thenReturn(Arrays.asList(trend));

        mockMvc.perform(get("/api/analytics/borrowing-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].periodLabel").value("Aug 2026"))
                .andExpect(jsonPath("$.data[0].count").value(14));
    }

    @Test
    @DisplayName("GET /api/analytics/low-availability - Should return books with low inventory")
    void testGetLowAvailability() throws Exception {
        BookResponseDto book = new BookResponseDto();
        book.setId(5L);
        book.setTitle("Refactoring");
        book.setTotalCopies(3);
        book.setAvailableCopies(1);

        when(analyticsService.getLowAvailabilityBooks()).thenReturn(Arrays.asList(book));

        mockMvc.perform(get("/api/analytics/low-availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Refactoring"))
                .andExpect(jsonPath("$.data[0].availableCopies").value(1));
    }
}
