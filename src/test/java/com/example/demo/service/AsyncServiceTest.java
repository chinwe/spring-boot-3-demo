package com.example.demo.service;

import com.example.demo.dto.AsyncTaskDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.execution.pool.core-size=2",
        "spring.task.execution.pool.max-size=4"
})
class AsyncServiceTest {

    @Autowired
    private AsyncService asyncService;

    @Test
    void testPerformLongRunningTaskSuccess() throws ExecutionException, InterruptedException {
        // Given
        String taskName = "test-task";
        int delaySeconds = 1;
        boolean shouldFail = false;

        // When
        CompletableFuture<AsyncTaskDto> future = asyncService.performLongRunningTask(taskName, delaySeconds, shouldFail);
        AsyncTaskDto result = future.get();

        // Then
        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals(AsyncTaskDto.TaskStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getCompletionTime());
        assertTrue(result.getCompletionTime().isAfter(result.getStartTime()));
        assertTrue(result.getResult().toString().contains(taskName));
        assertNull(result.getErrorMessage());
    }

    @Test
    void testPerformLongRunningTaskFailure() throws ExecutionException, InterruptedException {
        // Given
        String taskName = "failing-task";
        int delaySeconds = 1;
        boolean shouldFail = true;

        // When
        CompletableFuture<AsyncTaskDto> future = asyncService.performLongRunningTask(taskName, delaySeconds, shouldFail);
        AsyncTaskDto result = future.get();

        // Then
        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals(AsyncTaskDto.TaskStatus.FAILED, result.getStatus());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getCompletionTime());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Task configured to fail"));
    }

    @Test
    void testPerformAsyncCalculation() throws ExecutionException, InterruptedException {
        // Given
        int input = 5;

        // When
        CompletableFuture<Integer> future = asyncService.performAsyncCalculation(input);
        Integer result = future.get();

        // Then
        assertNotNull(result);
        // Expected: 5*5 + 5*2 + 1 = 25 + 10 + 1 = 36
        assertEquals(36, result);
    }

    @Test
    void testPerformAsyncDatabaseOperation() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<String> future = asyncService.performAsyncDatabaseOperation();
        String result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Database operation completed"));
    }

    @Test
    void testCreatePendingTask() {
        // Given
        String taskName = "pending-task";

        // When
        AsyncTaskDto result = asyncService.createPendingTask(taskName);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals(AsyncTaskDto.TaskStatus.PENDING, result.getStatus());
        assertNotNull(result.getStartTime());
        assertTrue(result.getResult().toString().contains(taskName));
        assertTrue(result.getResult().toString().contains("pending"));
    }
}