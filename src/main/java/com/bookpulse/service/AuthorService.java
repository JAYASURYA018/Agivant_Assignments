package com.bookpulse.service;

import com.bookpulse.dto.AuthorDto;
import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.entity.Author;
import com.bookpulse.entity.Book;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.AuthorRepository;
import com.bookpulse.repository.BookRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository, @Lazy BookService bookService) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuthorDto getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with ID " + id + " was not found"));
        return mapToDto(author);
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> getBooksByAuthorId(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author with ID " + authorId + " was not found");
        }
        List<Book> books = bookRepository.findByAuthorsId(authorId);
        return books.stream()
                .map(bookService::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public AuthorDto createAuthor(AuthorDto dto) {
        Author author = new Author(dto.getName(), dto.getBio(), dto.getImageUrl());
        Author saved = authorRepository.save(author);
        return mapToDto(saved);
    }

    public AuthorDto updateAuthor(Long id, AuthorDto dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with ID " + id + " was not found"));
        author.setName(dto.getName());
        author.setBio(dto.getBio());
        author.setImageUrl(dto.getImageUrl());
        Author saved = authorRepository.save(author);
        return mapToDto(saved);
    }

    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author with ID " + id + " was not found");
        }
        authorRepository.deleteById(id);
    }

    public AuthorDto mapToDto(Author author) {
        int count = author.getBooks() != null ? author.getBooks().size() : 0;
        return new AuthorDto(author.getId(), author.getName(), author.getBio(), author.getImageUrl(), count);
    }
}
