package com.example.demo.controller;

import com.example.demo.dto.AsyncTaskDto;
import com.example.demo.service.AsyncService;
import com.example.demo.service.AsyncMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AsyncController.class)
class AsyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AsyncService asyncService;

    @MockBean
    private AsyncMetricsService asyncMetricsService;

    @Test
    void testCompletableFutureEndpoint() throws Exception {
        // Given
        AsyncTaskDto mockTask = AsyncTaskDto.builder()
                .taskId("test-task-123")
                .status(AsyncTaskDto.TaskStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .completionTime(LocalDateTime.now())
                .result("Test task completed successfully")
                .build();

        when(asyncService.performLongRunningTask(anyString(), anyInt(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(mockTask));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/async/completable-future/test-task"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("test-task-123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result").value("Test task completed successfully"));
    }

    @Test
    void testDeferredResultEndpoint() throws Exception {
        // Given
        AsyncTaskDto mockTask = AsyncTaskDto.builder()
                .taskId("deferred-task-456")
                .status(AsyncTaskDto.TaskStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .completionTime(LocalDateTime.now())
                .result("Deferred task completed successfully")
                .build();

        when(asyncService.performLongRunningTask(anyString(), anyInt(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(mockTask));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(post("/async/deferred-result")
                        .contentType("application/json")
                        .content("{\"taskName\":\"test-deferred\",\"delaySeconds\":3,\"shouldFail\":false}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("deferred-task-456"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result").value("Deferred task completed successfully"));
    }

    @Test
    void testCallableEndpoint() throws Exception {
        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/async/callable/2"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Callable task completed after 2 seconds")));
    }

    @Test
    void testConcurrentRequestsEndpoint() throws Exception {
        // Given
        AsyncTaskDto mockTask1 = AsyncTaskDto.builder()
                .taskId("concurrent-task-1")
                .status(AsyncTaskDto.TaskStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .completionTime(LocalDateTime.now())
                .result("Concurrent task 1 completed")
                .build();

        AsyncTaskDto mockTask2 = AsyncTaskDto.builder()
                .taskId("concurrent-task-2")
                .status(AsyncTaskDto.TaskStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .completionTime(LocalDateTime.now())
                .result("Concurrent task 2 completed")
                .build();

        when(asyncService.performLongRunningTask(eq("concurrent-task-1"), anyInt(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(mockTask1));
        when(asyncService.performLongRunningTask(eq("concurrent-task-2"), anyInt(), anyBoolean()))
                .thenReturn(CompletableFuture.completedFuture(mockTask2));

        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/async/concurrent-test?taskCount=2&delaySeconds=3"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].taskId").value("concurrent-task-1"))
                .andExpect(jsonPath("$[1].taskId").value("concurrent-task-2"));
    }
}