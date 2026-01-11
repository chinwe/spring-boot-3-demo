package com.example.demo.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务异常测试类
 */
class BusinessExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Given
        String message = "Business logic error";

        // When
        BusinessException exception = new BusinessException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Given
        String message = "Business logic error";
        Throwable cause = new RuntimeException("Root cause");

        // When
        BusinessException exception = new BusinessException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testIsRuntimeException() {
        // Given
        BusinessException exception = new BusinessException("Test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }
}
