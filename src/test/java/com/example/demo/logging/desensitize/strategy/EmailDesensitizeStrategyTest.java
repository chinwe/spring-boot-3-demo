package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮箱脱敏策略单元测试
 */
@DisplayName("邮箱脱敏策略测试")
class EmailDesensitizeStrategyTest {

    private EmailDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new EmailDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.EMAIL);
        rule.setPattern("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        rule.setKeepPrefix(1);
        rule.setKeepSuffix(0);
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏标准邮箱地址")
    void shouldDesensitizeStandardEmail() {
        String input = "test@example.com";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("t***"));
        assertTrue(result.contains("@example.com"));
        assertFalse(result.equals(input));
    }

    @Test
    @DisplayName("应该脱敏包含多个邮箱的文本")
    void shouldDesensitizeMultipleEmails() {
        String input = "Emails: test@example.com and admin@test.org";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("t***"));
        assertTrue(result.contains("a***"));
        assertFalse(result.contains("test@example.com"));
    }

    @Test
    @DisplayName("应该匹配邮箱格式")
    void shouldMatchEmailPattern() {
        assertTrue(strategy.matches("test@example.com", rule));
        assertTrue(strategy.matches("user.name@domain.co.uk", rule));
        assertFalse(strategy.matches("not-an-email", rule));
    }

    @Test
    @DisplayName("应该处理 null 输入")
    void shouldHandleNullInput() {
        String result = strategy.desensitize(null, rule);
        assertNull(result);
    }

    @Test
    @DisplayName("应该处理不含邮箱的文本")
    void shouldHandleTextWithoutEmail() {
        String input = "This is just plain text";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该保留自定义前缀长度")
    void shouldRespectCustomKeepPrefix() {
        rule.setKeepPrefix(3);
        String input = "test@example.com";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("tes"));
    }

    @Test
    @DisplayName("应该处理短邮箱用户名")
    void shouldHandleShortUsername() {
        String input = "a@b.co";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertTrue(result.contains("@"));
    }
}
