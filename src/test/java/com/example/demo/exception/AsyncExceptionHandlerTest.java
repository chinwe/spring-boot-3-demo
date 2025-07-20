package com.example.demo.exception;

import com.example.demo.dto.AsyncErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class AsyncExceptionHandlerTest {

    private AsyncExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new AsyncExceptionHandler();
    }

    @Test
    void testHandleAsyncTimeout() {
        // Given
        AsyncRequestTimeoutException exception = new AsyncRequestTimeoutException();

        // When
        ResponseEntity<AsyncErrorResponse> response = exceptionHandler.handleAsyncTimeout(exception);

        // Then
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ASYNC_TIMEOUT", response.getBody().getErrorCode());
        assertEquals("Async request timed out", response.getBody().getErrorMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleTimeout() {
        // Given
        TimeoutException exception = new TimeoutException("Operation timed out");

        // When
        ResponseEntity<AsyncErrorResponse> response = exceptionHandler.handleTimeout(exception);

        // Then
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OPERATION_TIMEOUT", response.getBody().getErrorCode());
        assertTrue(response.getBody().getErrorMessage().contains("Operation timed out"));
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Test runtime error");

        // When
        ResponseEntity<AsyncErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RUNTIME_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getErrorMessage().contains("Test runtime error"));
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception exception = new Exception("Generic error");

        // When
        ResponseEntity<AsyncErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred", response.getBody().getErrorMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}