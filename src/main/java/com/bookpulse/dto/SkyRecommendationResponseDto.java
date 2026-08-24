package com.bookpulse.dto;

import java.util.List;

public class SkyRecommendationResponseDto {
    private String skyGreeting;
    private String querySummary;
    private List<RecommendationDto> recommendations;
    private String inspirationalQuote;

    public SkyRecommendationResponseDto() {
    }

    public SkyRecommendationResponseDto(String skyGreeting, String querySummary, List<RecommendationDto> recommendations, String inspirationalQuote) {
        this.skyGreeting = skyGreeting;
        this.querySummary = querySummary;
        this.recommendations = recommendations;
        this.inspirationalQuote = inspirationalQuote;
    }

    public String getSkyGreeting() {
        return skyGreeting;
    }

    public void setSkyGreeting(String skyGreeting) {
        this.skyGreeting = skyGreeting;
    }

    public String getQuerySummary() {
        return querySummary;
    }

    public void setQuerySummary(String querySummary) {
        this.querySummary = querySummary;
    }

    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationDto> recommendations) {
        this.recommendations = recommendations;
    }

    public String getInspirationalQuote() {
        return inspirationalQuote;
    }

    public void setInspirationalQuote(String inspirationalQuote) {
        this.inspirationalQuote = inspirationalQuote;
    }
}
