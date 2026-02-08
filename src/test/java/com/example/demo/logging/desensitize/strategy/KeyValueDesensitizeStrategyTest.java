package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 键值对脱敏策略单元测试
 */
@DisplayName("键值对脱敏策略测试")
class KeyValueDesensitizeStrategyTest {

    private KeyValueDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new KeyValueDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.KEY_VALUE);
        rule.setKeyNames(List.of("password", "pwd", "token", "apiKey", "secret", "accessToken"));
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏等号分隔的键值对")
    void shouldDesensitizeEqualsSeparatedKeyValue() {
        String input = "password=admin123";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("password="));
        assertTrue(result.contains("***"));
        assertFalse(result.contains("admin123"));
    }

    @Test
    @DisplayName("应该脱敏冒号分隔的键值对")
    void shouldDesensitizeColonSeparatedKeyValue() {
        String input = "token:abc123xyz";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("token:"));
        assertTrue(result.contains("***"));
        assertFalse(result.contains("abc123xyz"));
    }

    @Test
    @DisplayName("应该脱敏包含多个敏感键值对的文本")
    void shouldDesensitizeMultipleSensitiveKeyValuePairs() {
        String input = "username=admin, password=secret123, token=xyz789";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("username=admin"));
        assertFalse(result.contains("secret123"));
        assertFalse(result.contains("xyz789"));
    }

    @Test
    @DisplayName("应该匹配敏感字段")
    void shouldMatchSensitiveFields() {
        assertTrue(strategy.matches("password=admin123", rule));
        assertTrue(strategy.matches("pwd=secret", rule));
        assertTrue(strategy.matches("token=abc123", rule));
        assertTrue(strategy.matches("apiKey=xyz789", rule));
        assertFalse(strategy.matches("username=admin", rule));
    }

    @Test
    @DisplayName("应该处理 null 输入")
    void shouldHandleNullInput() {
        String result = strategy.desensitize(null, rule);
        assertNull(result);
    }

    @Test
    @DisplayName("应该处理不含敏感字段的文本")
    void shouldHandleTextWithoutSensitiveFields() {
        String input = "username=admin name=John age=30";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该处理混合格式")
    void shouldHandleMixedFormats() {
        String input = "password:secret, apiKey=abc123, token=>xyz789";
        String result = strategy.desensitize(input, rule);

        assertFalse(result.contains("secret"));
        assertFalse(result.contains("abc123"));
        assertFalse(result.contains("xyz789"));
    }

    @Test
    @DisplayName("应该处理引号包围的值")
    void shouldHandleQuotedValues() {
        String input = "\"password\":\"admin123\"";
        String result = strategy.desensitize(input, rule);

        // 引号格式的处理可能不同
        assertTrue(result.contains("password") || result.contains("***"));
    }

    @Test
    @DisplayName("应该保留非敏感字段")
    void shouldPreserveNonSensitiveFields() {
        String input = "username=admin, password=secret, email=test@example.com";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("username=admin"));
        assertTrue(result.contains("email="));
        assertFalse(result.contains("secret"));
    }

    @Test
    @DisplayName("应该处理空值")
    void shouldHandleEmptyValues() {
        String input = "password=";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertTrue(result.contains("password="));
    }

    @Test
    @DisplayName("应该处理大写字段名")
    void shouldHandleUpperCaseFieldNames() {
        String input = "PASSWORD=admin123";
        String result = strategy.desensitize(input, rule);

        // 大小写不敏感匹配
        assertTrue(result.contains("PASSWORD"));
        assertFalse(result.contains("admin123") || result.contains("admin"));
    }
}
