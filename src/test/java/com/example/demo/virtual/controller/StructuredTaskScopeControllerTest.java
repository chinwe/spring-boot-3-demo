package com.example.demo.virtual.controller;

import com.example.demo.virtual.dto.OrderAggregationResult;
import com.example.demo.virtual.dto.OrderAggregationResult.*;
import com.example.demo.virtual.dto.PaymentRaceResult;
import com.example.demo.virtual.service.StructuredConcurrencyService;
import com.example.demo.virtual.service.StructuredTaskScopeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StructuredTaskScopeController 单元测试
 */
@WebMvcTest(StructuredTaskScopeController.class)
class StructuredTaskScopeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StructuredTaskScopeService structuredTaskScopeService;

    @MockitoBean
    private StructuredConcurrencyService structuredConcurrencyService;

    @Test
    @DisplayName("POST /api/virtual/sts/shutdown-on-failure — 返回聚合结果")
    void shutdownOnFailure_returnsAggregationResult() throws Exception {
        // Given
        OrderAggregationResult mockResult = OrderAggregationResult.builder()
                .resultId("test-id")
                .orderId(1L)
                .executionTime(LocalDateTime.now())
                .totalDurationMillis(100L)
                .strategy(AggregationStrategy.SHUTDOWN_ON_FAILURE)
                .success(true)
                .subTaskResults(List.of())
                .userInfo(UserInfo.builder().userId(10L).username("testuser").build())
                .build();

        when(structuredTaskScopeService.aggregateOrderDetails(1L, 10L)).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/virtual/sts/shutdown-on-failure")
                        .param("orderId", "1")
                        .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.strategy").value("SHUTDOWN_ON_FAILURE"))
                .andExpect(jsonPath("$.userInfo.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/virtual/sts/shutdown-on-success — 返回竞速结果")
    void shutdownOnSuccess_returnsRaceResult() throws Exception {
        // Given
        PaymentRaceResult mockResult = PaymentRaceResult.builder()
                .resultId("race-id")
                .orderId(1L)
                .executionTime(LocalDateTime.now())
                .totalDurationMillis(50L)
                .winnerGateway("FastGateway")
                .success(true)
                .gatewayResults(List.of())
                .build();

        when(structuredTaskScopeService.racePaymentStatus(1L)).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/virtual/sts/shutdown-on-success")
                        .param("orderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.winnerGateway").value("FastGateway"));
    }

    @Test
    @DisplayName("POST /api/virtual/sts/custom-scope — 返回降级聚合结果")
    void customScope_returnsDegradedResult() throws Exception {
        // Given
        OrderAggregationResult mockResult = OrderAggregationResult.builder()
                .resultId("degrade-id")
                .orderId(1L)
                .executionTime(LocalDateTime.now())
                .totalDurationMillis(100L)
                .strategy(AggregationStrategy.DEGRADED_AGGREGATION)
                .success(true)
                .degraded(true)
                .subTaskResults(List.of())
                .userInfo(UserInfo.builder().userId(10L).username("testuser").build())
                .build();

        when(structuredTaskScopeService.aggregateWithDegradation(1L, 10L)).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/virtual/sts/custom-scope")
                        .param("orderId", "1")
                        .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.degraded").value(true))
                .andExpect(jsonPath("$.strategy").value("DEGRADED_AGGREGATION"));
    }

    @Test
    @DisplayName("GET /api/virtual/sts/demo-all — 综合演示")
    void demoAll_returnsAllDemos() throws Exception {
        // Given
        OrderAggregationResult aggResult = OrderAggregationResult.builder()
                .resultId("agg-id").orderId(1L).success(true)
                .strategy(AggregationStrategy.SHUTDOWN_ON_FAILURE).build();
        PaymentRaceResult raceResult = PaymentRaceResult.builder()
                .resultId("race-id").orderId(1L).success(true).build();
        OrderAggregationResult degradeResult = OrderAggregationResult.builder()
                .resultId("degrade-id").orderId(1L).success(true)
                .strategy(AggregationStrategy.DEGRADED_AGGREGATION).build();

        when(structuredTaskScopeService.aggregateOrderDetails(anyLong(), anyLong()))
                .thenReturn(aggResult);
        when(structuredTaskScopeService.racePaymentStatus(anyLong()))
                .thenReturn(raceResult);
        when(structuredTaskScopeService.aggregateWithDegradation(anyLong(), anyLong()))
                .thenReturn(degradeResult);

        // When & Then
        mockMvc.perform(get("/api/virtual/sts/demo-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("All demos completed successfully"));
    }

    @Test
    @DisplayName("GET /api/virtual/sts/compare — CompletableFuture vs StructuredTaskScope 对比")
    void compare_returnsComparisonInfo() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/virtual/sts/compare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
