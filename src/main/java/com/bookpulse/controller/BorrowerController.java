package com.bookpulse.controller;

import com.bookpulse.dto.ApiResponse;
import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.BorrowerDto;
import com.bookpulse.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@CrossOrigin(origins = "*")
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BorrowerDto>>> getAllBorrowers() {
        List<BorrowerDto> borrowers = borrowerService.getAllBorrowers();
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + borrowers.size() + " registered borrowers with health metrics", borrowers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BorrowerDto>> getBorrowerById(@PathVariable Long id) {
        BorrowerDto borrower = borrowerService.getBorrowerById(id);
        return ResponseEntity.ok(ApiResponse.success("Borrower profile retrieved", borrower));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<BorrowTransactionResponseDto>>> getBorrowerHistory(@PathVariable Long id) {
        List<BorrowTransactionResponseDto> history = borrowerService.getBorrowerHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + history.size() + " borrowing transactions for borrower", history));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BorrowerDto>> createBorrower(@Valid @RequestBody BorrowerDto request) {
        BorrowerDto created = borrowerService.createBorrower(request);
        return new ResponseEntity<>(ApiResponse.success("Borrower registered successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BorrowerDto>> updateBorrower(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerDto request
    ) {
        BorrowerDto updated = borrowerService.updateBorrower(id, request);
        return ResponseEntity.ok(ApiResponse.success("Borrower updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBorrower(@PathVariable Long id) {
        borrowerService.deleteBorrower(id);
        return ResponseEntity.ok(ApiResponse.success("Borrower removed successfully", null));
    }
}
