package com.bookpulse.dto;

import jakarta.validation.constraints.NotBlank;

public class SkyRecommendationRequestDto {

    @NotBlank(message = "Please enter your reading interest or prompt for Sky")
    private String prompt;

    private Long currentBookId; // Optional context if user is viewing a book

    public SkyRecommendationRequestDto() {
    }

    public SkyRecommendationRequestDto(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Long getCurrentBookId() {
        return currentBookId;
    }

    public void setCurrentBookId(Long currentBookId) {
        this.currentBookId = currentBookId;
    }
}
