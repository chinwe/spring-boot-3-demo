package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.springframework.stereotype.Component;

/**
 * 手机号脱敏策略
 * 示例: 13812345678 -> 138****5678
 */
@Component
public class PhoneDesensitizeStrategy extends AbstractDesensitizeStrategy {

    // 预编译手机号正则表达式
    private static final String PHONE_PATTERN = "\\b1[3-9]\\d{9}\\b";

    @Override
    public DesensitizeType getSupportedType() {
        return DesensitizeType.PHONE;
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null) {
            return false;
        }
        String pattern = rule.getPattern() != null ? rule.getPattern() : PHONE_PATTERN;
        return compilePattern(pattern).matcher(input).find();
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }

        String pattern = rule.getPattern() != null ? rule.getPattern() : PHONE_PATTERN;
        return doDesensitize(input, rule, compilePattern(pattern));
    }

    @Override
    protected String desensitizeMatched(String phone, DesensitizeRule rule) {
        // 手机号固定11位，保留前3后4
        int length = phone.length();
        if (length != 11) {
            return phone;
        }

        int keepPrefix = Math.min(rule.getKeepPrefix(), 6);
        int keepSuffix = Math.min(rule.getKeepSuffix(), 4);

        String prefix = phone.substring(0, keepPrefix);
        String suffix = phone.substring(length - keepSuffix);
        int maskLength = length - keepPrefix - keepSuffix;

        return prefix + generateMask(maskLength, rule) + suffix;
    }
}
