package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.VirtualThreadTaskDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 虚拟线程服务测试
 */
class VirtualThreadServiceTest {

    private VirtualThreadService service;

    @BeforeEach
    void setUp() {
        service = new VirtualThreadService();
    }

    @Test
    void testExecuteBasicTask() {
        VirtualThreadTaskDto result = service.executeBasicTask("TestTask", 100);

        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals("TestTask", result.getTaskName());
        assertEquals(VirtualThreadTaskDto.TaskStatus.COMPLETED, result.getStatus());
        assertTrue(result.getIsVirtualThread());
        assertNotNull(result.getThreadName());
    }

    @Test
    void testExecuteBatchTasks() {
        int taskCount = 10;
        List<VirtualThreadTaskDto> results = service.executeBatchTasks(taskCount, 50);

        assertNotNull(results);
        assertEquals(taskCount, results.size());

        // 验证所有任务都成功完成
        for (VirtualThreadTaskDto task : results) {
            if (task != null) {
                assertEquals(VirtualThreadTaskDto.TaskStatus.COMPLETED, task.getStatus());
                assertTrue(task.getIsVirtualThread());
            }
        }
    }

    @Test
    void testSimulatePinning() {
        VirtualThreadTaskDto result = service.simulatePinning("PinTask", 200);

        assertNotNull(result);
        assertNotNull(result.getTaskId());
        assertEquals("PinTask", result.getTaskName());
        assertEquals(VirtualThreadTaskDto.TaskStatus.COMPLETED, result.getStatus());
        assertTrue(result.getIsVirtualThread());
    }

    @Test
    void testTaskWithZeroDelay() {
        VirtualThreadTaskDto result = service.executeBasicTask("ZeroDelayTask", 0);

        assertNotNull(result);
        assertEquals(VirtualThreadTaskDto.TaskStatus.COMPLETED, result.getStatus());
    }

    @Test
    void testLargeBatch() {
        int largeTaskCount = 100;
        List<VirtualThreadTaskDto> results = service.executeBatchTasks(largeTaskCount, 10);

        assertNotNull(results);
        assertEquals(largeTaskCount, results.size());
    }
}
