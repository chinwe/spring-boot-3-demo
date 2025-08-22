package com.example.demo.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.example.demo.exception.NetworkException;
import com.example.demo.exception.TemporaryException;
import com.example.demo.listener.CustomRetryListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Retry配置类
 * 启用Spring Retry功能并配置相关的Bean
 */
@Slf4j
@Configuration
@EnableRetry
public class RetryConfiguration {

    @Autowired
    private CustomRetryListener customRetryListener;

    /**
     * 配置自定义的RetryTemplate
     * 用于演示编程式重试
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 设置重试策略 - 最多重试3次，只对指定异常重试
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(TemporaryException.class, true);
        retryableExceptions.put(NetworkException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        
        retryTemplate.setRetryPolicy(retryPolicy);

        // 设置退避策略 - 指数退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L); // 初始延迟1秒
        backOffPolicy.setMultiplier(2.0);        // 每次延迟翻倍
        backOffPolicy.setMaxInterval(10000L);    // 最大延迟10秒
        
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 注册监听器
        retryTemplate.registerListener(customRetryListener);

        return retryTemplate;
    }

}