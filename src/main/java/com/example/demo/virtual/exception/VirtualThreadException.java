package com.example.demo.virtual.exception;

/**
 * 虚拟线程异常
 * 虚拟线程相关操作的基础异常类
 */
public class VirtualThreadException extends RuntimeException {

    public VirtualThreadException(String message) {
        super(message);
    }

    public VirtualThreadException(String message, Throwable cause) {
        super(message, cause);
    }
}
