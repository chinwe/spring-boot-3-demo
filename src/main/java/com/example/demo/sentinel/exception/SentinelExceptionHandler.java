package com.example.demo.sentinel.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.example.demo.sentinel.dto.SentinelBlockedExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 异常处理器
 * 处理 Sentinel 的各种阻塞异常
 *
 * 优先级设置为 1，在通用异常处理器之前执行
 */
@Slf4j
@Order(1)
@RestControllerAdvice
public class SentinelExceptionHandler {

    /**
     * 处理 Sentinel 流控异常
     */
    @ExceptionHandler(FlowException.class)
    public ResponseEntity<Map<String, Object>> handleFlowException(FlowException ex) {
        log.warn("Flow control triggered for resource: {}, rule: {}",
                ex.getRuleLimitApp(), ex.getRule());

        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";

        SentinelBlockedExceptionDto dto = SentinelBlockedExceptionDto.fromBlockException(
                resource, "FlowException", "QPS limit exceeded"
        );

        return createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, dto);
    }

    /**
     * 处理 Sentinel 熔断降级异常
     */
    @ExceptionHandler(DegradeException.class)
    public ResponseEntity<Map<String, Object>> handleDegradeException(DegradeException ex) {
        log.warn("Circuit breaker triggered for resource: {}, rule: {}",
                ex.getRuleLimitApp(), ex.getRule());

        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";

        SentinelBlockedExceptionDto dto = SentinelBlockedExceptionDto.fromBlockException(
                resource, "DegradeException", "Service degraded"
        );

        return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, dto);
    }

    /**
     * 处理 Sentinel 系统规则异常
     */
    @ExceptionHandler(SystemBlockException.class)
    public ResponseEntity<Map<String, Object>> handleSystemBlockException(SystemBlockException ex) {
        log.warn("System rule triggered for resource: {}, rule: {}",
                ex.getRuleLimitApp(), ex.getRule());

        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";

        SentinelBlockedExceptionDto dto = SentinelBlockedExceptionDto.fromBlockException(
                resource, "SystemBlockException", "System load limit exceeded"
        );

        return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, dto);
    }

    /**
     * 处理 Sentinel 授权异常
     */
    @ExceptionHandler(AuthorityException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorityException(AuthorityException ex) {
        log.warn("Authority check failed for resource: {}, rule: {}",
                ex.getRuleLimitApp(), ex.getRule());

        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";

        SentinelBlockedExceptionDto dto = SentinelBlockedExceptionDto.fromBlockException(
                resource, "AuthorityException", "Access denied"
        );

        return createErrorResponse(HttpStatus.FORBIDDEN, dto);
    }

    /**
     * 处理通用 BlockException
     */
    @ExceptionHandler(BlockException.class)
    public ResponseEntity<Map<String, Object>> handleBlockException(BlockException ex) {
        log.warn("Sentinel block exception occurred: {}", ex.getClass().getSimpleName());

        String resource = "unknown";
        String blockType = ex.getClass().getSimpleName();

        SentinelBlockedExceptionDto dto = SentinelBlockedExceptionDto.fromBlockException(
                resource, blockType, "Request blocked by Sentinel"
        );

        return createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, dto);
    }

    /**
     * 处理自定义业务异常 - SentinelFlowControlException
     */
    @ExceptionHandler(SentinelFlowControlException.class)
    public ResponseEntity<Map<String, Object>> handleSentinelFlowControlException(SentinelFlowControlException ex) {
        log.warn("Custom Sentinel flow control exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "SENTINEL_FLOW_CONTROL");
        response.put("message", ex.getMessage());
        response.put("resource", ex.getResource());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * 处理自定义业务异常 - SentinelDegradeException
     */
    @ExceptionHandler(SentinelDegradeException.class)
    public ResponseEntity<Map<String, Object>> handleSentinelDegradeException(SentinelDegradeException ex) {
        log.warn("Custom Sentinel degrade exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "SENTINEL_DEGRADE");
        response.put("message", ex.getMessage());
        response.put("resource", ex.getResource());
        response.put("fallbackMessage", ex.getFallbackMessage());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 处理自定义业务异常 - SentinelParamFlowException
     */
    @ExceptionHandler(SentinelParamFlowException.class)
    public ResponseEntity<Map<String, Object>> handleSentinelParamFlowException(SentinelParamFlowException ex) {
        log.warn("Custom Sentinel param flow exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "SENTINEL_PARAM_FLOW");
        response.put("message", ex.getMessage());
        response.put("resource", ex.getResource());
        response.put("paramName", ex.getParamName());
        response.put("paramValue", ex.getParamValue());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * 创建统一的错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, SentinelBlockedExceptionDto dto) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", dto.getErrorCode());
        response.put("message", dto.getUserMessage());
        response.put("data", dto);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(response);
    }
}
