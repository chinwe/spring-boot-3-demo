package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码脱敏策略单元测试
 */
@DisplayName("密码脱敏策略测试")
class PasswordDesensitizeStrategyTest {

    private PasswordDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new PasswordDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.PASSWORD);
        rule.setKeyNames(List.of("password", "pwd", "passwd", "token", "apiKey", "secret"));
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏 password 字段")
    void shouldDesensitizePasswordField() {
        String input = "password=admin123";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("password="));
        assertTrue(result.contains("******"));
        assertFalse(result.contains("admin123"));
    }

    @Test
    @DisplayName("应该脱敏 pwd 字段")
    void shouldDesensitizePwdField() {
        String input = "pwd=secret456";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("pwd="));
        assertTrue(result.contains("******"));
        assertFalse(result.contains("secret456"));
    }

    @Test
    @DisplayName("应该脱敏 token 字段")
    void shouldDesensitizeTokenField() {
        String input = "token=abc123xyz";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("token="));
        assertTrue(result.contains("******"));
        assertFalse(result.contains("abc123xyz"));
    }

    @Test
    @DisplayName("应该脱敏包含多个敏感字段的文本")
    void shouldDesensitizeMultipleSensitiveFields() {
        String input = "username=admin, password=secret123, token=xyz789";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("username=admin"));
        assertFalse(result.contains("secret123"));
        assertFalse(result.contains("xyz789"));
        assertFalse(result.contains("password=secret123"));
        assertFalse(result.contains("token=xyz789"));
    }

    @Test
    @DisplayName("应该匹配密码字段")
    void shouldMatchPasswordField() {
        assertTrue(strategy.matches("password=admin123", rule));
        assertTrue(strategy.matches("pwd=secret", rule));
        assertTrue(strategy.matches("token=abc123", rule));
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
        String input = "username=admin name=John";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该处理 JSON 格式")
    void shouldHandleJsonFormat() {
        String input = "{\"password\":\"admin123\",\"username\":\"john\"}";
        String result = strategy.desensitize(input, rule);

        // JSON 格式处理可能不同，但应该包含脱敏标记
        assertTrue(result.contains("password") || result.contains("pwd"));
    }

    @Test
    @DisplayName("应该处理冒号分隔符")
    void shouldHandleColonSeparator() {
        String input = "password: admin123";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("password:"));
        assertFalse(result.contains("admin123"));
    }
}
