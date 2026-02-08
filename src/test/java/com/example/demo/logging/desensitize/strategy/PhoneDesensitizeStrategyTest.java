package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 手机号脱敏策略单元测试
 */
@DisplayName("手机号脱敏策略测试")
class PhoneDesensitizeStrategyTest {

    private PhoneDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new PhoneDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.PHONE);
        rule.setPattern("\\b1[3-9]\\d{9}\\b");
        rule.setKeepPrefix(3);
        rule.setKeepSuffix(4);
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏标准手机号")
    void shouldDesensitizeStandardPhone() {
        String input = "13812345678";
        String result = strategy.desensitize(input, rule);

        assertEquals("138****5678", result);
    }

    @Test
    @DisplayName("应该脱敏包含多个手机号的文本")
    void shouldDesensitizeMultiplePhones() {
        String input = "Phones: 13812345678 and 15987654321";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("138****5678"));
        assertTrue(result.contains("159****4321"));
        assertFalse(result.contains("13812345678"));
    }

    @Test
    @DisplayName("应该匹配手机号格式")
    void shouldMatchPhonePattern() {
        assertTrue(strategy.matches("13812345678", rule));
        assertTrue(strategy.matches("15987654321", rule));
        assertTrue(strategy.matches("18612341234", rule));
        assertFalse(strategy.matches("12812345678", rule)); // 12开头不是有效号段
        assertFalse(strategy.matches("12345", rule));
    }

    @Test
    @DisplayName("应该处理 null 输入")
    void shouldHandleNullInput() {
        String result = strategy.desensitize(null, rule);
        assertNull(result);
    }

    @Test
    @DisplayName("应该处理不含手机号的文本")
    void shouldHandleTextWithoutPhone() {
        String input = "This is just plain text";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该保留自定义前缀和后缀长度")
    void shouldRespectCustomKeepLengths() {
        rule.setKeepPrefix(4);
        rule.setKeepSuffix(2);
        String input = "13812345678";
        String result = strategy.desensitize(input, rule);

        assertEquals("1381*****78", result);
    }

    @Test
    @DisplayName("应该处理不同号段的手机号")
    void shouldHandleDifferentPhonePrefixes() {
        String[] phones = {"13012345678", "13112345678", "13212345678",
                          "13312345678", "13412345678", "13512345678",
                          "13612345678", "13712345678", "13812345678",
                          "13912345678", "15012345678", "15112345678",
                          "15212345678", "15312345678", "15512345678",
                          "15612345678", "15712345678", "15812345678",
                          "15912345678", "18612345678", "18712345678",
                          "18812345678", "18912345678"};

        for (String phone : phones) {
            String result = strategy.desensitize(phone, rule);
            assertTrue(result.startsWith(phone.substring(0, 3)));
            assertTrue(result.endsWith(phone.substring(7)));
            assertFalse(result.equals(phone));
        }
    }
}
