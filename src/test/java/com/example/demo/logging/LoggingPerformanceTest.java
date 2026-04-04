package com.example.demo.logging;

import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import com.example.demo.logging.desensitize.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志脱敏性能测试
 */
@SpringBootTest
@DisplayName("日志脱敏性能测试")
class LoggingPerformanceTest {

    private static final int WARMUP_ITERATIONS = 100;
    private static final int TEST_ITERATIONS = 10000;
    private static final long MAX_AVG_TIME_NS = 1000000; // 1ms per log

    @Resource
    private DesensitizeConfig desensitizeConfig;

    @Test
    @DisplayName("邮箱脱敏性能测试")
    void emailDesensitizationPerformanceTest() {
        // Given
        EmailDesensitizeStrategy strategy = new EmailDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.EMAIL);
        assertNotNull(rule, "Email rule should exist");
        String input = "User email: test@example.com";

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }
        long avgTimeNs = (System.nanoTime() - startTime) / TEST_ITERATIONS;

        // Then
        System.out.println("Email desensitization - Avg time: " + (avgTimeNs / 1000.0) + " μs");
        assertTrue(avgTimeNs < MAX_AVG_TIME_NS, "Email desensitization should be fast enough");
    }

    @Test
    @DisplayName("手机号脱敏性能测试")
    void phoneDesensitizationPerformanceTest() {
        // Given
        PhoneDesensitizeStrategy strategy = new PhoneDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.PHONE);
        assertNotNull(rule, "Phone rule should exist");
        String input = "User phone: 13812345678";

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }
        long avgTimeNs = (System.nanoTime() - startTime) / TEST_ITERATIONS;

        // Then
        System.out.println("Phone desensitization - Avg time: " + (avgTimeNs / 1000.0) + " μs");
        assertTrue(avgTimeNs < MAX_AVG_TIME_NS, "Phone desensitization should be fast enough");
    }

    @Test
    @DisplayName("身份证脱敏性能测试")
    void idCardDesensitizationPerformanceTest() {
        // Given
        IdCardDesensitizeStrategy strategy = new IdCardDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.ID_CARD);
        assertNotNull(rule, "ID card rule should exist");
        String input = "User ID card: 110101199001011234";

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }
        long avgTimeNs = (System.nanoTime() - startTime) / TEST_ITERATIONS;

        // Then
        System.out.println("ID card desensitization - Avg time: " + (avgTimeNs / 1000.0) + " μs");
        assertTrue(avgTimeNs < MAX_AVG_TIME_NS, "ID card desensitization should be fast enough");
    }

    @Test
    @DisplayName("密码脱敏性能测试")
    void passwordDesensitizationPerformanceTest() {
        // Given
        PasswordDesensitizeStrategy strategy = new PasswordDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.PASSWORD);
        assertNotNull(rule, "Password rule should exist");
        String input = "Login: username=admin, password=secret123";

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }
        long avgTimeNs = (System.nanoTime() - startTime) / TEST_ITERATIONS;

        // Then
        System.out.println("Password desensitization - Avg time: " + (avgTimeNs / 1000.0) + " μs");
        assertTrue(avgTimeNs < MAX_AVG_TIME_NS, "Password desensitization should be fast enough");
    }

    @Test
    @DisplayName("复合脱敏性能测试")
    void combinedDesensitizationPerformanceTest() {
        // Given
        List<DesensitizeRule> rules = desensitizeConfig != null ?
            desensitizeConfig.getEnabledRules() : List.of();
        assertFalse(rules.isEmpty(), "Should have at least one rule");
        String input = "Complete user data: email=test@example.com, phone=13812345678, " +
                      "idCard=110101199001011234, password=secret123, address=北京市朝阳区建国路88号";

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            String result = input;
            for (DesensitizeRule rule : rules) {
                DesensitizeStrategy strategy = getStrategyForType(rule.getType());
                if (strategy != null) {
                    result = strategy.desensitize(result, rule);
                }
            }
        }

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            String result = input;
            for (DesensitizeRule rule : rules) {
                DesensitizeStrategy strategy = getStrategyForType(rule.getType());
                if (strategy != null) {
                    result = strategy.desensitize(result, rule);
                }
            }
        }
        long avgTimeNs = (System.nanoTime() - startTime) / TEST_ITERATIONS;

        // Then
        System.out.println("Combined desensitization - Avg time: " + (avgTimeNs / 1000.0) + " μs");
        System.out.println("Throughput: " + (1_000_000_000.0 / avgTimeNs) + " logs/second");
        // 复合脱敏可能需要更长时间，但不应超过 5ms
        assertTrue(avgTimeNs < MAX_AVG_TIME_NS * 5, "Combined desensitization should be fast enough");
    }

    @Test
    @DisplayName("性能基准测试 - 不含敏感信息")
    void performanceBenchmarkWithoutSensitiveData() {
        // Given
        EmailDesensitizeStrategy strategy = new EmailDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.EMAIL);
        assertNotNull(rule, "Email rule should exist");
        String input = "This is just plain text without any sensitive information";

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            strategy.desensitize(input, rule);
        }
        long avgTimeNs = (System.nanoTime() - startTime) / TEST_ITERATIONS;

        // Then
        System.out.println("No sensitive data - Avg time: " + (avgTimeNs / 1000.0) + " μs");
        // 不含敏感信息时应该非常快
        assertTrue(avgTimeNs < MAX_AVG_TIME_NS / 10, "Should be very fast for non-sensitive data");
    }

    /**
     * 根据类型查找规则
     */
    private DesensitizeRule findRuleByType(DesensitizeType type) {
        if (desensitizeConfig == null || desensitizeConfig.getRules() == null) {
            return null;
        }
        return desensitizeConfig.getRules().stream()
            .filter(rule -> rule.getType() == type)
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据类型获取策略实例
     */
    private DesensitizeStrategy getStrategyForType(DesensitizeType type) {
        return switch (type) {
            case EMAIL -> new EmailDesensitizeStrategy();
            case PHONE -> new PhoneDesensitizeStrategy();
            case ID_CARD -> new IdCardDesensitizeStrategy();
            case BANK_CARD -> new BankCardDesensitizeStrategy();
            case PASSWORD -> new PasswordDesensitizeStrategy();
            case ADDRESS -> new AddressDesensitizeStrategy();
            case KEY_VALUE -> new KeyValueDesensitizeStrategy();
        };
    }
}
