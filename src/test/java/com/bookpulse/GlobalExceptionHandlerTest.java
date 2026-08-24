package com.bookpulse;

import com.bookpulse.dto.ErrorResponse;
import com.bookpulse.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException with 404 NOT_FOUND")
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Book not found with id: 99");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("NOT_FOUND", response.getBody().getError());
        assertEquals("Book not found with id: 99", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle InsufficientCopiesException with 400 BAD_REQUEST")
    void testHandleInsufficientCopies() {
        InsufficientCopiesException ex = new InsufficientCopiesException("No available copies to borrow");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientCopies(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("INSUFFICIENT_COPIES", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException with 409 CONFLICT")
    void testHandleDuplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("ISBN already registered");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResource(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("DUPLICATE_RESOURCE", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle InvalidOperationException with 400 BAD_REQUEST")
    void testHandleInvalidOperation() {
        InvalidOperationException ex = new InvalidOperationException("Book is already returned");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidOperation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("INVALID_OPERATION", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle Generic Exception with 500 INTERNAL_SERVER_ERROR")
    void testHandleGlobalException() {
        Exception ex = new NullPointerException("Unexpected null reference");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
    }
}
