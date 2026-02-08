package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.springframework.stereotype.Component;

/**
 * 身份证号脱敏策略
 * 示例: 110101199001011234 -> 110101********1234
 */
@Component
public class IdCardDesensitizeStrategy extends AbstractDesensitizeStrategy {

    // 预编译身份证号正则表达式（支持15位和18位）
    private static final String ID_CARD_PATTERN = "\\b[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]\\b";

    @Override
    public DesensitizeType getSupportedType() {
        return DesensitizeType.ID_CARD;
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null) {
            return false;
        }
        String pattern = rule.getPattern() != null ? rule.getPattern() : ID_CARD_PATTERN;
        return compilePattern(pattern).matcher(input).find();
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }

        String pattern = rule.getPattern() != null ? rule.getPattern() : ID_CARD_PATTERN;
        return doDesensitize(input, rule, compilePattern(pattern));
    }

    @Override
    protected String desensitizeMatched(String idCard, DesensitizeRule rule) {
        // 身份证号保留前6后4
        int length = idCard.length();
        if (length < 15) {
            return idCard;
        }

        int keepPrefix = Math.min(rule.getKeepPrefix(), 8);
        int keepSuffix = Math.min(rule.getKeepSuffix(), 4);

        String prefix = idCard.substring(0, keepPrefix);
        String suffix = idCard.substring(length - keepSuffix);
        int maskLength = length - keepPrefix - keepSuffix;

        return prefix + generateMask(maskLength, rule) + suffix;
    }
}
