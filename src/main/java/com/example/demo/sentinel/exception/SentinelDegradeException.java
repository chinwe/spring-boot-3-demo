package com.example.demo.sentinel.exception;

/**
 * Sentinel 熔断降级自定义异常
 * 用于业务层主动抛出的降级异常
 */
public class SentinelDegradeException extends RuntimeException {

    private final String resource;
    private final String fallbackMessage;

    public SentinelDegradeException(String resource, String message, String fallbackMessage) {
        super(message);
        this.resource = resource;
        this.fallbackMessage = fallbackMessage;
    }

    public SentinelDegradeException(String resource, String message, String fallbackMessage, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.fallbackMessage = fallbackMessage;
    }

    public String getResource() {
        return resource;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }
}
