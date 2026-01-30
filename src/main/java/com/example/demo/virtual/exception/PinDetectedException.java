package com.example.demo.virtual.exception;

/**
 * Pin 检测异常
 * 当检测到虚拟线程被固定时抛出
 */
public class PinDetectedException extends RuntimeException {

    private final String pinLocation;
    private final long durationMillis;

    public PinDetectedException(String message, String pinLocation, long durationMillis) {
        super(message);
        this.pinLocation = pinLocation;
        this.durationMillis = durationMillis;
    }

    public PinDetectedException(String message, String pinLocation) {
        this(message, pinLocation, -1);
    }

    public String getPinLocation() {
        return pinLocation;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
