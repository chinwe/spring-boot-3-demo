package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 银行卡脱敏策略单元测试
 */
@DisplayName("银行卡脱敏策略测试")
class BankCardDesensitizeStrategyTest {

    private BankCardDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new BankCardDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.BANK_CARD);
        rule.setPattern("\\b\\d{16,19}\\b");
        rule.setKeepPrefix(4);
        rule.setKeepSuffix(4);
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏16位银行卡号")
    void shouldDesensitize16DigitBankCard() {
        String input = "6222021234567890";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("6222"));
        assertTrue(result.endsWith("7890"));
        assertEquals(16, result.length());
    }

    @Test
    @DisplayName("应该脱敏19位银行卡号")
    void shouldDesensitize19DigitBankCard() {
        String input = "6222021234567890123";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("6222"));
        assertTrue(result.endsWith("0123"));
        assertEquals(19, result.length());
    }

    @Test
    @DisplayName("应该脱敏包含多个银行卡号的文本")
    void shouldDesensitizeMultipleBankCards() {
        String input = "Cards: 6222021234567890 and 6228123456789012";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("6222********7890"));
        assertTrue(result.contains("6228********9012"));
    }

    @Test
    @DisplayName("应该匹配银行卡号格式")
    void shouldMatchBankCardPattern() {
        assertTrue(strategy.matches("6222021234567890", rule));
        assertTrue(strategy.matches("6222021234567890123", rule));
        assertTrue(strategy.matches("1234567890123456", rule));
        assertFalse(strategy.matches("123456789012345", rule)); // 15位
        assertFalse(strategy.matches("12345678901234567890", rule)); // 20位
    }

    @Test
    @DisplayName("应该处理 null 输入")
    void shouldHandleNullInput() {
        String result = strategy.desensitize(null, rule);
        assertNull(result);
    }

    @Test
    @DisplayName("应该处理不含银行卡号的文本")
    void shouldHandleTextWithoutBankCard() {
        String input = "This is just plain text";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该保留自定义前缀和后缀长度")
    void shouldRespectCustomKeepLengths() {
        rule.setKeepPrefix(6);
        rule.setKeepSuffix(2);
        String input = "6222021234567890";
        String result = strategy.desensitize(input, rule);

        assertEquals("622202********90", result);
    }

    @Test
    @DisplayName("应该处理不同长度的银行卡号")
    void shouldHandleDifferentLengthBankCards() {
        String[] cards = {
            "1234567890123456",    // 16位
            "12345678901234567",   // 17位
            "123456789012345678",  // 18位
            "1234567890123456789",  // 19位
        };

        for (String card : cards) {
            String result = strategy.desensitize(card, rule);
            assertEquals(card.length(), result.length());
            assertTrue(result.startsWith(card.substring(0, 4)));
            assertTrue(result.endsWith(card.substring(card.length() - 4)));
            assertFalse(result.equals(card));
        }
    }
}
