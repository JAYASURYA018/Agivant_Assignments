package com.bookpulse;

import com.bookpulse.dto.AuthorDto;
import com.bookpulse.entity.Author;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.repository.AuthorRepository;
import com.bookpulse.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1L);
        author.setName("Robert C. Martin");
        author.setBio("Author of Clean Code and Clean Architecture");
    }

    @Test
    @DisplayName("Should retrieve all authors")
    void testGetAllAuthors() {
        when(authorRepository.findAll()).thenReturn(Arrays.asList(author));

        List<AuthorDto> result = authorService.getAllAuthors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Robert C. Martin", result.get(0).getName());
    }

    @Test
    @DisplayName("Should retrieve author by valid ID")
    void testGetAuthorById_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        AuthorDto result = authorService.getAuthorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Robert C. Martin", result.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when author ID does not exist")
    void testGetAuthorById_NotFound() {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorService.getAuthorById(999L));
    }

    @Test
    @DisplayName("Should create a new author")
    void testCreateAuthor() {
        AuthorDto dto = new AuthorDto();
        dto.setName("Martin Fowler");
        dto.setBio("Author of Refactoring");

        Author saved = new Author();
        saved.setId(2L);
        saved.setName("Martin Fowler");
        saved.setBio("Author of Refactoring");

        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        AuthorDto result = authorService.createAuthor(dto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Martin Fowler", result.getName());
    }
}
