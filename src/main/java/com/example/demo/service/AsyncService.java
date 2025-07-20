package com.example.demo.service;

import com.example.demo.dto.AsyncTaskDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AsyncService {

    @Resource
    private AsyncMetricsService metricsService;

    @Async("asyncTaskExecutor")
    public CompletableFuture<AsyncTaskDto> performLongRunningTask(String taskName, int delaySeconds, boolean shouldFail) {
        String taskId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        log.info("Starting async task: {} with ID: {}", taskName, taskId);
        metricsService.incrementTotalTasks();
        
        AsyncTaskDto task = AsyncTaskDto.builder()
                .taskId(taskId)
                .status(AsyncTaskDto.TaskStatus.PROCESSING)
                .startTime(LocalDateTime.now())
                .build();

        try {
            // Simulate long-running operation
            Thread.sleep(delaySeconds * 1000L);
            
            if (shouldFail) {
                throw new RuntimeException("Task configured to fail: " + taskName);
            }
            
            task.setStatus(AsyncTaskDto.TaskStatus.COMPLETED);
            task.setCompletionTime(LocalDateTime.now());
            task.setResult(String.format("Task '%s' completed successfully after %d seconds", taskName, delaySeconds));
            
            metricsService.incrementCompletedTasks();
            log.info("Completed async task: {} with ID: {}", taskName, taskId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(AsyncTaskDto.TaskStatus.FAILED);
            task.setCompletionTime(LocalDateTime.now());
            task.setErrorMessage("Task was interrupted: " + e.getMessage());
            metricsService.incrementFailedTasks();
            log.error("Async task interrupted: {} with ID: {}", taskName, taskId, e);
        } catch (Exception e) {
            task.setStatus(AsyncTaskDto.TaskStatus.FAILED);
            task.setCompletionTime(LocalDateTime.now());
            task.setErrorMessage("Task failed: " + e.getMessage());
            metricsService.incrementFailedTasks();
            log.error("Async task failed: {} with ID: {}", taskName, taskId, e);
        } finally {
            long endTime = System.currentTimeMillis();
            metricsService.logPerformanceMetrics("performLongRunningTask-" + taskName, startTime, endTime);
        }

        return CompletableFuture.completedFuture(task);
    }

    @Async("asyncTaskExecutor")
    public CompletableFuture<Integer> performAsyncCalculation(int input) {
        String taskId = UUID.randomUUID().toString();
        log.info("Starting async calculation with input: {} and ID: {}", input, taskId);
        
        try {
            // Simulate computational work
            Thread.sleep(2000);
            
            int result = input * input + input * 2 + 1;
            log.info("Completed async calculation: {} -> {} with ID: {}", input, result, taskId);
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async calculation interrupted with ID: {}", taskId, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Async calculation failed with ID: {}", taskId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("asyncTaskExecutor")
    public CompletableFuture<String> performAsyncDatabaseOperation() {
        String taskId = UUID.randomUUID().toString();
        log.info("Starting async database operation with ID: {}", taskId);
        
        try {
            // Simulate database operation
            Thread.sleep(3000);
            
            String result = "Database operation completed at " + LocalDateTime.now();
            log.info("Completed async database operation with ID: {}", taskId);
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async database operation interrupted with ID: {}", taskId, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Async database operation failed with ID: {}", taskId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public AsyncTaskDto createPendingTask(String taskName) {
        return AsyncTaskDto.builder()
                .taskId(UUID.randomUUID().toString())
                .status(AsyncTaskDto.TaskStatus.PENDING)
                .startTime(LocalDateTime.now())
                .result("Task '" + taskName + "' is pending execution")
                .build();
    }
}