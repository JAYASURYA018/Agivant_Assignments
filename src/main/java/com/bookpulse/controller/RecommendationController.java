package com.bookpulse.controller;

import com.bookpulse.dto.ApiResponse;
import com.bookpulse.dto.RecommendationDto;
import com.bookpulse.dto.SkyRecommendationRequestDto;
import com.bookpulse.dto.SkyRecommendationResponseDto;
import com.bookpulse.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getRecommendationsForBook(@PathVariable Long bookId) {
        List<RecommendationDto> recs = recommendationService.getRecommendationsForBook(bookId);
        return ResponseEntity.ok(ApiResponse.success("Smart recommendations generated for book", recs));
    }

    @PostMapping("/sky")
    public ResponseEntity<ApiResponse<SkyRecommendationResponseDto>> getSkyAiRecommendations(
            @Valid @RequestBody SkyRecommendationRequestDto request
    ) {
        SkyRecommendationResponseDto response = recommendationService.getSkyAiRecommendations(request);
        return ResponseEntity.ok(ApiResponse.success("Sky AI recommendations generated", response));
    }
}
