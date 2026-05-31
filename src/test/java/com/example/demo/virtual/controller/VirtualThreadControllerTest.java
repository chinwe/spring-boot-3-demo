package com.example.demo.virtual.controller;

import com.example.demo.virtual.dto.*;
import com.example.demo.virtual.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 虚拟线程控制器测试
 */
@WebMvcTest(VirtualThreadController.class)
class VirtualThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VirtualThreadService virtualThreadService;

    @MockitoBean
    private PinDetectionService pinDetectionService;

    @MockitoBean
    private ScopeValueService scopeValueService;

    @MockitoBean
    private StructuredConcurrencyService structuredConcurrencyService;

    @MockitoBean
    private VirtualThreadMetricsService virtualThreadMetricsService;

    private VirtualThreadTaskDto mockTaskDto;
    private PinDetectionReport mockPinReport;
    private StructuredConcurrencyResult mockStructuredResult;

    @BeforeEach
    void setUp() {
        // 准备 Mock 数据
        mockTaskDto = VirtualThreadTaskDto.builder()
                .taskId("test-task-1")
                .taskName("TestTask")
                .status(VirtualThreadTaskDto.TaskStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .durationMillis(100L)
                .threadName("test-virtual-thread")
                .isVirtualThread(true)
                .result("Task completed")
                .build();

        mockPinReport = PinDetectionReport.builder()
                .reportId("pin-report-1")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .durationMillis(1000L)
                .pinEvents(List.of())
                .totalPinEvents(0)
                .build();

        mockStructuredResult = StructuredConcurrencyResult.builder()
                .resultId("structured-result-1")
                .executionTime(LocalDateTime.now())
                .totalDurationMillis(500L)
                .strategy(StructuredConcurrencyResult.ConcurrencyStrategy.JOIN_ALL)
                .taskResults(List.of())
                .success(true)
                .build();
    }

    @Test
    void testExecuteBasicTask() throws Exception {
        when(virtualThreadService.executeBasicTask(anyString(), anyInt()))
                .thenReturn(mockTaskDto);

        mockMvc.perform(get("/api/virtual/basic-task")
                        .param("taskName", "TestTask")
                        .param("delayMillis", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("test-task-1"))
                .andExpect(jsonPath("$.taskName").value("TestTask"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.isVirtualThread").value(true));
    }

    @Test
    void testExecuteBatchTasks() throws Exception {
        List<VirtualThreadTaskDto> tasks = List.of(mockTaskDto);
        when(virtualThreadService.executeBatchTasks(anyInt(), anyInt()))
                .thenReturn(tasks);

        mockMvc.perform(get("/api/virtual/batch-tasks")
                        .param("taskCount", "10")
                        .param("delayMillis", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value("test-task-1"));
    }

    @Test
    void testDetectPinnedThreads() throws Exception {
        when(pinDetectionService.detectPinnedThreads())
                .thenReturn(mockPinReport);

        mockMvc.perform(get("/api/virtual/pin-detection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("pin-report-1"))
                .andExpect(jsonPath("$.totalPinEvents").value(0));
    }

    @Test
    void testDemonstrateScopedValue() throws Exception {
        when(scopeValueService.demonstrateScopedValue())
                .thenReturn("UserId: user-12345, TenantId: tenant-abc, RequestId: req-xyz-999");

        mockMvc.perform(get("/api/virtual/scoped-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    void testCompareThreadLocalVsScopedValue() throws Exception {
        when(scopeValueService.compareThreadLocalVsScopedValue())
                .thenReturn("Comparison result");

        mockMvc.perform(get("/api/virtual/scoped-value-comparison"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparison").exists());
    }

    @Test
    void testShutdownOnSuccess() throws Exception {
        when(structuredConcurrencyService.executeShutdownOnSuccess())
                .thenReturn(mockStructuredResult);

        mockMvc.perform(get("/api/virtual/structured-concurrency/shutdown-on-success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultId").value("structured-result-1"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testErrorHandling() throws Exception {
        StructuredConcurrencyResult errorResult = StructuredConcurrencyResult.builder()
                .resultId("error-result")
                .executionTime(LocalDateTime.now())
                .success(false)
                .errorMessage("Task execution failed")
                .build();

        when(structuredConcurrencyService.demonstrateErrorHandling())
                .thenReturn(errorResult);

        mockMvc.perform(get("/api/virtual/structured-concurrency/error-handling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testPerformanceComparison() throws Exception {
        PerformanceComparisonReport report = PerformanceComparisonReport.builder()
                .reportId("perf-report-1")
                .testTime(LocalDateTime.now())
                .taskCount(1000)
                .improvementPercentage(25.5)
                .build();

        when(virtualThreadMetricsService.comparePerformance(anyInt(), anyInt()))
                .thenReturn(report);

        mockMvc.perform(get("/api/virtual/performance-comparison")
                        .param("taskCount", "1000")
                        .param("delayMillis", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("perf-report-1"))
                .andExpect(jsonPath("$.taskCount").value(1000))
                .andExpect(jsonPath("$.improvementPercentage").value(25.5));
    }

    @Test
    void testSimulatePinning() throws Exception {
        when(virtualThreadService.simulatePinning(anyString(), anyInt()))
                .thenReturn(mockTaskDto);

        mockMvc.perform(get("/api/virtual/simulate-pinning")
                        .param("taskName", "PinTask")
                        .param("delayMillis", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("test-task-1"));
    }

    @Test
    void testPinTest_Synchronized() throws Exception {
        // Given - synchronized pin 类型
        mockMvc.perform(post("/api/virtual/pin-test")
                        .param("pinType", "SYNCHRONIZED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinType").value("SYNCHRONIZED"))
                .andExpect(jsonPath("$.message").exists());

        org.mockito.Mockito.verify(pinDetectionService).testSynchronizedPin(anyInt(), anyInt());
    }

    @Test
    void testPinTest_Native() throws Exception {
        // Given
        PinDetectionReport.PinEvent event = PinDetectionReport.PinEvent.builder()
                .pinType(PinDetectionReport.PinType.NATIVE_METHOD)
                .description("Native method pin event")
                .build();
        when(pinDetectionService.testNativeCodePin()).thenReturn(event);

        // When & Then
        mockMvc.perform(post("/api/virtual/pin-test")
                        .param("pinType", "NATIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinType").value("NATIVE"))
                .andExpect(jsonPath("$.event").exists());
    }

    @Test
    void testPinTest_FileIO() throws Exception {
        // Given
        when(pinDetectionService.testFileIOPin()).thenReturn(java.util.List.of());

        // When & Then
        mockMvc.perform(post("/api/virtual/pin-test")
                        .param("pinType", "FILE_IO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinType").value("FILE_IO"))
                .andExpect(jsonPath("$.events").exists());
    }

    @Test
    void testPinTest_UnknownType() throws Exception {
        mockMvc.perform(post("/api/virtual/pin-test")
                        .param("pinType", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.availableTypes").isArray());
    }

    @Test
    void testStructuredConcurrency_JoinAll() throws Exception {
        when(structuredConcurrencyService.executeBasicStructuredTasks())
                .thenReturn(mockStructuredResult);

        mockMvc.perform(post("/api/virtual/structured-concurrency")
                        .param("strategy", "JOIN_ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultId").value("structured-result-1"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testStructuredConcurrency_ShutdownOnSuccess() throws Exception {
        when(structuredConcurrencyService.executeShutdownOnSuccess())
                .thenReturn(mockStructuredResult);

        mockMvc.perform(post("/api/virtual/structured-concurrency")
                        .param("strategy", "SHUTDOWN_ON_SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testStructuredConcurrency_UnknownStrategy() throws Exception {
        mockMvc.perform(post("/api/virtual/structured-concurrency")
                        .param("strategy", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDemoAll() throws Exception {
        // Given
        when(virtualThreadService.executeBasicTask(anyString(), anyInt()))
                .thenReturn(mockTaskDto);
        when(pinDetectionService.detectPinnedThreads())
                .thenReturn(mockPinReport);
        when(scopeValueService.demonstrateScopedValue())
                .thenReturn("ScopedValue demo result");
        when(structuredConcurrencyService.executeBasicStructuredTasks())
                .thenReturn(mockStructuredResult);

        PerformanceComparisonReport perfReport = PerformanceComparisonReport.builder()
                .reportId("perf-1")
                .testTime(LocalDateTime.now())
                .taskCount(100)
                .build();
        when(virtualThreadMetricsService.comparePerformance(anyInt(), anyInt()))
                .thenReturn(perfReport);

        // When & Then
        mockMvc.perform(get("/api/virtual/demo-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("All demos completed successfully"))
                .andExpect(jsonPath("$.basicTask").exists())
                .andExpect(jsonPath("$.pinDetection").exists())
                .andExpect(jsonPath("$.scopedValue").exists())
                .andExpect(jsonPath("$.structuredConcurrency").exists())
                .andExpect(jsonPath("$.performanceComparison").exists());
    }
}
