package com.example.demo.sentinel.exception;

/**
 * Sentinel 流控自定义异常
 * 用于业务层主动抛出的流控异常
 */
public class SentinelFlowControlException extends RuntimeException {

    private final String resource;

    public SentinelFlowControlException(String resource, String message) {
        super(message);
        this.resource = resource;
    }

    public SentinelFlowControlException(String resource, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
