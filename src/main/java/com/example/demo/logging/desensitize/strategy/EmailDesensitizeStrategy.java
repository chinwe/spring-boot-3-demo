package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.springframework.stereotype.Component;

/**
 * 邮箱脱敏策略
 * 示例: test@example.com -> t***@example.com
 */
@Component
public class EmailDesensitizeStrategy extends AbstractDesensitizeStrategy {

    // 预编译邮箱正则表达式
    private static final String EMAIL_PATTERN = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";

    @Override
    public DesensitizeType getSupportedType() {
        return DesensitizeType.EMAIL;
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null) {
            return false;
        }
        // 优先使用规则中的模式，否则使用默认模式
        String pattern = rule.getPattern() != null ? rule.getPattern() : EMAIL_PATTERN;
        return compilePattern(pattern).matcher(input).find();
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }

        String pattern = rule.getPattern() != null ? rule.getPattern() : EMAIL_PATTERN;
        var compiledPattern = compilePattern(pattern);
        var matcher = compiledPattern.matcher(input);

        if (!matcher.find()) {
            return input;
        }

        matcher.reset();
        var sb = new StringBuffer();
        while (matcher.find()) {
            String email = matcher.group();
            String desensitized = desensitizeEmail(email, rule);
            matcher.appendReplacement(sb, desensitized);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 对单个邮箱进行脱敏
     * 格式: 保留用户名第一个字符 + @ + 域名
     * 示例: test@example.com -> t***@example.com
     */
    private String desensitizeEmail(String email, DesensitizeRule rule) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        int keepChars = Math.max(1, rule.getKeepPrefix());
        if (username.length() <= keepChars) {
            return email;
        }

        String prefix = username.substring(0, keepChars);
        int maskLength = Math.min(username.length() - keepChars, 3);

        return prefix + generateMask(maskLength, rule) + domain;
    }
}
