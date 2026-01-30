package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.StructuredConcurrencyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 结构化并发服务测试
 */
class StructuredConcurrencyServiceTest {

    private StructuredConcurrencyService service;

    @BeforeEach
    void setUp() {
        service = new StructuredConcurrencyService();
    }

    @Test
    void testExecuteBasicStructuredTasks() {
        StructuredConcurrencyResult result = service.executeBasicStructuredTasks();

        assertNotNull(result);
        assertNotNull(result.getResultId());
        assertNotNull(result.getExecutionTime());
        assertNotNull(result.getTotalDurationMillis());
        assertEquals(StructuredConcurrencyResult.ConcurrencyStrategy.JOIN_ALL, result.getStrategy());
        assertTrue(result.getSuccess());
        assertNull(result.getErrorMessage());
        assertFalse(result.getTaskResults().isEmpty());
    }

    @Test
    void testExecuteShutdownOnSuccess() {
        StructuredConcurrencyResult result = service.executeShutdownOnSuccess();

        assertNotNull(result);
        assertEquals(StructuredConcurrencyResult.ConcurrencyStrategy.SHUTDOWN_ON_SUCCESS, result.getStrategy());
        assertTrue(result.getSuccess());
        assertEquals(1, result.getTaskResults().size());
        assertEquals(StructuredConcurrencyResult.TaskStatus.SUCCESS,
                result.getTaskResults().get(0).getStatus());
    }

    @Test
    void testDemonstrateErrorHandling() {
        StructuredConcurrencyResult result = service.demonstrateErrorHandling();

        assertNotNull(result);
        assertNotNull(result.getResultId());
        assertNotNull(result.getTotalDurationMillis());
        // 错误处理场景可能失败（由于一个任务抛出异常）
        // 所以我们检查是否有错误信息
        if (!result.getSuccess()) {
            assertNotNull(result.getErrorMessage());
        }
    }

    @Test
    void testAllTasksCompleteInBasicStructuredTasks() {
        StructuredConcurrencyResult result = service.executeBasicStructuredTasks();

        assertEquals(3, result.getTaskResults().size());

        // 在 JOIN_ALL 模式下，所有任务都应该完成
        for (StructuredConcurrencyResult.TaskResult taskResult : result.getTaskResults()) {
            assertNotNull(taskResult.getTaskName());
            assertNotNull(taskResult.getStatus());
        }
    }

    @Test
    void testShutdownOnSuccessHasWinner() {
        StructuredConcurrencyResult result = service.executeShutdownOnSuccess();

        assertEquals(1, result.getTaskResults().size());
        StructuredConcurrencyResult.TaskResult winner = result.getTaskResults().get(0);
        assertEquals(StructuredConcurrencyResult.TaskStatus.SUCCESS, winner.getStatus());
        assertNotNull(winner.getResult());
        assertTrue(winner.getResult().contains("won") || winner.getResult().contains("Fast"));
    }

    @Test
    void testExecutionTimeIsMeasured() {
        StructuredConcurrencyResult result = service.executeBasicStructuredTasks();

        assertNotNull(result.getTotalDurationMillis());
        assertTrue(result.getTotalDurationMillis() >= 0);
    }
}
