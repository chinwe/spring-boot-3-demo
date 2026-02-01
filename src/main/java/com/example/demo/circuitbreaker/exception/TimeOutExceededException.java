package com.example.demo.circuitbreaker.exception;

/**
 * 超时异常
 * 当请求执行时间超过超时阈值时抛出此异常
 */
public class TimeOutExceededException extends RuntimeException {

    private final String timeLimiterName;
    private final long timeoutDuration;

    public TimeOutExceededException(String timeLimiterName, long timeoutDuration) {
        super(String.format("Timeout exceeded for limiter '%s'. Timeout duration: %d ms",
                timeLimiterName, timeoutDuration));
        this.timeLimiterName = timeLimiterName;
        this.timeoutDuration = timeoutDuration;
    }

    public String getTimeLimiterName() {
        return timeLimiterName;
    }

    public long getTimeoutDuration() {
        return timeoutDuration;
    }
}
