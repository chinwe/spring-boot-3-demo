package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.StructuredConcurrencyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 结构化并发服务
 * 使用 CompletableFuture 和虚拟线程演示结构化并发模式
 * 注意：在 JDK 25 中，StructuredTaskScope 可能仍处于预览状态或有不同的 API
 */
@Service
@Slf4j
public class StructuredConcurrencyService {

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 执行基础结构化并发任务
     * 模拟等待所有子任务完成
     */
    public StructuredConcurrencyResult executeBasicStructuredTasks() {
        String resultId = UUID.randomUUID().toString();
        LocalDateTime executionTime = LocalDateTime.now();

        log.info("=== Executing basic structured tasks ===");

        StructuredConcurrencyResult result = StructuredConcurrencyResult.builder()
                .resultId(resultId)
                .executionTime(executionTime)
                .strategy(StructuredConcurrencyResult.ConcurrencyStrategy.JOIN_ALL)
                .taskResults(new ArrayList<>())
                .success(false)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            // 使用 CompletableFuture 模拟结构化并发
            CompletableFuture<StructuredConcurrencyResult.TaskResult> task1 = CompletableFuture.supplyAsync(() -> {
                log.info("Task 1 started in thread: {}", Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                    log.info("Task 1 completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 1")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Task 1 result")
                            .durationMillis(500L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 1")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage("Interrupted")
                            .build();
                }
            }, virtualThreadExecutor);

            CompletableFuture<StructuredConcurrencyResult.TaskResult> task2 = CompletableFuture.supplyAsync(() -> {
                log.info("Task 2 started in thread: {}", Thread.currentThread().getName());
                try {
                    Thread.sleep(300);
                    log.info("Task 2 completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 2")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Task 2 result")
                            .durationMillis(300L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 2")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage("Interrupted")
                            .build();
                }
            }, virtualThreadExecutor);

            CompletableFuture<StructuredConcurrencyResult.TaskResult> task3 = CompletableFuture.supplyAsync(() -> {
                log.info("Task 3 started in thread: {}", Thread.currentThread().getName());
                try {
                    Thread.sleep(200);
                    log.info("Task 3 completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 3")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Task 3 result")
                            .durationMillis(200L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 3")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage("Interrupted")
                            .build();
                }
            }, virtualThreadExecutor);

            // 等待所有任务完成
            CompletableFuture.allOf(task1, task2, task3).join();

            // 收集结果
            result.getTaskResults().add(task1.join());
            result.getTaskResults().add(task2.join());
            result.getTaskResults().add(task3.join());

            result.setSuccess(true);
            log.info("All tasks completed successfully");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Structured tasks failed: " + e.getMessage());
            log.error("Structured tasks failed", e);
        }

        long endTime = System.currentTimeMillis();
        result.setTotalDurationMillis(endTime - startTime);

        return result;
    }

    /**
     * 演示第一个成功即返回模式
     */
    public StructuredConcurrencyResult executeShutdownOnSuccess() {
        String resultId = UUID.randomUUID().toString();
        LocalDateTime executionTime = LocalDateTime.now();

        log.info("=== Executing first-success tasks ===");

        StructuredConcurrencyResult result = StructuredConcurrencyResult.builder()
                .resultId(resultId)
                .executionTime(executionTime)
                .strategy(StructuredConcurrencyResult.ConcurrencyStrategy.SHUTDOWN_ON_SUCCESS)
                .taskResults(new ArrayList<>())
                .success(false)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            CompletableFuture<StructuredConcurrencyResult.TaskResult> fastTask = CompletableFuture.supplyAsync(() -> {
                log.info("Fast task started");
                try {
                    Thread.sleep(100);
                    log.info("Fast task completed!");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Fast Task")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Fast task won!")
                            .durationMillis(100L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Fast Task")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage("Interrupted")
                            .build();
                }
            }, virtualThreadExecutor);

            CompletableFuture<StructuredConcurrencyResult.TaskResult> slowTask = CompletableFuture.supplyAsync(() -> {
                log.info("Slow task started");
                try {
                    Thread.sleep(1000);
                    log.info("Slow task completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Slow Task")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Slow task result")
                            .durationMillis(1000L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Slow Task")
                            .status(StructuredConcurrencyResult.TaskStatus.CANCELLED)
                            .errorMessage("Cancelled")
                            .build();
                }
            }, virtualThreadExecutor);

            CompletableFuture<StructuredConcurrencyResult.TaskResult> mediumTask = CompletableFuture.supplyAsync(() -> {
                log.info("Medium task started");
                try {
                    Thread.sleep(500);
                    log.info("Medium task completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Medium Task")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Medium task result")
                            .durationMillis(500L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Medium Task")
                            .status(StructuredConcurrencyResult.TaskStatus.CANCELLED)
                            .errorMessage("Cancelled")
                            .build();
                }
            }, virtualThreadExecutor);

            // 等待任意一个完成
            CompletableFuture.anyOf(fastTask, slowTask, mediumTask).join();

            // 获取第一个完成的结果
            String winner = null;
            if (fastTask.isDone()) {
                winner = "Fast task";
            } else if (mediumTask.isDone()) {
                winner = "Medium task";
            } else if (slowTask.isDone()) {
                winner = "Slow task";
            }

            log.info("First completed: {}", winner);

            result.setSuccess(true);
            if (fastTask.isDone()) result.getTaskResults().add(fastTask.join());
            if (slowTask.isDone()) result.getTaskResults().add(slowTask.join());
            if (mediumTask.isDone()) result.getTaskResults().add(mediumTask.join());

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Tasks failed: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        result.setTotalDurationMillis(endTime - startTime);

        return result;
    }

    /**
     * 演示错误处理
     */
    public StructuredConcurrencyResult demonstrateErrorHandling() {
        String resultId = UUID.randomUUID().toString();
        LocalDateTime executionTime = LocalDateTime.now();

        log.info("=== Demonstrating error handling ===");

        StructuredConcurrencyResult result = StructuredConcurrencyResult.builder()
                .resultId(resultId)
                .executionTime(executionTime)
                .strategy(StructuredConcurrencyResult.ConcurrencyStrategy.SHUTDOWN_ON_FAILURE)
                .taskResults(new ArrayList<>())
                .success(false)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            CompletableFuture<StructuredConcurrencyResult.TaskResult> task1 = CompletableFuture.supplyAsync(() -> {
                log.info("Task 1 started");
                try {
                    Thread.sleep(200);
                    log.info("Task 1 completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 1")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Task 1 success")
                            .durationMillis(200L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 1")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage("Interrupted")
                            .build();
                }
            }, virtualThreadExecutor);

            CompletableFuture<StructuredConcurrencyResult.TaskResult> task2 = CompletableFuture.supplyAsync(() -> {
                log.info("Task 2 started");
                try {
                    Thread.sleep(100);
                    log.error("Task 2 throwing exception");
                    throw new RuntimeException("Task 2 failed intentionally!");
                } catch (Exception e) {
                    log.error("Task 2 failed", e);
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 2")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage(e.getMessage())
                            .durationMillis(100L)
                            .build();
                }
            }, virtualThreadExecutor);

            CompletableFuture<StructuredConcurrencyResult.TaskResult> task3 = CompletableFuture.supplyAsync(() -> {
                log.info("Task 3 started");
                try {
                    Thread.sleep(300);
                    log.info("Task 3 completed");
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 3")
                            .status(StructuredConcurrencyResult.TaskStatus.SUCCESS)
                            .result("Task 3 success")
                            .durationMillis(300L)
                            .build();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return StructuredConcurrencyResult.TaskResult.builder()
                            .taskName("Task 3")
                            .status(StructuredConcurrencyResult.TaskStatus.FAILED)
                            .errorMessage("Interrupted")
                            .build();
                }
            }, virtualThreadExecutor);

            // 等待所有任务完成
            CompletableFuture.allOf(task1, task2, task3).join();

            // 收集结果并检查失败
            List<StructuredConcurrencyResult.TaskResult> results = List.of(
                    task1.join(), task2.join(), task3.join()
            );

            boolean hasFailure = results.stream()
                    .anyMatch(r -> r.getStatus() == StructuredConcurrencyResult.TaskStatus.FAILED);

            result.getTaskResults().addAll(results);

            if (hasFailure) {
                result.setErrorMessage("Some tasks failed. Check individual task results.");
            } else {
                result.setSuccess(true);
            }

        } catch (Exception e) {
            result.setErrorMessage("Tasks failed: " + e.getMessage());
            log.error("Tasks failed", e);
        }

        long endTime = System.currentTimeMillis();
        result.setTotalDurationMillis(endTime - startTime);

        return result;
    }
}
