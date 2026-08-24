package com.bookpulse.dto;

public class CategoryStatDto {
    private Long categoryId;
    private String categoryName;
    private long borrowCount;
    private double percentage;

    public CategoryStatDto() {
    }

    public CategoryStatDto(Long categoryId, String categoryName, long borrowCount, double percentage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.borrowCount = borrowCount;
        this.percentage = percentage;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(long borrowCount) {
        this.borrowCount = borrowCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
