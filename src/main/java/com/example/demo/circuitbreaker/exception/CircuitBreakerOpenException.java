package com.example.demo.circuitbreaker.exception;

/**
 * 熔断器打开异常
 * 当熔断器处于打开状态时抛出此异常
 */
public class CircuitBreakerOpenException extends RuntimeException {

    private final String circuitBreakerName;

    public CircuitBreakerOpenException(String circuitBreakerName) {
        super(String.format("Circuit breaker '%s' is OPEN and not accepting requests", circuitBreakerName));
        this.circuitBreakerName = circuitBreakerName;
    }

    public CircuitBreakerOpenException(String circuitBreakerName, String message) {
        super(message);
        this.circuitBreakerName = circuitBreakerName;
    }

    public String getCircuitBreakerName() {
        return circuitBreakerName;
    }
}
