package com.bookpulse;

import com.bookpulse.controller.BorrowController;
import com.bookpulse.dto.BorrowRequestDto;
import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.ReturnRequestDto;
import com.bookpulse.exception.GlobalExceptionHandler;
import com.bookpulse.exception.InsufficientCopiesException;
import com.bookpulse.service.BorrowService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BorrowControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BorrowService borrowService;

    @InjectMocks
    private BorrowController borrowController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(borrowController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/books/{id}/borrow should return 201 CREATED when borrow succeeds")
    void shouldBorrowBook() throws Exception {
        BorrowRequestDto request = new BorrowRequestDto(1L, 14);

        BorrowTransactionResponseDto response = new BorrowTransactionResponseDto();
        response.setId(10L);
        response.setBookId(1L);
        response.setBorrowerId(1L);
        response.setStatus("BORROWED");
        response.setDueDate(LocalDateTime.now().plusDays(14));

        when(borrowService.borrowBook(eq(1L), any(BorrowRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/books/1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));
    }

    @Test
    @DisplayName("POST /api/books/{id}/borrow should return 400 BAD REQUEST when book is unavailable")
    void shouldReturn400WhenBorrowingUnavailableBook() throws Exception {
        BorrowRequestDto request = new BorrowRequestDto(1L);

        when(borrowService.borrowBook(eq(1L), any(BorrowRequestDto.class)))
                .thenThrow(new InsufficientCopiesException("Book 'Clean Code' is currently unavailable (0 available copies)"));

        mockMvc.perform(post("/api/books/1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_COPIES"));
    }

    @Test
    @DisplayName("POST /api/books/{id}/return should return 200 OK when return succeeds")
    void shouldReturnBook() throws Exception {
        ReturnRequestDto request = new ReturnRequestDto(1L);

        BorrowTransactionResponseDto response = new BorrowTransactionResponseDto();
        response.setId(10L);
        response.setBookId(1L);
        response.setStatus("RETURNED");

        when(borrowService.returnBook(eq(1L), any(ReturnRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/books/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RETURNED"));
    }

    @Test
    @DisplayName("GET /api/books/{id}/history should return 200 OK with transaction list")
    void shouldGetBookHistory() throws Exception {
        BorrowTransactionResponseDto trans = new BorrowTransactionResponseDto();
        trans.setId(1L);
        trans.setBookTitle("Clean Code");

        when(borrowService.getBookHistory(1L)).thenReturn(List.of(trans));

        mockMvc.perform(get("/api/books/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }
}
