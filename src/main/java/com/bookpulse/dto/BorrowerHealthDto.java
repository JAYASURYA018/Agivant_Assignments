package com.bookpulse.dto;

public class BorrowerHealthDto {
    private long totalBorrowed;
    private long totalReturned;
    private long currentlyBorrowed;
    private long overdueCount;
    private String status; // "GOOD", "WARNING", "RISK"
    private int healthPercentage; // e.g. 100%, 80%, 30%
    private String statusColor; // "green", "amber", "red"

    public BorrowerHealthDto() {
    }

    public BorrowerHealthDto(long totalBorrowed, long totalReturned, long currentlyBorrowed, long overdueCount) {
        this.totalBorrowed = totalBorrowed;
        this.totalReturned = totalReturned;
        this.currentlyBorrowed = currentlyBorrowed;
        this.overdueCount = overdueCount;

        if (overdueCount == 0) {
            this.status = "GOOD";
            this.statusColor = "#10B981"; // Emerald green
            this.healthPercentage = 100;
        } else if (overdueCount == 1) {
            this.status = "WARNING";
            this.statusColor = "#F59E0B"; // Amber yellow
            this.healthPercentage = totalBorrowed > 0 ? (int) Math.max(50, ((totalBorrowed - overdueCount) * 100 / totalBorrowed)) : 75;
        } else {
            this.status = "RISK";
            this.statusColor = "#EF4444"; // Rose red
            this.healthPercentage = totalBorrowed > 0 ? (int) Math.max(10, ((totalBorrowed - overdueCount) * 100 / totalBorrowed)) : 30;
        }
    }

    public long getTotalBorrowed() {
        return totalBorrowed;
    }

    public void setTotalBorrowed(long totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }

    public long getTotalReturned() {
        return totalReturned;
    }

    public void setTotalReturned(long totalReturned) {
        this.totalReturned = totalReturned;
    }

    public long getCurrentlyBorrowed() {
        return currentlyBorrowed;
    }

    public void setCurrentlyBorrowed(long currentlyBorrowed) {
        this.currentlyBorrowed = currentlyBorrowed;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHealthPercentage() {
        return healthPercentage;
    }

    public void setHealthPercentage(int healthPercentage) {
        this.healthPercentage = healthPercentage;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
