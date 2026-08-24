package com.bookpulse;

import com.bookpulse.controller.AuthorController;
import com.bookpulse.dto.AuthorDto;
import com.bookpulse.exception.GlobalExceptionHandler;
import com.bookpulse.service.AuthorService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    private ObjectMapper objectMapper;
    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        authorDto = new AuthorDto();
        authorDto.setId(1L);
        authorDto.setName("Joshua Bloch");
        authorDto.setBio("Author of Effective Java");
    }

    @Test
    @DisplayName("GET /api/authors - Should return all authors")
    void testGetAllAuthors() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(Arrays.asList(authorDto));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Joshua Bloch"));
    }

    @Test
    @DisplayName("GET /api/authors/{id} - Should return single author")
    void testGetAuthorById() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(authorDto);

        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Joshua Bloch"));
    }

    @Test
    @DisplayName("POST /api/authors - Should create new author")
    void testCreateAuthor() throws Exception {
        when(authorService.createAuthor(any(AuthorDto.class))).thenReturn(authorDto);

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Joshua Bloch"));
    }
}
