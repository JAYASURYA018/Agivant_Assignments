package com.bookpulse.controller;

import com.bookpulse.dto.ApiResponse;
import com.bookpulse.dto.BorrowRequestDto;
import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.ReturnRequestDto;
import com.bookpulse.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/api/books/{id}/borrow")
    public ResponseEntity<ApiResponse<BorrowTransactionResponseDto>> borrowBook(
            @PathVariable("id") Long bookId,
            @RequestBody(required = false) BorrowRequestDto request
    ) {
        BorrowTransactionResponseDto transaction = borrowService.borrowBook(bookId, request);
        return new ResponseEntity<>(ApiResponse.success("Book borrowed successfully. Due date: " + transaction.getDueDate().toLocalDate(), transaction), HttpStatus.CREATED);
    }

    @PostMapping("/api/books/{id}/return")
    public ResponseEntity<ApiResponse<BorrowTransactionResponseDto>> returnBook(
            @PathVariable("id") Long bookId,
            @RequestBody(required = false) ReturnRequestDto request
    ) {
        BorrowTransactionResponseDto transaction = borrowService.returnBook(bookId, request);
        return ResponseEntity.ok(ApiResponse.success("Book returned successfully. Inventory updated.", transaction));
    }

    @PostMapping("/api/transactions/{id}/return")
    public ResponseEntity<ApiResponse<BorrowTransactionResponseDto>> returnByTransactionId(
            @PathVariable("id") Long transactionId
    ) {
        BorrowTransactionResponseDto transaction = borrowService.returnByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Book successfully checked in and returned to library circulation.", transaction));
    }

    @GetMapping("/api/books/{id}/history")
    public ResponseEntity<ApiResponse<List<BorrowTransactionResponseDto>>> getBookHistory(
            @PathVariable("id") Long bookId
    ) {
        List<BorrowTransactionResponseDto> history = borrowService.getBookHistory(bookId);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + history.size() + " historical borrowing records for book", history));
    }

    @GetMapping("/api/transactions")
    public ResponseEntity<ApiResponse<List<BorrowTransactionResponseDto>>> getAllTransactions(
            @RequestParam(required = false, defaultValue = "ALL") String status
    ) {
        List<BorrowTransactionResponseDto> list = borrowService.getAllTransactions(status);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + list.size() + " transactions", list));
    }
}
