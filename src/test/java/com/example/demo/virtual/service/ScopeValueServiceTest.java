package com.example.demo.virtual.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ScopeValue 服务测试
 */
class ScopeValueServiceTest {

    private ScopeValueService service;

    @BeforeEach
    void setUp() {
        service = new ScopeValueService();
    }

    @Test
    void testDemonstrateScopedValue() {
        String result = service.demonstrateScopedValue();

        assertNotNull(result);
        assertTrue(result.contains("UserId:"));
        assertTrue(result.contains("TenantId:"));
        assertTrue(result.contains("RequestId:"));
    }

    @Test
    void testExecuteChildTasks() throws Exception {
        List<String> results = service.executeChildTasks();

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testCompareThreadLocalVsScopedValue() {
        String result = service.compareThreadLocalVsScopedValue();

        assertNotNull(result);
        assertTrue(result.contains("ThreadLocal"));
        assertTrue(result.contains("ScopedValue"));
        assertTrue(result.contains("Comparison"));
    }

    @Test
    void testScopedValueContext() {
        // 验证 ScopedValue 可以正确设置和获取值
        String result = service.demonstrateScopedValue();

        assertNotNull(result);
        // 验证返回的格式
        assertTrue(result.contains("user-") || result.contains("UserId"));
    }
}
