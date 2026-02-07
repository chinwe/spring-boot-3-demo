package com.example.demo.sentinel.exception;

/**
 * Sentinel 热点参数限流自定义异常
 * 用于业务层主动抛出的热点参数限流异常
 */
public class SentinelParamFlowException extends RuntimeException {

    private final String resource;
    private final String paramName;
    private final String paramValue;

    public SentinelParamFlowException(String resource, String paramName, String paramValue, String message) {
        super(message);
        this.resource = resource;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public SentinelParamFlowException(String resource, String paramName, String paramValue, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public String getResource() {
        return resource;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamValue() {
        return paramValue;
    }
}
