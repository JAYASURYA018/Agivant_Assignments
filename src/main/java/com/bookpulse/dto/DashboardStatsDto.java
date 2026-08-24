package com.bookpulse.dto;

import java.util.List;

public class DashboardStatsDto {
    private long totalBooks;
    private long totalCopies;
    private long availableCopies;
    private long borrowedBooks;
    private long overdueBooks;
    private long totalBorrowers;
    private String mostBorrowedBookTitle;
    private long mostBorrowedBookCount;
    private String mostPopularCategoryName;
    private List<BorrowTransactionResponseDto> recentTransactions;

    public DashboardStatsDto() {
    }

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(long totalCopies) {
        this.totalCopies = totalCopies;
    }

    public long getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(long availableCopies) {
        this.availableCopies = availableCopies;
    }

    public long getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(long borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    public long getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(long overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public long getTotalBorrowers() {
        return totalBorrowers;
    }

    public void setTotalBorrowers(long totalBorrowers) {
        this.totalBorrowers = totalBorrowers;
    }

    public String getMostBorrowedBookTitle() {
        return mostBorrowedBookTitle;
    }

    public void setMostBorrowedBookTitle(String mostBorrowedBookTitle) {
        this.mostBorrowedBookTitle = mostBorrowedBookTitle;
    }

    public long getMostBorrowedBookCount() {
        return mostBorrowedBookCount;
    }

    public void setMostBorrowedBookCount(long mostBorrowedBookCount) {
        this.mostBorrowedBookCount = mostBorrowedBookCount;
    }

    public String getMostPopularCategoryName() {
        return mostPopularCategoryName;
    }

    public void setMostPopularCategoryName(String mostPopularCategoryName) {
        this.mostPopularCategoryName = mostPopularCategoryName;
    }

    public List<BorrowTransactionResponseDto> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<BorrowTransactionResponseDto> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
