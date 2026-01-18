package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.hamcrest.Matchers;

import jakarta.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * RetryController 集成测试
 * 验证重试相关的HTTP接口功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "logging.level.com.example.demo=DEBUG"
})
class RetryControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Test
    void testBasicRetryEndpoint_Success() throws Exception {
        mockMvc.perform(get("/retry/basic")
                .param("shouldSucceed", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Basic retry succeeded")));
    }

    @Test
    void testBasicRetryEndpoint_Failure() throws Exception {
        mockMvc.perform(get("/retry/basic")
                .param("shouldSucceed", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Recovery")));
    }

    @Test
    void testLocalRetryEndpoint_Success() throws Exception {
        mockMvc.perform(get("/retry/local")
                .param("shouldSucceed", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Local service call succeeded")));
    }

    @Test
    void testRemoteRetryEndpoint_Success() throws Exception {
        mockMvc.perform(get("/retry/remote")
                .param("shouldSucceed", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Remote service call succeeded")));
    }

    @Test
    void testConditionalRetryEndpoint_TemporaryException() throws Exception {
        mockMvc.perform(get("/retry/conditional")
                .param("exceptionType", "temporary"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testConditionalRetryEndpoint_NetworkException() throws Exception {
        mockMvc.perform(get("/retry/conditional")
                .param("exceptionType", "network"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testConditionalRetryEndpoint_BusinessException() throws Exception {
        mockMvc.perform(get("/retry/conditional")
                .param("exceptionType", "business"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("失败")));
    }

    @Test
    void testImperativeRetryEndpoint_Success() throws Exception {
        mockMvc.perform(get("/retry/imperative")
                .param("shouldSucceed", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Imperative retry succeeded")));
    }

    @Test
    void testSpelRetryEndpoint_NormalPriority() throws Exception {
        mockMvc.perform(get("/retry/spel")
                .param("shouldSucceed", "true")
                .param("priority", "normal"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("SpEL")));
    }

    @Test
    void testSpelRetryEndpoint_CriticalPriority() throws Exception {
        mockMvc.perform(get("/retry/spel")
                .param("shouldSucceed", "true")
                .param("priority", "critical"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("SpEL")));
    }

    @Test
    void testResetCountersEndpoint() throws Exception {
        mockMvc.perform(post("/retry/reset"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("重置")));
    }

    @Test
    void testAllExamplesEndpoint() throws Exception {
        mockMvc.perform(get("/retry/all-examples"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Spring Retry 示例结果")));
    }

    @Test
    void testAllEndpointsWithDefaultParameters() throws Exception {
        // 测试所有接口的默认参数访问
        mockMvc.perform(get("/retry/basic")).andExpect(status().isOk());
        mockMvc.perform(get("/retry/local")).andExpect(status().isOk());
        mockMvc.perform(get("/retry/remote")).andExpect(status().isOk());
        mockMvc.perform(get("/retry/conditional")).andExpect(status().isOk());
        mockMvc.perform(get("/retry/imperative")).andExpect(status().isOk());
        mockMvc.perform(get("/retry/spel")).andExpect(status().isOk());
        mockMvc.perform(post("/retry/reset")).andExpect(status().isOk());
        mockMvc.perform(get("/retry/all-examples")).andExpect(status().isOk());
    }
}