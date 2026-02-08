package com.example.demo.logging.desensitize.strategy;

import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 密码脱敏策略（基于键值对字段名）
 * 示例: password=admin123 -> password=******
 */
@Component
public class PasswordDesensitizeStrategy extends AbstractDesensitizeStrategy {

    @Override
    public DesensitizeType getSupportedType() {
        return DesensitizeType.PASSWORD;
    }

    @Override
    public boolean matches(String input, DesensitizeRule rule) {
        if (!isRuleSupported(rule) || input == null) {
            return false;
        }
        return containsSensitiveKey(input, rule.getKeyNames());
    }

    @Override
    public String desensitize(String input, DesensitizeRule rule) {
        if (input == null || !isRuleSupported(rule)) {
            return input;
        }

        List<String> keyNames = rule.getKeyNames();
        if (keyNames == null || keyNames.isEmpty()) {
            return input;
        }

        return desensitizeByKeyNames(input, keyNames, rule);
    }

    /**
     * 检查是否包含敏感字段名
     */
    private boolean containsSensitiveKey(String input, List<String> keyNames) {
        if (keyNames == null || keyNames.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();
        return keyNames.stream()
            .anyMatch(key -> lowerInput.contains(key.toLowerCase() + "=") ||
                         lowerInput.contains(key.toLowerCase() + ":") ||
                         lowerInput.contains("\"" + key.toLowerCase() + "\""));
    }

    /**
     * 根据字段名进行脱敏
     * 支持格式: key=value, "key":"value", key: value
     */
    private String desensitizeByKeyNames(String input, List<String> keyNames, DesensitizeRule rule) {
        String result = input;

        for (String keyName : keyNames) {
            result = desensitizeByKey(result, keyName, rule);
        }

        return result;
    }

    /**
     * 对特定键的值进行脱敏
     */
    private String desensitizeByKey(String input, String keyName, DesensitizeRule rule) {
        // 支持 key=value, key:value 格式
        String pattern = "(?i)(" + java.util.regex.Pattern.quote(keyName) + "\\s*[=:]\\s*)([^,}\\s\"]+)";
        var compiledPattern = compilePattern(pattern);
        var matcher = compiledPattern.matcher(input);

        if (!matcher.find()) {
            return input;
        }

        matcher.reset();
        var sb = new StringBuffer();
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String mask = generateMask(6, rule);
            matcher.appendReplacement(sb, prefix + mask);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    protected String desensitizeMatched(String matched, DesensitizeRule rule) {
        // 对于密码类型，全部替换为掩码
        return generateMask(6, rule);
    }
}
