package com.bookpulse.service;

import com.bookpulse.dto.AuthorDto;
import com.bookpulse.dto.BookRequestDto;
import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.dto.CategoryDto;
import com.bookpulse.entity.Author;
import com.bookpulse.entity.Book;
import com.bookpulse.entity.Category;
import com.bookpulse.exception.DuplicateResourceException;
import com.bookpulse.exception.InvalidOperationException;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.AuthorRepository;
import com.bookpulse.repository.BookRepository;
import com.bookpulse.repository.BorrowTransactionRepository;
import com.bookpulse.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;

    public BookService(
            BookRepository bookRepository,
            CategoryRepository categoryRepository,
            AuthorRepository authorRepository,
            BorrowTransactionRepository borrowTransactionRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.borrowTransactionRepository = borrowTransactionRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookResponseDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " was not found"));
        return mapToResponseDto(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> searchBooks(String query, Long categoryId, Boolean availableOnly, String sortBy) {
        String cleanQuery = null;
        if (query != null && !query.trim().isEmpty()) {
            cleanQuery = query.replaceAll("[^a-zA-Z0-9]", "");
        }
        List<Book> books = bookRepository.searchBooks(query != null ? query.trim() : null, cleanQuery, categoryId, availableOnly);

        List<BookResponseDto> dtos = books.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        if ("most-borrowed".equalsIgnoreCase(sortBy)) {
            dtos.sort(Comparator.comparingLong(BookResponseDto::getLifetimeBorrowCount).reversed());
        } else if ("recently-added".equalsIgnoreCase(sortBy)) {
            dtos.sort(Comparator.comparing(BookResponseDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("title".equalsIgnoreCase(sortBy)) {
            dtos.sort(Comparator.comparing(BookResponseDto::getTitle, String.CASE_INSENSITIVE_ORDER));
        }

        return dtos;
    }

    public BookResponseDto createBook(BookRequestDto dto) {
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new DuplicateResourceException("A book with ISBN '" + dto.getIsbn() + "' already exists");
        }

        int total = dto.getTotalCopies();
        int available = dto.getAvailableCopies() != null ? dto.getAvailableCopies() : total;

        if (available < 0) {
            throw new InvalidOperationException("Available copies cannot be negative");
        }
        if (available > total) {
            throw new InvalidOperationException("Available copies (" + available + ") cannot exceed total copies (" + total + ")");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + dto.getCategoryId() + " was not found"));

        Set<Author> authors = getAuthorsFromIds(dto.getAuthorIds());

        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setPublicationYear(dto.getPublicationYear());
        book.setTotalCopies(total);
        book.setAvailableCopies(available);
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setCategory(category);
        book.setAuthors(authors);

        Book saved = bookRepository.save(book);
        return mapToResponseDto(saved);
    }

    public BookResponseDto updateBook(Long id, BookRequestDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " was not found"));

        if (!book.getIsbn().equals(dto.getIsbn()) && bookRepository.existsByIsbnAndIdNot(dto.getIsbn(), id)) {
            throw new DuplicateResourceException("A book with ISBN '" + dto.getIsbn() + "' already exists");
        }

        int total = dto.getTotalCopies();
        int available = dto.getAvailableCopies() != null ? dto.getAvailableCopies() : book.getAvailableCopies();

        if (available < 0) {
            throw new InvalidOperationException("Available copies cannot be negative");
        }
        if (available > total) {
            throw new InvalidOperationException("Available copies (" + available + ") cannot exceed total copies (" + total + ")");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + dto.getCategoryId() + " was not found"));

        Set<Author> authors = getAuthorsFromIds(dto.getAuthorIds());

        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setPublicationYear(dto.getPublicationYear());
        book.setTotalCopies(total);
        book.setAvailableCopies(available);
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setCategory(category);
        book.setAuthors(authors);

        Book saved = bookRepository.save(book);
        return mapToResponseDto(saved);
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " was not found"));

        // Check if there are active loans
        long activeLoans = borrowTransactionRepository.findActiveTransactionsForBook(id).size();
        if (activeLoans > 0) {
            throw new InvalidOperationException("Cannot delete book while " + activeLoans + " active copies are currently borrowed");
        }

        bookRepository.delete(book);
    }

    private Set<Author> getAuthorsFromIds(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new InvalidOperationException("At least one author must be specified for the book");
        }
        Set<Author> authors = new HashSet<>();
        for (Long authorId : authorIds) {
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author with ID " + authorId + " was not found"));
            authors.add(author);
        }
        return authors;
    }

    public BookResponseDto mapToResponseDto(Book book) {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setDescription(book.getDescription());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setBorrowedCopies(Math.max(0, book.getTotalCopies() - book.getAvailableCopies()));
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());

        if (book.getCategory() != null) {
            dto.setCategory(new CategoryDto(book.getCategory().getId(), book.getCategory().getName(), book.getCategory().getDescription()));
        }

        if (book.getAuthors() != null) {
            List<AuthorDto> authorDtos = book.getAuthors().stream()
                    .map(a -> new AuthorDto(a.getId(), a.getName(), a.getBio()))
                    .collect(Collectors.toList());
            dto.setAuthors(authorDtos);
        }

        // Count historical transactions
        try {
            long count = borrowTransactionRepository.findByBookIdOrderByBorrowedAtDesc(book.getId()).size();
            dto.setLifetimeBorrowCount(count);
        } catch (Exception e) {
            dto.setLifetimeBorrowCount(0);
        }

        return dto;
    }
}
