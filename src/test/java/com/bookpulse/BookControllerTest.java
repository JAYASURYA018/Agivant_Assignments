package com.bookpulse;

import com.bookpulse.controller.BookController;
import com.bookpulse.dto.BookRequestDto;
import com.bookpulse.dto.BookResponseDto;
import com.bookpulse.exception.GlobalExceptionHandler;
import com.bookpulse.exception.ResourceNotFoundException;
import com.bookpulse.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/books should return 200 OK with list of books")
    void shouldGetAllBooks() throws Exception {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(1L);
        dto.setTitle("Clean Code");
        dto.setIsbn("9780132350884");

        when(bookService.getAllBooks()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 200 OK for existing book")
    void shouldGetBookById() throws Exception {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(1L);
        dto.setTitle("Clean Code");

        when(bookService.getBookById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 404 NOT FOUND for non-existent book")
    void shouldReturn404ForMissingBook() throws Exception {
        when(bookService.getBookById(999L)).thenThrow(new ResourceNotFoundException("Book with ID 999 was not found"));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Book with ID 999 was not found"));
    }

    @Test
    @DisplayName("POST /api/books should return 201 CREATED for valid payload")
    void shouldCreateBook() throws Exception {
        BookRequestDto request = new BookRequestDto();
        request.setTitle("Effective Java");
        request.setIsbn("9780134685991");
        request.setPublicationYear(2018);
        request.setTotalCopies(5);
        request.setCategoryId(1L);
        request.setAuthorIds(List.of(1L));

        BookResponseDto response = new BookResponseDto();
        response.setId(2L);
        response.setTitle("Effective Java");
        response.setIsbn("9780134685991");

        when(bookService.createBook(any(BookRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2L));
    }

    @Test
    @DisplayName("POST /api/books should return 400 BAD REQUEST when required fields are missing")
    void shouldReturn400OnInvalidPayload() throws Exception {
        BookRequestDto invalidRequest = new BookRequestDto(); // Missing required fields

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
