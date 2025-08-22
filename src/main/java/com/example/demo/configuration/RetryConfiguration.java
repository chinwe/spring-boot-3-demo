package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.example.demo.exception.NetworkException;
import com.example.demo.exception.TemporaryException;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Retry配置类
 * 启用Spring Retry功能并配置相关的Bean
 */
@Slf4j
@Configuration
@EnableRetry
public class RetryConfiguration {

    /**
     * 配置自定义的RetryTemplate
     * 用于演示编程式重试
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 设置重试策略 - 最多重试3次，只对指定异常重试
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(TemporaryException.class, true);
        retryableExceptions.put(NetworkException.class, true);
        retryPolicy.setRetryableExceptions(retryableExceptions);
        
        retryTemplate.setRetryPolicy(retryPolicy);

        // 设置退避策略 - 指数退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L); // 初始延迟1秒
        backOffPolicy.setMultiplier(2.0);        // 每次延迟翻倍
        backOffPolicy.setMaxInterval(10000L);    // 最大延迟10秒
        
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 注册监听器
        retryTemplate.registerListener(customRetryListener());

        return retryTemplate;
    }

    /**
     * 自定义重试监听器
     */
    @Bean
    public RetryListener customRetryListener() {
        return new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(org.springframework.retry.RetryContext context, 
                    org.springframework.retry.RetryCallback<T, E> callback) {
                log.info("开始重试操作，重试上下文: {}", context.getAttribute("context.name"));
                return true;
            }

            @Override
            public <T, E extends Throwable> void onSuccess(org.springframework.retry.RetryContext context, 
                    org.springframework.retry.RetryCallback<T, E> callback, T result) {
                log.info("重试操作成功，重试次数: {}", context.getRetryCount());
            }

            @Override
            public <T, E extends Throwable> void onError(org.springframework.retry.RetryContext context, 
                    org.springframework.retry.RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("重试操作失败，重试次数: {}，异常: {}", 
                        context.getRetryCount(), throwable.getMessage());
            }

            @Override
            public <T, E extends Throwable> void close(org.springframework.retry.RetryContext context, 
                    org.springframework.retry.RetryCallback<T, E> callback, Throwable throwable) {
                if (throwable != null) {
                    log.error("重试操作最终失败，总重试次数: {}，最终异常: {}", 
                            context.getRetryCount(), throwable.getMessage());
                } else {
                    log.info("重试操作结束，总重试次数: {}", context.getRetryCount());
                }
            }
        };
    }
}