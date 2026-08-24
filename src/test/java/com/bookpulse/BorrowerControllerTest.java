package com.bookpulse;

import com.bookpulse.controller.BorrowerController;
import com.bookpulse.dto.BorrowTransactionResponseDto;
import com.bookpulse.dto.BorrowerDto;
import com.bookpulse.dto.BorrowerHealthDto;
import com.bookpulse.exception.GlobalExceptionHandler;
import com.bookpulse.service.BorrowerService;
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
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BorrowerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BorrowerService borrowerService;

    @InjectMocks
    private BorrowerController borrowerController;

    private ObjectMapper objectMapper;
    private BorrowerDto borrowerDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(borrowerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        borrowerDto = new BorrowerDto();
        borrowerDto.setId(1L);
        borrowerDto.setName("Likitha Nambari");
        borrowerDto.setEmail("likitha@example.com");
        borrowerDto.setPhone("+1-555-0101");

        BorrowerHealthDto health = new BorrowerHealthDto();
        health.setStatus("GOOD");
        health.setHealthPercentage(100);
        borrowerDto.setHealth(health);
    }

    @Test
    @DisplayName("GET /api/borrowers - Should return all borrowers with health scores")
    void testGetAllBorrowers() throws Exception {
        when(borrowerService.getAllBorrowers()).thenReturn(Arrays.asList(borrowerDto));

        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Likitha Nambari"))
                .andExpect(jsonPath("$.data[0].health.status").value("GOOD"));
    }

    @Test
    @DisplayName("GET /api/borrowers/{id} - Should return single borrower by ID")
    void testGetBorrowerById() throws Exception {
        when(borrowerService.getBorrowerById(1L)).thenReturn(borrowerDto);

        mockMvc.perform(get("/api/borrowers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("likitha@example.com"));
    }

    @Test
    @DisplayName("POST /api/borrowers - Should register new borrower")
    void testCreateBorrower() throws Exception {
        when(borrowerService.createBorrower(any(BorrowerDto.class))).thenReturn(borrowerDto);

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Likitha Nambari"));
    }

    @Test
    @DisplayName("GET /api/borrowers/{id}/history - Should return loan transaction history for borrower")
    void testGetBorrowerHistory() throws Exception {
        BorrowTransactionResponseDto tx = new BorrowTransactionResponseDto();
        tx.setId(101L);
        tx.setBookTitle("Clean Code");
        tx.setBorrowerName("Likitha Nambari");
        tx.setStatus("BORROWED");
        tx.setBorrowedAt(LocalDateTime.now().minusDays(3));

        when(borrowerService.getBorrowerHistory(1L)).thenReturn(Arrays.asList(tx));

        mockMvc.perform(get("/api/borrowers/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].bookTitle").value("Clean Code"));
    }
}
