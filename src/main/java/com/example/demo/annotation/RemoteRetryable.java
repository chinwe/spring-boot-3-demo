package com.example.demo.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义远程重试注解
 * 用于远程服务调用的重试，配置更积极的重试策略
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Retryable(maxAttempts = 5, 
           backoff = @Backoff(delay = 1000, maxDelay = 30000, multiplier = 1.2, random = true))
public @interface RemoteRetryable {
    
    @AliasFor(annotation = Retryable.class, attribute = "recover")
    String recover() default "";

    @AliasFor(annotation = Retryable.class, attribute = "value")
    Class<? extends Throwable>[] value() default {};

    @AliasFor(annotation = Retryable.class, attribute = "retryFor")
    Class<? extends Throwable>[] retryFor() default {};

    @AliasFor(annotation = Retryable.class, attribute = "noRetryFor")
    Class<? extends Throwable>[] noRetryFor() default {};

    @AliasFor(annotation = Retryable.class, attribute = "label")
    String label() default "";
}