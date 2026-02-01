package com.example.demo.circuitbreaker.aspect;

import com.example.demo.circuitbreaker.annotation.CallerRateLimiter;
import com.example.demo.circuitbreaker.exception.RateLimitExceededException;
import com.example.demo.circuitbreaker.model.CallerRateLimit;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 X-Caller Header 的差异化限流切面
 * 核心功能：
 * 1. 从 HTTP Header X-Caller 提取调用方标识
 * 2. 为每个调用方创建独立的 RateLimiter 实例
 * 3. 支持通过注解配置差异化限流配额
 */
@Aspect
@Component
@Slf4j
public class CallerRateLimiterAspect {

    private static final String DEFAULT_CALLER = "default";
    private static final String CALLER_HEADER = "X-Caller";

    private final RateLimiterRegistry rateLimiterRegistry;
    private final Map<String, CallerRateLimit> callerRateLimitConfigs = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public CallerRateLimiterAspect(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Around("@annotation(callerRateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, CallerRateLimiter callerRateLimiter) throws Throwable {
        if (!callerRateLimiter.enabled()) {
            return joinPoint.proceed();
        }

        // 获取调用方标识
        String caller = extractCaller(joinPoint, callerRateLimiter);
        log.debug("Processing request from caller: {}", caller);

        // 解析并获取调用方限流配置
        CallerRateLimit rateLimit = getCallerRateLimit(caller, callerRateLimiter);

        // 获取或创建限流器
        RateLimiter rateLimiterInstance = getOrCreateRateLimiter(caller, rateLimit);

        // 尝试获取许可
        boolean permission = rateLimiterInstance.acquirePermission();
        if (!permission) {
            log.warn("Rate limit exceeded for caller: {}, limit: {}", caller, rateLimit.getLimitForPeriod());
            throw new RateLimitExceededException(
                    rateLimiterInstance.getName(),
                    caller,
                    rateLimit.getLimitForPeriod()
            );
        }

        try {
            return joinPoint.proceed();
        } finally {
            log.debug("Request completed for caller: {}", caller);
        }
    }

    /**
     * 提取调用方标识
     * 优先从方法参数获取，其次从 HTTP Header 获取
     */
    private String extractCaller(ProceedingJoinPoint joinPoint, CallerRateLimiter annotation) {
        // 尝试从方法参数获取
        String callerParamName = annotation.callerParamName();
        if (!callerParamName.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] parameterNames = signature.getParameterNames();

            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    if (parameterNames[i].equals(callerParamName)) {
                        Object callerValue = joinPoint.getArgs()[i];
                        if (callerValue != null) {
                            return callerValue.toString();
                        }
                    }
                }
            }
        }

        // 尝试从 HTTP Header 获取
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String caller = request.getHeader(CALLER_HEADER);
            if (caller != null && !caller.isEmpty()) {
                return caller;
            }
        }

        // 返回默认值
        return DEFAULT_CALLER;
    }

    /**
     * 解析注解中的调用方限流配置
     */
    private CallerRateLimit getCallerRateLimit(String caller, CallerRateLimiter annotation) {
        String cacheKey = annotation.prefix() + "_" + caller;

        // 尝试从缓存获取
        CallerRateLimit cached = callerRateLimitConfigs.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 解析 callerConfigs
        Map<String, CallerRateLimit> configs = parseCallerConfigs(annotation.callerConfigs(), annotation);
        callerRateLimitConfigs.putAll(configs);

        // 尝试从解析结果获取
        CallerRateLimit parsed = configs.get(cacheKey);
        if (parsed != null) {
            return parsed;
        }

        // 创建默认配置
        CallerRateLimit defaultConfig = CallerRateLimit.builder()
                .caller(caller)
                .limitForPeriod(annotation.defaultLimitForPeriod())
                .limitRefreshPeriodInSeconds(annotation.defaultLimitRefreshPeriodInSeconds())
                .timeoutDurationInSeconds(annotation.defaultTimeoutDurationInSeconds())
                .build();

        callerRateLimitConfigs.put(cacheKey, defaultConfig);
        return defaultConfig;
    }

    /**
     * 解析调用方配置字符串
     * 格式：caller1=limit,refreshPeriod,timeout;caller2=...
     */
    private Map<String, CallerRateLimit> parseCallerConfigs(String callerConfigs, CallerRateLimiter annotation) {
        Map<String, CallerRateLimit> configs = new HashMap<>();

        if (callerConfigs == null || callerConfigs.isEmpty()) {
            return configs;
        }

        String[] entries = callerConfigs.split(";");
        for (String entry : entries) {
            try {
                String[] parts = entry.split("=");
                if (parts.length != 2) {
                    continue;
                }

                String caller = parts[0].trim();
                String[] values = parts[1].split(",");

                int limitForPeriod = Integer.parseInt(values[0].trim());
                int refreshPeriod = values.length > 1 ? Integer.parseInt(values[1].trim()) : annotation.defaultLimitRefreshPeriodInSeconds();
                int timeout = values.length > 2 ? Integer.parseInt(values[2].trim()) : annotation.defaultTimeoutDurationInSeconds();

                CallerRateLimit config = CallerRateLimit.builder()
                        .caller(caller)
                        .limitForPeriod(limitForPeriod)
                        .limitRefreshPeriodInSeconds(refreshPeriod)
                        .timeoutDurationInSeconds(timeout)
                        .build();

                String cacheKey = annotation.prefix() + "_" + caller;
                configs.put(cacheKey, config);
            } catch (Exception e) {
                log.warn("Failed to parse caller config: {}", entry, e);
            }
        }

        return configs;
    }

    /**
     * 获取或创建限流器实例
     */
    private RateLimiter getOrCreateRateLimiter(String caller, CallerRateLimit rateLimit) {
        String limiterName = rateLimit.getCaller(); // 使用 caller 作为名称的一部分

        return rateLimiters.computeIfAbsent(limiterName, key -> {
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitForPeriod(rateLimit.getLimitForPeriod())
                    .limitRefreshPeriod(rateLimit.getLimitRefreshPeriod())
                    .timeoutDuration(rateLimit.getTimeoutDuration())
                    .build();

            return rateLimiterRegistry.rateLimiter(key, config);
        });
    }

    /**
     * 获取指定调用方的限流器（用于监控）
     */
    public Optional<RateLimiter> getRateLimiter(String caller) {
        return Optional.ofNullable(rateLimiters.get(caller));
    }

    /**
     * 清理指定调用方的限流器
     */
    public void removeRateLimiter(String caller) {
        rateLimiters.remove(caller);
        log.info("Removed rate limiter for caller: {}", caller);
    }

    /**
     * 清理所有限流器
     */
    public void clearAllRateLimiters() {
        rateLimiters.clear();
        callerRateLimitConfigs.clear();
        log.info("Cleared all rate limiters");
    }
}
