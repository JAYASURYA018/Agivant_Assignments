package com.bookpulse.dto;

import jakarta.validation.constraints.NotNull;

public class BorrowRequestDto {

    @NotNull(message = "Borrower ID is required to borrow a book")
    private Long borrowerId;

    private Integer loanDays; // Optional: custom duration (defaults to 14 days)

    public BorrowRequestDto() {
    }

    public BorrowRequestDto(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public BorrowRequestDto(Long borrowerId, Integer loanDays) {
        this.borrowerId = borrowerId;
        this.loanDays = loanDays;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Integer getLoanDays() {
        return loanDays;
    }

    public void setLoanDays(Integer loanDays) {
        this.loanDays = loanDays;
    }
}
