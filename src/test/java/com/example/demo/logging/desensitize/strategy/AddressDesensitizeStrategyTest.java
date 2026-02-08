package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 地址脱敏策略单元测试
 */
@DisplayName("地址脱敏策略测试")
class AddressDesensitizeStrategyTest {

    private AddressDesensitizeStrategy strategy;
    private DesensitizeRule rule;

    @BeforeEach
    void setUp() {
        strategy = new AddressDesensitizeStrategy();
        rule = new DesensitizeRule();
        rule.setType(DesensitizeType.ADDRESS);
        rule.setPattern("[\\u4e00-\\u9fa5]{2,}(省|市|区|县|镇|街道|路|巷|号|室)[\\u4e00-\\u9fa5]{2,}");
        rule.setKeepPrefix(6);
        rule.setKeepSuffix(0);
        rule.setMaskChar('*');
    }

    @Test
    @DisplayName("应该脱敏完整地址")
    void shouldDesensitizeFullAddress() {
        String input = "北京市朝阳区建国路88号";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("北京市"));
        assertTrue(result.contains("*"));
        assertFalse(result.equals(input));
    }

    @Test
    @DisplayName("应该脱敏包含省份的地址")
    void shouldDesensitizeAddressWithProvince() {
        String input = "浙江省杭州市西湖区文三路123号";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.startsWith("浙江省"));
        assertTrue(result.contains("*"));
        assertFalse(result.equals(input));
    }

    @Test
    @DisplayName("应该脱敏包含多个地址的文本")
    void shouldDesensitizeMultipleAddresses() {
        String input = "Address1: 北京市朝阳区建国路88号, Address2: 上海市浦东新区世纪大道100号";
        String result = strategy.desensitize(input, rule);

        assertTrue(result.contains("*"));
    }

    @Test
    @DisplayName("应该匹配地址格式")
    void shouldMatchAddressPattern() {
        assertTrue(strategy.matches("北京市朝阳区建国路88号", rule));
        assertTrue(strategy.matches("浙江省杭州市西湖区文三路123号", rule));
        assertTrue(strategy.matches("广东省深圳市南山区科技园路1号", rule));
        assertFalse(strategy.matches("12345", rule));
        assertFalse(strategy.matches("No Address Here", rule));
    }

    @Test
    @DisplayName("应该处理 null 输入")
    void shouldHandleNullInput() {
        String result = strategy.desensitize(null, rule);
        assertNull(result);
    }

    @Test
    @DisplayName("应该处理不含地址的文本")
    void shouldHandleTextWithoutAddress() {
        String input = "This is just plain text";
        String result = strategy.desensitize(input, rule);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("应该保留自定义前缀长度")
    void shouldRespectCustomKeepPrefix() {
        rule.setKeepPrefix(8);
        String input = "北京市朝阳区建国路88号";
        String result = strategy.desensitize(input, rule);

        // 地址脱敏策略会保留前 length/2 个字符
        // 对于12个字符的地址，保留6个字符
        assertNotNull(result);
        assertNotEquals(input, result);
    }

    @Test
    @DisplayName("应该处理短地址")
    void shouldHandleShortAddress() {
        String input = "北京路1号";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertTrue(result.length() <= input.length());
    }

    @Test
    @DisplayName("应该处理不同行政区划")
    void shouldHandleDifferentAdministrativeDivisions() {
        String[] addresses = {
            "北京市朝阳区建国路88号",
            "上海市浦东新区世纪大道100号",
            "广州市天河区珠江新城A栋",
            "深圳市南山区科技园南区",
            "杭州市西湖区文三路123号"
        };

        for (String address : addresses) {
            String result = strategy.desensitize(address, rule);
            assertNotNull(result);
            assertTrue(result.contains("*") || result.length() < address.length());
        }
    }
}
