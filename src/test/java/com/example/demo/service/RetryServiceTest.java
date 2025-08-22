package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.example.demo.exception.BusinessException;
import com.example.demo.exception.NetworkException;
import com.example.demo.exception.TemporaryException;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RetryService 单元测试
 * 验证各种重试场景的功能正确性
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.example.demo=DEBUG"
})
class RetryServiceTest {

    @Resource
    private RetryService retryService;

    @BeforeEach
    void setUp() {
        // 每个测试前重置计数器
        retryService.resetCounters();
    }

    @Test
    void testBasicRetryExample_Success() {
        // 测试成功场景
        String result = retryService.basicRetryExample(true);
        assertNotNull(result);
        assertTrue(result.contains("成功"));
    }

    @Test
    void testBasicRetryExample_FailureWithRecovery() {
        // 测试失败后恢复场景
        String result = retryService.basicRetryExample(false);
        assertNotNull(result);
        assertTrue(result.contains("恢复"));
    }

    @Test
    void testLocalServiceCall_Success() {
        // 测试本地服务调用成功
        String result = retryService.localServiceCall(true);
        assertNotNull(result);
        assertTrue(result.contains("本地服务调用成功"));
    }

    @Test
    void testLocalServiceCall_FailureWithRecovery() {
        // 测试本地服务调用失败后恢复
        String result = retryService.localServiceCall(false);
        assertNotNull(result);
        assertTrue(result.contains("恢复"));
    }

    @Test
    void testRemoteServiceCall_Success() {
        // 测试远程服务调用成功
        String result = retryService.remoteServiceCall(true);
        assertNotNull(result);
        assertTrue(result.contains("远程服务调用成功"));
    }

    @Test
    void testRemoteServiceCall_FailureWithRecovery() {
        // 测试远程服务调用失败后恢复
        String result = retryService.remoteServiceCall(false);
        assertNotNull(result);
        assertTrue(result.contains("恢复"));
    }

    @Test
    void testConditionalRetryExample_TemporaryException() {
        // 测试临时异常的条件重试
        String result = retryService.conditionalRetryExample("temporary");
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("恢复"));
    }

    @Test
    void testConditionalRetryExample_NetworkException() {
        // 测试网络异常的条件重试
        String result = retryService.conditionalRetryExample("network");
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("恢复"));
    }

    @Test
    void testConditionalRetryExample_BusinessException() {
        // 测试业务异常不重试的场景
        assertThrows(BusinessException.class, () -> {
            retryService.conditionalRetryExample("business");
        });
    }

    @Test
    void testImperativeRetryExample_Success() {
        // 测试编程式重试成功
        String result = retryService.imperativeRetryExample(true);
        assertNotNull(result);
        assertTrue(result.contains("编程式重试成功"));
    }

    @Test
    void testImperativeRetryExample_FailureWithRecovery() {
        // 测试编程式重试失败后恢复
        String result = retryService.imperativeRetryExample(false);
        assertNotNull(result);
        assertTrue(result.contains("恢复"));
    }

    @Test
    void testSpelRetryExample_NormalPriority() {
        // 测试SpEL重试 - 普通优先级
        String result = retryService.spelRetryExample(true, "normal");
        assertNotNull(result);
        assertTrue(result.contains("SpEL重试成功"));
        assertTrue(result.contains("normal"));
    }

    @Test
    void testSpelRetryExample_CriticalPriority() {
        // 测试SpEL重试 - 关键优先级
        String result = retryService.spelRetryExample(true, "critical");
        assertNotNull(result);
        assertTrue(result.contains("SpEL重试成功"));
        assertTrue(result.contains("critical"));
    }

    @Test
    void testSpelRetryExample_FailureWithRecovery_NormalPriority() {
        // 测试SpEL重试失败后恢复 - 普通优先级
        String result = retryService.spelRetryExample(false, "normal");
        assertNotNull(result);
        assertTrue(result.contains("恢复"));
        assertTrue(result.contains("normal"));
    }

    @Test
    void testSpelRetryExample_FailureWithRecovery_CriticalPriority() {
        // 测试SpEL重试失败后恢复 - 关键优先级
        String result = retryService.spelRetryExample(false, "critical");
        assertNotNull(result);
        assertTrue(result.contains("恢复"));
        assertTrue(result.contains("critical"));
    }

    @Test
    void testResetCounters() {
        // 测试重置计数器功能
        assertDoesNotThrow(() -> {
            retryService.resetCounters();
        });
    }

    @Test
    void testMultipleRetryOperations() {
        // 测试多个重试操作的独立性
        String result1 = retryService.basicRetryExample(true);
        String result2 = retryService.localServiceCall(true);
        String result3 = retryService.imperativeRetryExample(true);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        
        assertTrue(result1.contains("基本重试成功"));
        assertTrue(result2.contains("本地服务调用成功"));
        assertTrue(result3.contains("编程式重试成功"));
    }
}