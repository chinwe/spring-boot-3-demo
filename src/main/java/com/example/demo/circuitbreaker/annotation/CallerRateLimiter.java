package com.example.demo.circuitbreaker.annotation;

import com.example.demo.circuitbreaker.aspect.CallerRateLimiterAspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基于 X-Caller Header 的差异化限流注解
 * 支持为不同调用方设置不同的限流配额
 *
 * 使用示例：
 * <pre>
 * {@code
 * @CallerRateLimiter(
 *     prefix = "callerLimiter",
 *     defaultLimitForPeriod = 10,
 *     callerConfigs = "mobile=100,1,5;web=50,1,5;admin=1000,1,10"
 * )
 * public String myMethod() {
 *     // 方法实现
 * }
 * }
 * </pre>
 *
 * @see CallerRateLimiterAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CallerRateLimiter {

    /**
     * 限流器名称前缀
     * 实际限流器名称为：prefix + "_" + caller
     */
    String prefix() default "callerLimiter";

    /**
     * 默认限流周期内请求数
     * 当调用方未在 callerConfigs 中配置时使用此值
     */
    int defaultLimitForPeriod() default 10;

    /**
     * 默认限流刷新周期（秒）
     */
    int defaultLimitRefreshPeriodInSeconds() default 1;

    /**
     * 默认超时等待时间（秒）
     */
    int defaultTimeoutDurationInSeconds() default 5;

    /**
     * 调用方限流配置
     * 格式：caller1=limitForPeriod,limitRefreshPeriodInSeconds,timeoutDurationInSeconds;caller2=...
     * 示例：mobile=100,1,5;web=50,1,5;admin=1000,1,10
     */
    String callerConfigs() default "";

    /**
     * 从方法参数中获取调用方标识的参数名
     * 如果设置此项，则从方法参数中获取调用方标识，而不是从 HTTP Header 中获取
     */
    String callerParamName() default "";

    /**
     * 是否启用限流
     */
    boolean enabled() default true;

    /**
     * 回退方法名称
     * 当限流触发时调用此方法
     */
    String fallbackMethod() default "";
}
