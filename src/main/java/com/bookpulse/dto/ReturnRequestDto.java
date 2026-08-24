package com.bookpulse.dto;

import jakarta.validation.constraints.NotNull;

public class ReturnRequestDto {

    @NotNull(message = "Borrower ID is required to process return")
    private Long borrowerId;

    public ReturnRequestDto() {
    }

    public ReturnRequestDto(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }
}
