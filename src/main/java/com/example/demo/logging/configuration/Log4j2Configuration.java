package com.example.demo.logging.configuration;

import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import com.example.demo.logging.desensitize.layout.DesensitizePatternLayout;
import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.strategy.AbstractDesensitizeStrategy;
import com.example.demo.logging.desensitize.strategy.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Log4j2 自动配置类
 * 负责：
 * 1. 加载 YAML 脱敏配置文件
 * 2. 注册所有脱敏策略 Bean
 * 3. 将配置注入到 DesensitizePatternLayout 静态持有者
 */
@Slf4j
@Configuration
public class Log4j2Configuration {

    private static final String DEFAULT_CONFIG_FILE = "log-desensitize.yml";

    /**
     * 脱敏配置 Bean
     */
    @Bean
    public DesensitizeConfig desensitizeConfig() {
        return loadDesensitizeConfig();
    }

    /**
     * 脱敏策略映射 Bean
     */
    @Bean
    public Map<DesensitizeType, DesensitizeStrategy> desensitizeStrategyMap(
            EmailDesensitizeStrategy emailStrategy,
            PhoneDesensitizeStrategy phoneStrategy,
            IdCardDesensitizeStrategy idCardStrategy,
            BankCardDesensitizeStrategy bankCardStrategy,
            PasswordDesensitizeStrategy passwordStrategy,
            AddressDesensitizeStrategy addressStrategy,
            KeyValueDesensitizeStrategy keyValueStrategy) {

        Map<DesensitizeType, DesensitizeStrategy> strategyMap = new HashMap<>();
        strategyMap.put(DesensitizeType.EMAIL, emailStrategy);
        strategyMap.put(DesensitizeType.PHONE, phoneStrategy);
        strategyMap.put(DesensitizeType.ID_CARD, idCardStrategy);
        strategyMap.put(DesensitizeType.BANK_CARD, bankCardStrategy);
        strategyMap.put(DesensitizeType.PASSWORD, passwordStrategy);
        strategyMap.put(DesensitizeType.ADDRESS, addressStrategy);
        strategyMap.put(DesensitizeType.KEY_VALUE, keyValueStrategy);

        log.info("Registered {} desensitize strategies", strategyMap.size());
        return strategyMap;
    }

    /**
     * 初始化 Log4j2 脱敏配置
     * 将 Spring 加载的配置注入到 DesensitizePatternLayout 静态持有者
     */
    @Bean
    public Log4j2DesensitizeInitializer log4j2DesensitizeInitializer(
            DesensitizeConfig desensitizeConfig,
            Map<DesensitizeType, DesensitizeStrategy> desensitizeStrategyMap) {

        Log4j2DesensitizeInitializer initializer = new Log4j2DesensitizeInitializer();

        // 设置 PatternLayout 静态配置
        DesensitizePatternLayout.setStaticConfig(desensitizeConfig);
        DesensitizePatternLayout.setStaticStrategyMap(desensitizeStrategyMap);

        // 设置正则表达式缓存状态
        AbstractDesensitizeStrategy.setCacheEnabled(desensitizeConfig);

        log.info("Desensitize static config initialized with {} rules, enabled={}, cache={}",
            desensitizeConfig.getRules() != null ? desensitizeConfig.getRules().size() : 0,
            desensitizeConfig.isEnabled(),
            desensitizeConfig.getPerformance() != null && desensitizeConfig.getPerformance().isCachePatterns());

        return initializer;
    }

    /**
     * 加载脱敏配置文件
     */
    private DesensitizeConfig loadDesensitizeConfig() {
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_CONFIG_FILE);
            if (!resource.exists()) {
                log.warn("Desensitize config file not found: {}, using default config", DEFAULT_CONFIG_FILE);
                return createDefaultConfig();
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            try (InputStream inputStream = resource.getInputStream()) {
                DesensitizeConfig config = mapper.readValue(inputStream, DesensitizeConfig.class);
                log.info("Loaded desensitize config: {} rules, enabled={}",
                    config.getRules() != null ? config.getRules().size() : 0,
                    config.isEnabled());
                return config;
            }
        } catch (Exception e) {
            log.warn("Failed to load desensitize config: {}, using default config", e.getMessage());
            return createDefaultConfig();
        }
    }

    /**
     * 创建默认配置
     */
    private DesensitizeConfig createDefaultConfig() {
        DesensitizeConfig config = new DesensitizeConfig();
        config.setEnabled(true);

        List<DesensitizeRule> rules = List.of(
            createEmailRule(),
            createPhoneRule(),
            createIdCardRule(),
            createBankCardRule(),
            createPasswordRule(),
            createAddressRule()
        );
        config.setRules(rules);

        return config;
    }

    /**
     * 创建邮箱规则
     */
    private DesensitizeRule createEmailRule() {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setType(DesensitizeType.EMAIL);
        rule.setEnabled(true);
        rule.setPattern("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        rule.setKeepPrefix(1);
        rule.setKeepSuffix(0);
        rule.setMaskChar('*');
        return rule;
    }

    /**
     * 创建手机号规则
     */
    private DesensitizeRule createPhoneRule() {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setType(DesensitizeType.PHONE);
        rule.setEnabled(true);
        rule.setPattern("\\b1[3-9]\\d{9}\\b");
        rule.setKeepPrefix(3);
        rule.setKeepSuffix(4);
        rule.setMaskChar('*');
        return rule;
    }

    /**
     * 创建身份证规则
     */
    private DesensitizeRule createIdCardRule() {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setType(DesensitizeType.ID_CARD);
        rule.setEnabled(true);
        rule.setPattern("\\b[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]\\b");
        rule.setKeepPrefix(6);
        rule.setKeepSuffix(4);
        rule.setMaskChar('*');
        return rule;
    }

    /**
     * 创建银行卡规则
     */
    private DesensitizeRule createBankCardRule() {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setType(DesensitizeType.BANK_CARD);
        rule.setEnabled(true);
        rule.setPattern("\\b\\d{16,19}\\b");
        rule.setKeepPrefix(4);
        rule.setKeepSuffix(4);
        rule.setMaskChar('*');
        return rule;
    }

    /**
     * 创建密码规则
     */
    private DesensitizeRule createPasswordRule() {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setType(DesensitizeType.PASSWORD);
        rule.setEnabled(true);
        rule.setKeyNames(List.of("password", "pwd", "passwd", "token", "apiKey", "secret"));
        rule.setMaskChar('*');
        return rule;
    }

    /**
     * 创建地址规则
     */
    private DesensitizeRule createAddressRule() {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setType(DesensitizeType.ADDRESS);
        rule.setEnabled(true);
        rule.setPattern("[\\u4e00-\\u9fa5]{2,}(省|市|区|县|镇|街道|路|巷|号|室)[\\u4e00-\\u9fa5]{2,}");
        rule.setKeepPrefix(6);
        rule.setKeepSuffix(0);
        rule.setMaskChar('*');
        return rule;
    }

    /**
     * Log4j2 脱敏初始化器
     * 用于确保配置在 Log4j2 初始化后正确注入
     */
    public static class Log4j2DesensitizeInitializer {
        // 标记类，仅用于触发初始化
    }
}
