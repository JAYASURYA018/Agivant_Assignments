package com.bookpulse.dto;

public class BorrowingTrendDto {
    private String periodLabel; // e.g. "May", "Jun", "Jul", "Aug"
    private long count;

    public BorrowingTrendDto() {
    }

    public BorrowingTrendDto(String periodLabel, long count) {
        this.periodLabel = periodLabel;
        this.count = count;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
