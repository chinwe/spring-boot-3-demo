package com.example.demo.exception;

/**
 * 临时异常类，用于演示retry功能
 * 这种异常通常是由于临时性问题（如网络问题、服务暂时不可用等）引起的
 */
public class TemporaryException extends RuntimeException {
    
    public TemporaryException(String message) {
        super(message);
    }
    
    public TemporaryException(String message, Throwable cause) {
        super(message, cause);
    }
}