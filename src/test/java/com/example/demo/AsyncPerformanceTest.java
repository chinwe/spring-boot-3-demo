package com.example.demo;

import com.example.demo.service.AsyncMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AsyncPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AsyncMetricsService metricsService;

    @Test
    void testAsyncPerformanceBenefit() throws Exception {
        // Reset metrics before test
        metricsService.resetMetrics();
        
        long startTime = System.currentTimeMillis();
        
        // Execute multiple concurrent async requests
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    MvcResult mvcResult = mockMvc.perform(get("/async/completable-future/perf-test-" + Thread.currentThread().getId() + "?delaySeconds=2"))
                            .andExpect(request().asyncStarted())
                            .andReturn();
                    
                    mockMvc.perform(asyncDispatch(mvcResult))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Verify that concurrent execution is faster than sequential
        // 5 tasks * 2 seconds each = 10 seconds sequential
        // With async, should complete in ~2-3 seconds
        assertTrue(totalTime < 8000, "Async execution should be faster than sequential. Actual time: " + totalTime + "ms");
        
        // Verify metrics
        Map<String, Object> metrics = metricsService.getAsyncMetrics();
        assertEquals(5L, metrics.get("totalTasks"));
        assertEquals(5L, metrics.get("completedTasks"));
        assertEquals(0L, metrics.get("failedTasks"));
        
        System.out.println("Performance test completed in " + totalTime + "ms");
        System.out.println("Metrics: " + metrics);
    }

    @Test
    void testMetricsEndpoint() throws Exception {
        // Test metrics endpoint
        mockMvc.perform(get("/async/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").exists())
                .andExpect(jsonPath("$.completedTasks").exists())
                .andExpect(jsonPath("$.failedTasks").exists())
                .andExpect(jsonPath("$.threadPool").exists());
    }

    @Test
    void testMetricsReset() throws Exception {
        // Test metrics reset
        mockMvc.perform(post("/async/metrics/reset"))
                .andExpect(status().isOk())
                .andExpect(content().string("Async metrics reset successfully"));
    }
}