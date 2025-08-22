package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Retry演示配置
 * 用于SpEL表达式示例中的Bean引用
 */
@Configuration
public class DemoRetryConfiguration {

    /**
     * 运行时配置Bean - 用于SpEL表达式引用
     */
    @Bean
    public RuntimeConfigs runtimeConfigs() {
        return new RuntimeConfigs();
    }

    /**
     * 运行时配置类
     */
    public static class RuntimeConfigs {
        public int getMaxAttempts() {
            return 5;
        }

        public long getInitial() {
            return 1000L;
        }

        public long getMax() {
            return 10000L;
        }

        public double getMult() {
            return 2.0;
        }
    }

    /**
     * 整数5的Bean - 用于SpEL表达式示例
     */
    @Bean("integerFiveBean")
    public Integer integerFiveBean() {
        return 5;
    }

    /**
     * 异常检查器Bean - 用于复杂的SpEL表达式示例
     */
    @Bean
    public ExceptionChecker exceptionChecker() {
        return new ExceptionChecker();
    }

    /**
     * 异常检查器类
     */
    public static class ExceptionChecker {
        public boolean shouldRetry(Throwable throwable) {
            // 自定义重试逻辑
            if (throwable == null) {
                return false;
            }
            
            String message = throwable.getMessage();
            if (message == null) {
                return false;
            }
            
            // 包含特定关键词时进行重试
            return message.contains("temporary") || 
                   message.contains("timeout") || 
                   message.contains("network");
        }
    }
}