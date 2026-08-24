package com.bookpulse.service;

import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.BorrowerDto;
import com.bookpulse.dto.BorrowerHealthDto;
import com.bookpulse.entity.BorrowTransaction;
import com.bookpulse.entity.Borrower;
import com.bookpulse.entity.TransactionStatus;
import com.bookpulse.exception.DuplicateResourceException;
import com.bookpulse.exception.InvalidOperationException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;

    public BorrowerService(BorrowerRepository borrowerRepository, BorrowTransactionRepository borrowTransactionRepository) {
        this.borrowerRepository = borrowerRepository;
        this.borrowTransactionRepository = borrowTransactionRepository;
    }

    @Transactional(readOnly = true)
    public List<BorrowerDto> getAllBorrowers() {
        return borrowerRepository.findAll().stream()
                .map(this::mapToDtoWithHealth)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BorrowerDto getBorrowerById(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower with ID " + id + " was not found"));
        return mapToDtoWithHealth(borrower);
    }

    @Transactional(readOnly = true)
    public List<BorrowTransactionResponseDto> getBorrowerHistory(Long id) {
        if (!borrowerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Borrower with ID " + id + " was not found");
        }
        return borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(id).stream()
                .map(this::mapTransactionToDto)
                .collect(Collectors.toList());
    }

    public BorrowerDto createBorrower(BorrowerDto dto) {
        if (borrowerRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new DuplicateResourceException("A borrower with email '" + dto.getEmail() + "' already exists");
        }
        Borrower borrower = new Borrower(dto.getName(), dto.getEmail(), dto.getPhone());
        Borrower saved = borrowerRepository.save(borrower);
        return mapToDtoWithHealth(saved);
    }

    public BorrowerDto updateBorrower(Long id, BorrowerDto dto) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower with ID " + id + " was not found"));

        if (!borrower.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                borrowerRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateResourceException("A borrower with email '" + dto.getEmail() + "' already exists");
        }

        borrower.setName(dto.getName());
        borrower.setEmail(dto.getEmail());
        borrower.setPhone(dto.getPhone());
        Borrower saved = borrowerRepository.save(borrower);
        return mapToDtoWithHealth(saved);
    }

    public void deleteBorrower(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower with ID " + id + " was not found"));

        List<BorrowTransaction> transactions = borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(id);
        boolean hasActiveLoans = transactions.stream().anyMatch(t -> t.getStatus() == TransactionStatus.BORROWED);
        if (hasActiveLoans) {
            throw new InvalidOperationException("Cannot delete borrower with currently active borrowed books");
        }

        borrowerRepository.delete(borrower);
    }

    public BorrowerDto mapToDtoWithHealth(Borrower borrower) {
        BorrowerDto dto = new BorrowerDto();
        dto.setId(borrower.getId());
        dto.setName(borrower.getName());
        dto.setEmail(borrower.getEmail());
        dto.setPhone(borrower.getPhone());
        dto.setCreatedAt(borrower.getCreatedAt());

        // Calculate Borrowing Health
        List<BorrowTransaction> transactions = borrowTransactionRepository.findByBorrowerIdOrderByBorrowedAtDesc(borrower.getId());
        long totalBorrowed = transactions.size();
        long totalReturned = transactions.stream().filter(t -> t.getStatus() == TransactionStatus.RETURNED).count();
        long currentlyBorrowed = transactions.stream().filter(t -> t.getStatus() == TransactionStatus.BORROWED).count();
        
        LocalDateTime now = LocalDateTime.now();
        long overdueCount = transactions.stream().filter(t -> 
                t.getStatus() == TransactionStatus.OVERDUE || 
                (t.getStatus() == TransactionStatus.BORROWED && t.getDueDate().isBefore(now))
        ).count();

        BorrowerHealthDto health = new BorrowerHealthDto(totalBorrowed, totalReturned, currentlyBorrowed, overdueCount);
        dto.setHealth(health);

        return dto;
    }

    private BorrowTransactionResponseDto mapTransactionToDto(BorrowTransaction t) {
        BorrowTransactionResponseDto dto = new BorrowTransactionResponseDto();
        dto.setId(t.getId());
        dto.setBookId(t.getBook().getId());
        dto.setBookTitle(t.getBook().getTitle());
        dto.setBookIsbn(t.getBook().getIsbn());
        dto.setBookCoverImageUrl(t.getBook().getCoverImageUrl());
        dto.setBorrowerId(t.getBorrower().getId());
        dto.setBorrowerName(t.getBorrower().getName());
        dto.setBorrowerEmail(t.getBorrower().getEmail());
        dto.setBorrowedAt(t.getBorrowedAt());
        dto.setDueDate(t.getDueDate());
        dto.setReturnedAt(t.getReturnedAt());
        dto.setStatus(t.getStatus().name());

        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = t.getStatus() == TransactionStatus.OVERDUE || 
                (t.getStatus() == TransactionStatus.BORROWED && t.getDueDate().isBefore(now));
        dto.setOverdue(isOverdue);

        if (t.getStatus() == TransactionStatus.BORROWED) {
            long days = ChronoUnit.DAYS.between(now, t.getDueDate());
            dto.setDaysRemaining(days);
        } else {
            dto.setDaysRemaining(0);
        }

        return dto;
    }
}
