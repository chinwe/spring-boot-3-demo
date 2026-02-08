package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.springframework.stereotype.Component;

/**
 * 地址脱敏策略
 * 示例: 北京市朝阳区建国路88号 -> 北京市朝阳区********
 */
@Component
public class AddressDesensitizeStrategy extends AbstractDesensitizeStrategy {

    // 预编译地址正则表达式
    private static final String ADDRESS_PATTERN = "[\\u4e00-\\u9fa5]{2,}(省|市|区|县|镇|街道|路|巷|号|室)[\\u4e00-\\u9fa5]{2,}";

    @Override
    public DesensitizeType getSupportedType() {
        return DesensitizeType.ADDRESS;
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null) {
            return false;
        }
        String pattern = rule.getPattern() != null ? rule.getPattern() : ADDRESS_PATTERN;
        return compilePattern(pattern).matcher(input).find();
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }

        String pattern = rule.getPattern() != null ? rule.getPattern() : ADDRESS_PATTERN;
        return doDesensitize(input, rule, compilePattern(pattern));
    }

    @Override
    protected String desensitizeMatched(String address, DesensitizeRule rule) {
        // 地址保留前6个字符
        int length = address.length();
        if (length < 8) {
            return address;
        }

        int keepPrefix = Math.min(rule.getKeepPrefix(), length / 2);
        int keepSuffix = rule.getKeepSuffix();

        String prefix = address.substring(0, keepPrefix);
        int maskLength = length - keepPrefix - keepSuffix;

        if (maskLength <= 0) {
            return address;
        }

        return prefix + generateMask(maskLength, rule);
    }
}
