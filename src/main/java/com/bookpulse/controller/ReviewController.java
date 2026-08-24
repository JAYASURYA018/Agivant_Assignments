package com.bookpulse.controller;

import com.bookpulse.dto.ApiResponse;
import com.bookpulse.dto.ReviewDto;
import com.bookpulse.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviewsByBookId(@PathVariable Long bookId) {
        List<ReviewDto> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + reviews.size() + " reviews", reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> addReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewDto request
    ) {
        ReviewDto created = reviewService.addReview(bookId, request);
        return new ResponseEntity<>(ApiResponse.success("Review posted successfully! Thank you for your feedback.", created), HttpStatus.CREATED);
    }
}
