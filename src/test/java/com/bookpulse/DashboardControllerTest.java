package com.bookpulse;

import com.bookpulse.controller.DashboardController;
import com.bookpulse.dto.DashboardStatsDto;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private DashboardController dashboardController;

    private DashboardStatsDto statsDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        statsDto = new DashboardStatsDto();
        statsDto.setTotalBooks(12);
        statsDto.setTotalCopies(45);
        statsDto.setAvailableCopies(36);
        statsDto.setBorrowedBooks(9);
        statsDto.setOverdueBooks(2);
        statsDto.setMostBorrowedBookTitle("Clean Code");
        statsDto.setMostPopularCategoryName("Software Engineering");
        statsDto.setTotalBorrowers(8);
    }

    @Test
    @DisplayName("GET /api/dashboard - Should return comprehensive dashboard statistics")
    void testGetDashboardStats() throws Exception {
        when(analyticsService.getDashboardStats()).thenReturn(statsDto);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalBooks").value(12))
                .andExpect(jsonPath("$.data.totalCopies").value(45))
                .andExpect(jsonPath("$.data.availableCopies").value(36))
                .andExpect(jsonPath("$.data.overdueBooks").value(2))
                .andExpect(jsonPath("$.data.mostBorrowedBookTitle").value("Clean Code"));
    }
}
