package com.example.demo;

import com.example.demo.dto.AsyncTaskDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class AsyncIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEndToEndAsyncProcessing() throws Exception {
        // Test CompletableFuture endpoint
        MvcResult mvcResult = mockMvc.perform(get("/async/completable-future/integration-test?delaySeconds=1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.taskId").exists())
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.completionTime").exists());
    }

    @Test
    void testDeferredResultIntegration() throws Exception {
        // Test DeferredResult endpoint
        String requestBody = """
                {
                    "taskName": "integration-deferred-test",
                    "delaySeconds": 1,
                    "shouldFail": false
                }
                """;

        MvcResult mvcResult = mockMvc.perform(post("/async/deferred-result")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.taskId").exists());
    }

    @Test
    void testCallableIntegration() throws Exception {
        // Test Callable endpoint
        MvcResult mvcResult = mockMvc.perform(get("/async/callable/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Callable task completed after 1 seconds")));
    }

    @Test
    void testConcurrentProcessingIntegration() throws Exception {
        // Test concurrent processing
        MvcResult mvcResult = mockMvc.perform(get("/async/concurrent-test?taskCount=2&delaySeconds=1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    void testValidationErrorHandling() throws Exception {
        // Test validation error
        String invalidRequestBody = """
                {
                    "taskName": "",
                    "delaySeconds": 35,
                    "shouldFail": false
                }
                """;

        mockMvc.perform(post("/async/deferred-result")
                        .contentType("application/json")
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errorMessage").exists());
    }
}