package com.bookpulse.controller;

import com.bookpulse.dto.ApiResponse;
import com.bookpulse.dto.AuthorDto;
import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDto>>> getAllAuthors() {
        List<AuthorDto> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + authors.size() + " authors", authors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorDto>> getAuthorById(@PathVariable Long id) {
        AuthorDto author = authorService.getAuthorById(id);
        return ResponseEntity.ok(ApiResponse.success("Author details retrieved", author));
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<ApiResponse<List<BookResponseDto>>> getBooksByAuthorId(@PathVariable Long id) {
        List<BookResponseDto> books = authorService.getBooksByAuthorId(id);
        return ResponseEntity.ok(ApiResponse.success("Retrieved " + books.size() + " books for author", books));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuthorDto>> createAuthor(@Valid @RequestBody AuthorDto request) {
        AuthorDto created = authorService.createAuthor(request);
        return new ResponseEntity<>(ApiResponse.success("Author created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorDto>> updateAuthor(@PathVariable Long id, @Valid @RequestBody AuthorDto request) {
        AuthorDto updated = authorService.updateAuthor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Author updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.ok(ApiResponse.success("Author deleted successfully", null));
    }
}
