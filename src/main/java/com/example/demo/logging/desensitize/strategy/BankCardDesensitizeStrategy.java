package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.springframework.stereotype.Component;

/**
 * 银行卡号脱敏策略
 * 示例: 6222021234567890123 -> 6222***********0123
 */
@Component
public class BankCardDesensitizeStrategy extends AbstractDesensitizeStrategy {

    // 预编译银行卡号正则表达式（16-19位）
    private static final String BANK_CARD_PATTERN = "\\b\\d{16,19}\\b";

    @Override
    public DesensitizeType getSupportedType() {
        return DesensitizeType.BANK_CARD;
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null) {
            return false;
        }
        String pattern = rule.getPattern() != null ? rule.getPattern() : BANK_CARD_PATTERN;
        return compilePattern(pattern).matcher(input).find();
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }

        String pattern = rule.getPattern() != null ? rule.getPattern() : BANK_CARD_PATTERN;
        return doDesensitize(input, rule, compilePattern(pattern));
    }

    @Override
    protected String desensitizeMatched(String bankCard, DesensitizeRule rule) {
        // 银行卡号保留前4后4
        int length = bankCard.length();
        if (length < 16) {
            return bankCard;
        }

        int keepPrefix = Math.min(rule.getKeepPrefix(), 6);
        int keepSuffix = Math.min(rule.getKeepSuffix(), 4);

        String prefix = bankCard.substring(0, keepPrefix);
        String suffix = bankCard.substring(length - keepSuffix);
        int maskLength = length - keepPrefix - keepSuffix;

        return prefix + generateMask(maskLength, rule) + suffix;
    }
}
