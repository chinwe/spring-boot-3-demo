package com.example.demo.circuitbreaker.exception;

/**
 * 舱壁已满异常
 * 当并发请求数超过舱壁容量时抛出此异常
 */
public class BulkheadFullException extends RuntimeException {

    private final String bulkheadName;
    private final int maxConcurrentCalls;

    public BulkheadFullException(String bulkheadName, int maxConcurrentCalls) {
        super(String.format("Bulkhead '%s' is full. Max concurrent calls: %d",
                bulkheadName, maxConcurrentCalls));
        this.bulkheadName = bulkheadName;
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public String getBulkheadName() {
        return bulkheadName;
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }
}
