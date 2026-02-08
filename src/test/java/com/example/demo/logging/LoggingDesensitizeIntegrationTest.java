package com.example.demo.logging;

import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import com.example.demo.logging.desensitize.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志脱敏功能集成测试
 */
@SpringBootTest
@DisplayName("日志脱敏集成测试")
class LoggingDesensitizeIntegrationTest {

    @Autowired(required = false)
    private DesensitizeConfig desensitizeConfig;

    @Test
    @DisplayName("应该成功加载脱敏配置")
    void shouldLoadDesensitizeConfig() {
        assertNotNull(desensitizeConfig, "DesensitizeConfig should be loaded");
        assertTrue(desensitizeConfig.isEnabled(), "Desensitization should be enabled");
    }

    @Test
    @DisplayName("应该包含所有必需的脱敏规则")
    void shouldContainAllRequiredRules() {
        assertNotNull(desensitizeConfig);

        List<DesensitizeRule> rules = desensitizeConfig.getRules();
        assertNotNull(rules);
        assertFalse(rules.isEmpty(), "Should have at least one rule");

        // 检查是否包含所有必需的规则类型
        List<DesensitizeType> requiredTypes = List.of(
            DesensitizeType.EMAIL,
            DesensitizeType.PHONE,
            DesensitizeType.ID_CARD,
            DesensitizeType.BANK_CARD,
            DesensitizeType.PASSWORD
        );

        for (DesensitizeType requiredType : requiredTypes) {
            boolean hasRule = rules.stream()
                .anyMatch(rule -> rule.getType() == requiredType);
            assertTrue(hasRule, "Should have rule for type: " + requiredType);
        }
    }

    @Test
    @DisplayName("邮箱脱敏策略应该正常工作")
    void emailStrategyShouldWork() {
        EmailDesensitizeStrategy strategy = new EmailDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.EMAIL);

        assertNotNull(rule, "Email rule should exist");

        String input = "test@example.com";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertNotEquals(input, result);
        assertTrue(result.contains("@"));
    }

    @Test
    @DisplayName("手机号脱敏策略应该正常工作")
    void phoneStrategyShouldWork() {
        PhoneDesensitizeStrategy strategy = new PhoneDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.PHONE);

        assertNotNull(rule, "Phone rule should exist");

        String input = "13812345678";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertNotEquals(input, result);
        assertEquals(11, result.length());
    }

    @Test
    @DisplayName("身份证脱敏策略应该正常工作")
    void idCardStrategyShouldWork() {
        IdCardDesensitizeStrategy strategy = new IdCardDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.ID_CARD);

        assertNotNull(rule, "ID card rule should exist");

        String input = "110101199001011234";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertNotEquals(input, result);
        assertEquals(18, result.length());
    }

    @Test
    @DisplayName("银行卡脱敏策略应该正常工作")
    void bankCardStrategyShouldWork() {
        BankCardDesensitizeStrategy strategy = new BankCardDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.BANK_CARD);

        assertNotNull(rule, "Bank card rule should exist");

        String input = "6222021234567890";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertNotEquals(input, result);
        assertEquals(16, result.length());
    }

    @Test
    @DisplayName("密码脱敏策略应该正常工作")
    void passwordStrategyShouldWork() {
        PasswordDesensitizeStrategy strategy = new PasswordDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.PASSWORD);

        assertNotNull(rule, "Password rule should exist");

        String input = "password=admin123";
        String result = strategy.desensitize(input, rule);

        assertNotNull(result);
        assertFalse(result.contains("admin123"));
        assertTrue(result.contains("password="));
    }

    @Test
    @DisplayName("地址脱敏策略应该正常工作")
    void addressStrategyShouldWork() {
        AddressDesensitizeStrategy strategy = new AddressDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.ADDRESS);

        if (rule != null) {
            String input = "北京市朝阳区建国路88号";
            String result = strategy.desensitize(input, rule);

            assertNotNull(result);
            assertNotEquals(input, result);
        }
    }

    @Test
    @DisplayName("键值对脱敏策略应该正常工作")
    void keyValueStrategyShouldWork() {
        KeyValueDesensitizeStrategy strategy = new KeyValueDesensitizeStrategy();
        DesensitizeRule rule = findRuleByType(DesensitizeType.KEY_VALUE);

        if (rule != null) {
            String input = "password=secret";
            String result = strategy.desensitize(input, rule);

            assertNotNull(result);
            assertFalse(result.contains("secret"));
        }
    }

    @Test
    @DisplayName("应该处理包含多种敏感信息的复合场景")
    void shouldHandleComplexScenarioWithMultipleSensitiveTypes() {
        String input = "User registered: email=test@example.com, phone=13812345678, idCard=110101199001011234";

        EmailDesensitizeStrategy emailStrategy = new EmailDesensitizeStrategy();
        PhoneDesensitizeStrategy phoneStrategy = new PhoneDesensitizeStrategy();
        IdCardDesensitizeStrategy idCardStrategy = new IdCardDesensitizeStrategy();

        DesensitizeRule emailRule = findRuleByType(DesensitizeType.EMAIL);
        DesensitizeRule phoneRule = findRuleByType(DesensitizeType.PHONE);
        DesensitizeRule idCardRule = findRuleByType(DesensitizeType.ID_CARD);

        assertNotNull(emailRule);
        assertNotNull(phoneRule);
        assertNotNull(idCardRule);

        String result = input;
        result = emailStrategy.desensitize(result, emailRule);
        result = phoneStrategy.desensitize(result, phoneRule);
        result = idCardStrategy.desensitize(result, idCardRule);

        // 验证所有敏感信息都被脱敏
        assertFalse(result.contains("test@example.com"));
        assertFalse(result.contains("13812345678"));
        assertFalse(result.contains("110101199001011234"));
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
}
