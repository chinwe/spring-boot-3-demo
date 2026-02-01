package com.example.demo.circuitbreaker.exception;

/**
 * 限流超出异常
 * 当请求超过限流阈值时抛出此异常
 */
public class RateLimitExceededException extends RuntimeException {

    private final String rateLimiterName;
    private final String caller;
    private final int limitForPeriod;

    public RateLimitExceededException(String rateLimiterName, String caller, int limitForPeriod) {
        super(String.format("Rate limit exceeded for limiter '%s', caller '%s'. Limit: %d requests per period",
                rateLimiterName, caller, limitForPeriod));
        this.rateLimiterName = rateLimiterName;
        this.caller = caller;
        this.limitForPeriod = limitForPeriod;
    }

    public String getRateLimiterName() {
        return rateLimiterName;
    }

    public String getCaller() {
        return caller;
    }

    public int getLimitForPeriod() {
        return limitForPeriod;
    }
}
