package com.example.demo.exception;

/**
 * 网络异常类，用于演示网络相关的重试场景
 */
public class NetworkException extends RuntimeException {
    
    public NetworkException(String message) {
        super(message);
    }
    
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}