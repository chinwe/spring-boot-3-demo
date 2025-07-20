package com.example.demo.controller;

import com.example.demo.dto.AsyncTaskDto;
import com.example.demo.service.AsyncService;
import com.example.demo.service.AsyncMetricsService;
import com.example.demo.vo.AsyncTaskVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/async")
public class AsyncController {

    @Resource
    private AsyncService asyncService;

    @Resource
    private AsyncMetricsService metricsService;

    @GetMapping("/completable-future/{taskName}")
    public CompletableFuture<AsyncTaskDto> completableFutureExample(
            @PathVariable String taskName,
            @RequestParam(defaultValue = "5") int delaySeconds,
            @RequestParam(defaultValue = "false") boolean shouldFail) {
        
        log.info("Received CompletableFuture request for task: {} with delay: {}s", taskName, delaySeconds);
        
        return asyncService.performLongRunningTask(taskName, delaySeconds, shouldFail)
                .exceptionally(throwable -> {
                    log.error("CompletableFuture task failed: {}", taskName, throwable);
                    return AsyncTaskDto.builder()
                            .taskId("error-" + System.currentTimeMillis())
                            .status(AsyncTaskDto.TaskStatus.FAILED)
                            .startTime(LocalDateTime.now())
                            .completionTime(LocalDateTime.now())
                            .errorMessage("CompletableFuture execution failed: " + throwable.getMessage())
                            .build();
                });
    }

    @PostMapping("/deferred-result")
    public DeferredResult<AsyncTaskDto> deferredResultExample(@Valid @RequestBody AsyncTaskVo asyncTaskVo) {
        log.info("Received DeferredResult request for task: {} with delay: {}s", 
                asyncTaskVo.getTaskName(), asyncTaskVo.getDelaySeconds());
        
        DeferredResult<AsyncTaskDto> deferredResult = new DeferredResult<>(30000L); // 30 second timeout
        
        // Set timeout callback
        deferredResult.onTimeout(() -> {
            log.warn("DeferredResult timeout for task: {}", asyncTaskVo.getTaskName());
            AsyncTaskDto timeoutTask = AsyncTaskDto.builder()
                    .taskId("timeout-" + System.currentTimeMillis())
                    .status(AsyncTaskDto.TaskStatus.FAILED)
                    .startTime(LocalDateTime.now())
                    .completionTime(LocalDateTime.now())
                    .errorMessage("Request timeout after 30 seconds")
                    .build();
            deferredResult.setResult(timeoutTask);
        });
        
        // Set error callback
        deferredResult.onError(throwable -> {
            log.error("DeferredResult error for task: {}", asyncTaskVo.getTaskName(), throwable);
            AsyncTaskDto errorTask = AsyncTaskDto.builder()
                    .taskId("error-" + System.currentTimeMillis())
                    .status(AsyncTaskDto.TaskStatus.FAILED)
                    .startTime(LocalDateTime.now())
                    .completionTime(LocalDateTime.now())
                    .errorMessage("DeferredResult error: " + throwable.getMessage())
                    .build();
            deferredResult.setErrorResult(errorTask);
        });
        
        // Execute async task
        asyncService.performLongRunningTask(
                asyncTaskVo.getTaskName(), 
                asyncTaskVo.getDelaySeconds(), 
                asyncTaskVo.getShouldFail())
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Async task failed in DeferredResult: {}", asyncTaskVo.getTaskName(), throwable);
                        AsyncTaskDto failedTask = AsyncTaskDto.builder()
                                .taskId("failed-" + System.currentTimeMillis())
                                .status(AsyncTaskDto.TaskStatus.FAILED)
                                .startTime(LocalDateTime.now())
                                .completionTime(LocalDateTime.now())
                                .errorMessage("Task execution failed: " + throwable.getMessage())
                                .build();
                        deferredResult.setResult(failedTask);
                    } else {
                        log.info("DeferredResult task completed: {}", asyncTaskVo.getTaskName());
                        deferredResult.setResult(result);
                    }
                });
        
        return deferredResult;
    }

    @GetMapping("/callable/{delaySeconds}")
    public Callable<String> callableExample(@PathVariable int delaySeconds) {
        log.info("Received Callable request with delay: {}s", delaySeconds);
        
        return () -> {
            try {
                log.info("Starting Callable task execution with delay: {}s", delaySeconds);
                
                // Simulate long-running operation
                Thread.sleep(delaySeconds * 1000L);
                
                String result = String.format("Callable task completed after %d seconds at %s", 
                        delaySeconds, LocalDateTime.now());
                
                log.info("Completed Callable task with delay: {}s", delaySeconds);
                return result;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Callable task interrupted with delay: {}s", delaySeconds, e);
                throw new RuntimeException("Callable task was interrupted", e);
            } catch (Exception e) {
                log.error("Callable task failed with delay: {}s", delaySeconds, e);
                throw new RuntimeException("Callable task execution failed: " + e.getMessage(), e);
            }
        };
    }

    @GetMapping("/concurrent-test")
    public CompletableFuture<List<AsyncTaskDto>> concurrentRequestsExample(
            @RequestParam(defaultValue = "3") int taskCount,
            @RequestParam(defaultValue = "5") int delaySeconds) {
        
        log.info("Received concurrent test request for {} tasks with {}s delay each", taskCount, delaySeconds);
        
        // Create multiple concurrent tasks
        List<CompletableFuture<AsyncTaskDto>> futures = new java.util.ArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            String taskName = "concurrent-task-" + (i + 1);
            CompletableFuture<AsyncTaskDto> future = asyncService.performLongRunningTask(taskName, delaySeconds, false)
                    .exceptionally(throwable -> {
                        log.error("Concurrent task failed: {}", taskName, throwable);
                        return AsyncTaskDto.builder()
                                .taskId("error-" + System.currentTimeMillis())
                                .status(AsyncTaskDto.TaskStatus.FAILED)
                                .startTime(LocalDateTime.now())
                                .completionTime(LocalDateTime.now())
                                .errorMessage("Concurrent task failed: " + throwable.getMessage())
                                .build();
                    });
            futures.add(future);
        }
        
        // Combine all futures into a single result
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<AsyncTaskDto> results = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    
                    log.info("Completed concurrent test with {} tasks", results.size());
                    return results;
                })
                .exceptionally(throwable -> {
                    log.error("Concurrent test failed", throwable);
                    return List.of(AsyncTaskDto.builder()
                            .taskId("concurrent-error-" + System.currentTimeMillis())
                            .status(AsyncTaskDto.TaskStatus.FAILED)
                            .startTime(LocalDateTime.now())
                            .completionTime(LocalDateTime.now())
                            .errorMessage("Concurrent test execution failed: " + throwable.getMessage())
                            .build());
                });
    }

    @GetMapping("/metrics")
    public Map<String, Object> getAsyncMetrics() {
        log.info("Retrieving async metrics");
        return metricsService.getAsyncMetrics();
    }

    @PostMapping("/metrics/reset")
    public String resetMetrics() {
        log.info("Resetting async metrics");
        metricsService.resetMetrics();
        return "Async metrics reset successfully";
    }
}