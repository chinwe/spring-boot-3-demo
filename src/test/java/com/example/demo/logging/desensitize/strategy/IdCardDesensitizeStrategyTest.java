package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 身份证脱敏策略单元测试
 */
@DisplayName("身份证脱敏策略测试")
class IdCardDesensitizeStrategyTest {

    private IdCardDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new IdCardDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.ID_CARD);
        rule.setPattern("\\b[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]\\b");
        rule.setKeepPrefix(6);
        rule.setKeepSuffix(4);
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏18位身份证号")
    void shouldDesensitize18DigitIdCard() {
        String input = "110101199001011234";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("110101"));
        assertTrue(result.endsWith("1234"));
        assertFalse(result.equals(input));
        assertEquals(18, result.length());
    }

    @Test
    @DisplayName("应该脱敏包含大写X的身份证号")
    void shouldDesensitizeIdCardWithUppercaseX() {
        String input = "11010119900101123X";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("110101"));
        assertTrue(result.endsWith("23X"));
        assertFalse(result.equals(input));
    }

    @Test
    @DisplayName("应该脱敏包含小写x的身份证号")
    void shouldDesensitizeIdCardWithLowercaseX() {
        String input = "11010119900101123x";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("110101"));
        assertTrue(result.endsWith("23x"));
    }

    @Test
    @DisplayName("应该匹配身份证格式")
    void shouldMatchIdCardPattern() {
        assertTrue(strategy.matches("110101199001011234", rule));
        assertTrue(strategy.matches("310101198512311234", rule));
        assertTrue(strategy.matches("440101200001011234", rule));
        assertFalse(strategy.matches("12345", rule));
        assertFalse(strategy.matches("123456789012345678", rule));
    }

    @Test
    @DisplayName("应该处理 null 输入")
    void shouldHandleNullInput() {
        String result = strategy.desensitize(null, rule);
        assertNull(result);
    }

    @Test
    @DisplayName("应该处理不含身份证的文本")
    void shouldHandleTextWithoutIdCard() {
        String input = "This is just plain text";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该保留自定义前缀和后缀长度")
    void shouldRespectCustomKeepLengths() {
        rule.setKeepPrefix(8);
        rule.setKeepSuffix(2);
        String input = "110101199001011234";
        String result = strategy.desensitize(input, rule);

        assertEquals("11010119********34", result);
    }
}
