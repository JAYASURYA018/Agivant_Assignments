package com.bookpulse.dto;

import java.util.List;

public class RecommendationDto {
    private Long bookId;
    private String title;
    private String isbn;
    private String coverImageUrl;
    private String categoryName;
    private List<String> authors;
    private int availableCopies;
    private String reason; // e.g. "Same Category", "Same Author: Robert C. Martin", "Frequently Borrowed Together"
    private double matchScore;

    public RecommendationDto() {
    }

    public RecommendationDto(Long bookId, String title, String isbn, String coverImageUrl, String categoryName, List<String> authors, int availableCopies, String reason, double matchScore) {
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.coverImageUrl = coverImageUrl;
        this.categoryName = categoryName;
        this.authors = authors;
        this.availableCopies = availableCopies;
        this.reason = reason;
        this.matchScore = matchScore;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }
}
