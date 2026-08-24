package com.bookpulse.controller;

import com.bookpulse.dto.ApiResponse;
import com.bookpulse.dto.BorrowerDto;
import com.bookpulse.dto.LoginRequestDto;
import com.bookpulse.dto.SignupRequestDto;
import com.bookpulse.entity.Borrower;
import com.bookpulse.exception.DuplicateResourceException;
import com.bookpulse.exception.InvalidOperationException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.BorrowerRepository;
import com.bookpulse.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final BorrowerRepository borrowerRepository;
    private final BorrowerService borrowerService;

    public AuthController(BorrowerRepository borrowerRepository, BorrowerService borrowerService) {
        this.borrowerRepository = borrowerRepository;
        this.borrowerService = borrowerService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<BorrowerDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        if (borrowerRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new DuplicateResourceException("An account with email '" + request.getEmail() + "' already exists. Please Log In directly.");
        }

        Borrower borrower = new Borrower(
                request.getName().trim(),
                request.getEmail().trim(),
                request.getPassword(),
                request.getPhone() != null ? request.getPhone().trim() : null
        );

        Borrower saved = borrowerRepository.save(borrower);
        BorrowerDto dto = borrowerService.getBorrowerById(saved.getId());

        return new ResponseEntity<>(ApiResponse.success("Account created successfully in database! Welcome to BookBasket, " + saved.getName() + "!", dto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<BorrowerDto>> login(@Valid @RequestBody LoginRequestDto request) {
        Borrower borrower = borrowerRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email '" + request.getEmail() + "'. Please Sign Up first to create your account in the database."));

        if (borrower.getPassword() != null && !borrower.getPassword().isEmpty()) {
            if (!borrower.getPassword().equals(request.getPassword())) {
                throw new InvalidOperationException("Invalid password. Please check your password or sign up with a new email.");
            }
        }

        BorrowerDto dto = borrowerService.getBorrowerById(borrower.getId());
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully! Welcome back, " + borrower.getName() + "!", dto));
    }
}
