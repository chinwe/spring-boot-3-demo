package com.example.demo.virtual.service;

import com.example.demo.virtual.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * ScopeValue 演示服务
 * 展示 ScopedValue 的用法和与 ThreadLocal 的对比
 */
@Service
@Slf4j
public class ScopeValueService {

    private final ThreadLocal<String> threadLocalUserId = new ThreadLocal<>();
    private final ThreadLocal<String> threadLocalTenantId = new ThreadLocal<>();
    private final ThreadLocal<String> threadLocalRequestId = new ThreadLocal<>();

    /**
     * 基础 ScopedValue 演示
     */
    public String demonstrateScopedValue() {
        log.info("=== Demonstrating ScopedValue ===");

        // 在 ScopedValue 上下文中执行任务
        try {
            String result = ScopedValue.where(UserContext.USER_ID, "user-12345")
                    .where(UserContext.TENANT_ID, "tenant-abc")
                    .where(UserContext.REQUEST_ID, "req-xyz-999")
                    .call(() -> {
                        // 在主线程中访问
                        log.info("Main thread - USER_ID: {}", UserContext.USER_ID.get());
                        log.info("Main thread - TENANT_ID: {}", UserContext.TENANT_ID.get());
                        log.info("Main thread - REQUEST_ID: {}", UserContext.REQUEST_ID.get());

                        return String.format("UserId: %s, TenantId: %s, RequestId: %s",
                                UserContext.USER_ID.get(),
                                UserContext.TENANT_ID.get(),
                                UserContext.REQUEST_ID.get());
                    });

            log.info("ScopedValue demo result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("ScopedValue demo failed", e);
            throw new RuntimeException("Failed to demonstrate ScopedValue", e);
        }
    }

    /**
     * 在子线程中访问 ScopedValue
     * ScopedValue 会自动传递给结构化并发的子线程
     */
    public List<String> executeChildTasks() throws Exception {
        log.info("=== Demonstrating ScopedValue with child tasks ===");

        List<String> results = new ArrayList<>();

        // 使用 ScopedValue 设置上下文
        String result = ScopedValue.where(UserContext.USER_ID, "user-67890")
                .where(UserContext.TENANT_ID, "tenant-def")
                .where(UserContext.REQUEST_ID, "req-uvw-888")
                .call(() -> {
                    log.info("Parent task - USER_ID: {}", UserContext.USER_ID.get());

                    // 在虚拟线程中执行子任务
                    // 注意：普通的 CompletableFuture 不会自动继承 ScopedValue
                    // 需要使用 StructuredTaskScope 才能自动继承

                    List<String> childResults = new ArrayList<>();

                    // 创建多个子任务
                    for (int i = 0; i < 3; i++) {
                        final int taskIndex = i;
                        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                            // 注意：这里使用普通的 CompletableFuture，ScopedValue 不会自动传递
                            // 在实际应用中，应该使用 StructuredTaskScope
                            log.info("Child task {} - USER_ID (may be null): {}", taskIndex, UserContext.USER_ID.get());

                            // 由于没有使用 StructuredTaskScope，这里可能获取不到值
                            String userId = UserContext.USER_ID.get();
                            if (userId != null) {
                                return String.format("Child task %d - UserId: %s", taskIndex, userId);
                            } else {
                                return String.format("Child task %d - No ScopedValue inherited (need StructuredTaskScope)", taskIndex);
                            }
                        }, Executors.newVirtualThreadPerTaskExecutor());

                        childResults.add(future.get());
                    }

                    return String.join(" | ", childResults);
                });

        results.add(result);
        return results;
    }

    /**
     * 对比 ThreadLocal 与 ScopedValue
     */
    public String compareThreadLocalVsScopedValue() {
        log.info("=== Comparing ThreadLocal vs ScopedValue ===");

        StringBuilder comparison = new StringBuilder();
        comparison.append("ThreadLocal vs ScopedValue Comparison:\n\n");

        // 测试 ThreadLocal
        long threadLocalStart = System.nanoTime();
        String threadLocalResult = testThreadLocal();
        long threadLocalDuration = (System.nanoTime() - threadLocalStart) / 1_000_000;

        comparison.append("1. ThreadLocal:\n");
        comparison.append("   Result: ").append(threadLocalResult).append("\n");
        comparison.append("   Duration: ").append(threadLocalDuration).append(" ms\n\n");

        // 测试 ScopedValue
        long scopedValueStart = System.nanoTime();
        String scopedValueResult = testScopedValue();
        long scopedValueDuration = (System.nanoTime() - scopedValueStart) / 1_000_000;

        comparison.append("2. ScopedValue:\n");
        comparison.append("   Result: ").append(scopedValueResult).append("\n");
        comparison.append("   Duration: ").append(scopedValueDuration).append(" ms\n\n");

        comparison.append("Key Differences:\n");
        comparison.append("- ThreadLocal: Mutable, requires explicit cleanup, can cause memory leaks\n");
        comparison.append("- ScopedValue: Immutable, automatic cleanup, safer for structured concurrency\n");

        log.info(comparison.toString());
        return comparison.toString();
    }

    /**
     * 测试 ThreadLocal
     */
    private String testThreadLocal() {
        try {
            threadLocalUserId.set("user-threadlocal-123");
            threadLocalTenantId.set("tenant-threadlocal-abc");
            threadLocalRequestId.set("req-threadlocal-xyz");

            log.info("ThreadLocal - USER_ID: {}", threadLocalUserId.get());

            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                // ThreadLocal 不会自动传递给子线程
                String userId = threadLocalUserId.get();
                log.info("ThreadLocal in child thread - USER_ID: {}", userId);
                return userId != null ? userId : "null (not inherited)";
            }, Executors.newVirtualThreadPerTaskExecutor());

            String childResult = future.get();

            // 需要手动清理
            threadLocalUserId.remove();
            threadLocalTenantId.remove();
            threadLocalRequestId.remove();

            return "Parent: user-threadlocal-123, Child: " + childResult;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return "ThreadLocal test failed: " + e.getMessage();
        }
    }

    /**
     * 测试 ScopedValue
     */
    private String testScopedValue() {
        try {
            return ScopedValue.where(UserContext.USER_ID, "user-scoped-123")
                    .where(UserContext.TENANT_ID, "tenant-scoped-abc")
                    .call(() -> {
                        log.info("ScopedValue - USER_ID: {}", UserContext.USER_ID.get());

                        // 注意：使用 CompletableFuture 不会自动传递 ScopedValue
                        // 需要使用 StructuredTaskScope
                        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                            String userId = UserContext.USER_ID.get();
                            log.info("ScopedValue in child thread - USER_ID: {}", userId);
                            return userId != null ? userId : "null (use StructuredTaskScope for inheritance)";
                        }, Executors.newVirtualThreadPerTaskExecutor());

                        try {
                            String childResult = future.get();
                            return "Parent: user-scoped-123, Child: " + childResult;
                        } catch (InterruptedException | ExecutionException e) {
                            Thread.currentThread().interrupt();
                            return "ScopedValue test failed";
                        }
                    });
        } catch (Exception e) {
            return "ScopedValue test failed: " + e.getMessage();
        }
    }
}
