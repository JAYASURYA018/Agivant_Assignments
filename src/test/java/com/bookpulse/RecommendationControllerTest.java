package com.bookpulse;

import com.bookpulse.controller.RecommendationController;
import com.bookpulse.dto.RecommendationDto;
import com.bookpulse.dto.SkyRecommendationRequestDto;
import com.bookpulse.dto.SkyRecommendationResponseDto;
import com.bookpulse.exception.GlobalExceptionHandler;
import com.bookpulse.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RecommendationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/recommendations/{bookId} - Should return algorithmic recommendations")
    void testGetRecommendationsForBook() throws Exception {
        RecommendationDto rec = new RecommendationDto();
        rec.setBookId(2L);
        rec.setTitle("Clean Architecture");
        rec.setReason("Shares category 'Software Engineering' with selected title.");
        rec.setMatchScore(0.9);

        when(recommendationService.getRecommendationsForBook(1L)).thenReturn(Arrays.asList(rec));

        mockMvc.perform(get("/api/recommendations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].bookId").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Clean Architecture"));
    }

    @Test
    @DisplayName("POST /api/recommendations/sky - Should return Sky AI smart suggestions")
    void testGetSkyRecommendations() throws Exception {
        SkyRecommendationRequestDto req = new SkyRecommendationRequestDto();
        req.setPrompt("I am interested in clean code and software design");

        RecommendationDto rec = new RecommendationDto();
        rec.setBookId(1L);
        rec.setTitle("Clean Code");
        rec.setReason("Direct match for 'clean code'");

        SkyRecommendationResponseDto resp = new SkyRecommendationResponseDto();
        resp.setSkyGreeting("Hello! Sky here 🪄");
        resp.setQuerySummary("Matched clean code and software design");
        resp.setInspirationalQuote("Every book holds a little world. Find yours.");
        resp.setRecommendations(Arrays.asList(rec));

        when(recommendationService.getSkyAiRecommendations(any(SkyRecommendationRequestDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/recommendations/sky")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.skyGreeting").value("Hello! Sky here 🪄"))
                .andExpect(jsonPath("$.data.recommendations[0].title").value("Clean Code"));
    }
}
