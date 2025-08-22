package com.example.demo.listener;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定义重试监听器
 * 用于监控和记录重试操作的详细信息
 */
@Slf4j
@Component
public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        String operationName = getOperationName(callback);
        log.info("🚀 Starting retry operation: {}", operationName);
        
        // 在上下文中记录开始时间
        context.setAttribute("start_time", System.currentTimeMillis());
        context.setAttribute("operation_name", operationName);
        
        return true; // 返回true继续重试，false则停止
    }

    @Override
    public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
        String operationName = (String) context.getAttribute("operation_name");
        long startTime = (Long) context.getAttribute("start_time");
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("✅ Retry operation succeeded: {} | Retry count: {} | Total duration: {}ms", 
                operationName, context.getRetryCount(), duration);
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        String operationName = (String) context.getAttribute("operation_name");
        
        log.warn("❌ Retry operation failed: {} | Attempt {} | Exception: {} | Message: {}", 
                operationName, context.getRetryCount() + 1, 
                throwable.getClass().getSimpleName(), throwable.getMessage());
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        String operationName = (String) context.getAttribute("operation_name");
        long startTime = (Long) context.getAttribute("start_time");
        long totalDuration = System.currentTimeMillis() - startTime;
        
        if (throwable != null) {
            log.error("🔄 Retry operation finally failed: {} | Total retries: {} | Total duration: {}ms | Final exception: {}", 
                    operationName, context.getRetryCount(), totalDuration, throwable.getMessage());
        } else {
            log.info("🎯 Retry operation completed: {} | Total retries: {} | Total duration: {}ms", 
                    operationName, context.getRetryCount(), totalDuration);
        }
        
        // 清理上下文
        context.removeAttribute("start_time");
        context.removeAttribute("operation_name");
    }

    /**
     * 尝试从回调中提取操作名称
     */
    private <T, E extends Throwable> String getOperationName(RetryCallback<T, E> callback) {
        if (callback != null) {
            String className = callback.getClass().getSimpleName();
            if (className.contains("$")) {
                // 处理Lambda表达式的情况
                return "Lambda Operation";
            }
            return className;
        }
        return "Unknown Operation";
    }
}