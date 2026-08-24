package com.bookpulse.service;

import com.bookpulse.dto.BorrowRequestDto;
import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.ReturnRequestDto;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.BorrowTransaction;
import com.bookpulse.entity.Borrower;
import com.bookpulse.entity.TransactionStatus;
import com.bookpulse.exception.InsufficientCopiesException;
import com.bookpulse.exception.InvalidOperationException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowService {

    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;

    public BorrowService(
            BookRepository bookRepository,
            BorrowerRepository borrowerRepository,
            BorrowTransactionRepository borrowTransactionRepository) {
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
        this.borrowTransactionRepository = borrowTransactionRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BorrowTransactionResponseDto borrowBook(Long bookId, BorrowRequestDto request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + bookId + " was not found"));

        if (request == null || request.getBorrowerId() == null) {
            throw new InvalidOperationException("Borrower ID is required to borrow a book");
        }

        Borrower borrower = borrowerRepository.findById(request.getBorrowerId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower with ID " + request.getBorrowerId() + " was not found"));

        // Check if borrower already has an active loan for this book
        borrowTransactionRepository.findActiveTransaction(bookId, request.getBorrowerId())
                .ifPresent(t -> {
                    throw new InvalidOperationException("Borrower '" + borrower.getName() + "' already has an active copy of this book on loan");
                });

        // Strict business constraint: A book cannot be borrowed if availableCopies == 0
        if (book.getAvailableCopies() <= 0) {
            throw new InsufficientCopiesException("Book '" + book.getTitle() + "' is currently unavailable (0 available copies)");
        }

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (book.getAvailableCopies() < 0) {
            throw new InvalidOperationException("Available copies cannot become negative");
        }
        bookRepository.save(book);

        // Create transaction record
        int loanDays = (request.getLoanDays() != null && request.getLoanDays() > 0) ? request.getLoanDays() : 14;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(loanDays);

        BorrowTransaction transaction = new BorrowTransaction(book, borrower, now, dueDate);
        BorrowTransaction savedTransaction = borrowTransactionRepository.save(transaction);

        return mapToDto(savedTransaction);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BorrowTransactionResponseDto returnBook(Long bookId, ReturnRequestDto request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + bookId + " was not found"));

        BorrowTransaction transaction;
        if (request != null && request.getBorrowerId() != null) {
            Borrower borrower = borrowerRepository.findById(request.getBorrowerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Borrower with ID " + request.getBorrowerId() + " was not found"));

            transaction = borrowTransactionRepository.findActiveTransaction(bookId, request.getBorrowerId())
                    .orElseThrow(() -> new InvalidOperationException(
                            "No active borrow record found for book '" + book.getTitle() + "' and borrower '" + borrower.getName() + "'"));
        } else {
            // Find most recent active loan for this book
            List<BorrowTransaction> activeLoans = borrowTransactionRepository.findByBookIdOrderByBorrowedAtDesc(bookId).stream()
                    .filter(t -> t.getStatus() == TransactionStatus.BORROWED || t.getStatus() == TransactionStatus.OVERDUE)
                    .collect(Collectors.toList());
            if (activeLoans.isEmpty()) {
                throw new InvalidOperationException("No active borrow record found for book '" + book.getTitle() + "'");
            }
            transaction = activeLoans.get(0);
        }

        return processReturn(book, transaction);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BorrowTransactionResponseDto returnByTransactionId(Long transactionId) {
        BorrowTransaction transaction = borrowTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow transaction with ID " + transactionId + " was not found"));

        if (transaction.getStatus() == TransactionStatus.RETURNED) {
            throw new InvalidOperationException("This book loan has already been marked as returned");
        }

        Book book = transaction.getBook();
        return processReturn(book, transaction);
    }

    private BorrowTransactionResponseDto processReturn(Book book, BorrowTransaction transaction) {
        // Business constraint: availableCopies cannot exceed totalCopies
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new InvalidOperationException("Available copies (" + book.getAvailableCopies() + 
                    ") cannot exceed total inventory copies (" + book.getTotalCopies() + ")");
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        transaction.setReturnedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.RETURNED);
        BorrowTransaction updatedTransaction = borrowTransactionRepository.save(transaction);

        return mapToDto(updatedTransaction);
    }

    @Transactional(readOnly = true)
    public List<BorrowTransactionResponseDto> getBookHistory(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book with ID " + bookId + " was not found");
        }
        return borrowTransactionRepository.findByBookIdOrderByBorrowedAtDesc(bookId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BorrowTransactionResponseDto> getAllTransactions(String statusFilter) {
        List<BorrowTransaction> transactions;
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            try {
                TransactionStatus status = TransactionStatus.valueOf(statusFilter.toUpperCase());
                transactions = borrowTransactionRepository.findByStatusOrderByDueDateAsc(status);
            } catch (IllegalArgumentException e) {
                transactions = borrowTransactionRepository.findAll();
            }
        } else {
            transactions = borrowTransactionRepository.findAll();
        }

        return transactions.stream()
                .map(this::mapToDto)
                .sorted((a, b) -> b.getBorrowedAt().compareTo(a.getBorrowedAt()))
                .collect(Collectors.toList());
    }

    public BorrowTransactionResponseDto mapToDto(BorrowTransaction t) {
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
