package com.example.bookmanagement.controller;

import com.example.bookmanagement.model.*;
import com.example.bookmanagement.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {
    private final BookRepository books;
    private final AuthorRepository authors;
    private final CategoryRepository categories;
    private final BorrowerRepository borrowers;
    private final LoanRepository loans;

    public BookController(BookRepository books, AuthorRepository authors,
                           CategoryRepository categories, BorrowerRepository borrowers,
                           LoanRepository loans) {
        this.books = books;
        this.authors = authors;
        this.categories = categories;
        this.borrowers = borrowers;
        this.loans = loans;
    }

    @GetMapping
    public List<Book> getBooks() {
        return books.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable Long id) {
        return books.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Book not found with id " + id)));
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String keyword) {
        return books.findByTitleContainingIgnoreCase(keyword);
    }

    @PostMapping
    public ResponseEntity<?> addBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saveBook(new Book(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Optional<Book> existing = books.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Book not found with id " + id));
        }
        return ResponseEntity.ok(saveBook(existing.get(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (!books.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Book not found with id " + id));
        }
        books.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Book deleted"));
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<?> borrow(@PathVariable Long id, @RequestBody BorrowRequest request) {
        Optional<Book> optionalBook = books.findById(id);
        if (optionalBook.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Book not found with id " + id));

        Book book = optionalBook.get();
        if (book.getAvailableCopies() <= 0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Book is currently unavailable"));

        Borrower borrower = borrowers.findById(request.borrowerId())
                .orElse(null);
        if (borrower == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Borrower not found"));

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setBorrowCount(book.getBorrowCount() + 1);
        books.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrower(borrower);
        loan.setBorrowDate(LocalDate.now());
        loans.save(loan);

        return ResponseEntity.ok(Map.of("message", "Book borrowed successfully"));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        Optional<Book> optionalBook = books.findById(id);
        if (optionalBook.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Book not found with id " + id));

        Loan loan = loans.findFirstByBookIdAndReturnDateIsNull(id);
        if (loan == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "This book is not currently borrowed"));

        Book book = optionalBook.get();
        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
        books.save(book);

        loan.setReturnDate(LocalDate.now());
        loans.save(loan);

        return ResponseEntity.ok(Map.of("message", "Book returned successfully"));
    }

    @GetMapping("/reports/available")
    public List<Book> availableBooks() {
        return books.findAvailableBooks();
    }

    @GetMapping("/reports/borrowed")
    public List<Loan> currentlyBorrowed() {
        return loans.findByReturnDateIsNull();
    }

    @GetMapping("/reports/categories")
    public List<Object[]> categoryCounts() {
        return books.countBooksByCategory();
    }

    @GetMapping("/reports/most-borrowed")
    public ResponseEntity<?> mostBorrowed() {
        List<Book> result = books.findMostBorrowed();
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result.get(0));
    }

    private Book saveBook(Book book, BookRequest request) {
        Author author = authors.findById(request.authorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        Category category = categories.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        book.setTitle(request.title());
        book.setAuthor(author);
        book.setCategory(category);
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());
        return books.save(book);
    }

    public record BookRequest(String title, Long authorId, Long categoryId,
                              int totalCopies, int availableCopies) {}

    public record BorrowRequest(Long borrowerId) {}
}
