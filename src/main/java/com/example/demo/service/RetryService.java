package com.example.demo.service;

import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.annotation.LocalRetryable;
import com.example.demo.annotation.RemoteRetryable;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.NetworkException;
import com.example.demo.exception.TemporaryException;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Retry示例服务类
 * 展示不同的重试场景和配置方式
 */
@Slf4j
@Service
public class RetryService {

    @Resource
    private RetryTemplate retryTemplate;

    // 用于模拟调用次数的计数器
    private final AtomicInteger attemptCounter = new AtomicInteger(0);
    private final AtomicInteger localCounter = new AtomicInteger(0);
    private final AtomicInteger remoteCounter = new AtomicInteger(0);
    private final AtomicInteger networkCounter = new AtomicInteger(0);

    /**
     * 基本的声明式重试示例
     * 最多重试3次，延迟1秒，对TemporaryException进行重试
     */
    @Retryable(retryFor = TemporaryException.class, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000))
    public String basicRetryExample(boolean shouldSucceed) {
        int attempt = attemptCounter.incrementAndGet();
        log.info("基本重试示例 - 第{}次尝试", attempt);
        
        if (!shouldSucceed && attempt < 3) {
            throw new TemporaryException("模拟临时异常 - 第" + attempt + "次尝试");
        }
        
        attemptCounter.set(0); // 重置计数器
        return "基本重试成功！总共尝试了" + attempt + "次";
    }

    /**
     * 基本重试的恢复方法
     */
    @Recover
    public String recoverFromBasicRetry(TemporaryException ex) {
        log.error("基本重试最终失败，执行恢复逻辑: {}", ex.getMessage());
        attemptCounter.set(0); // 重置计数器
        return "基本重试失败后的恢复结果";
    }

    /**
     * 使用自定义本地重试注解的示例
     */
    @LocalRetryable(retryFor = TemporaryException.class, recover = "recoverFromLocalRetry")
    public String localServiceCall(boolean shouldSucceed) {
        int attempt = localCounter.incrementAndGet();
        log.info("本地服务调用 - 第{}次尝试", attempt);
        
        if (!shouldSucceed && attempt < 3) {
            throw new TemporaryException("本地服务临时不可用 - 第" + attempt + "次尝试");
        }
        
        localCounter.set(0); // 重置计数器
        return "本地服务调用成功！总共尝试了" + attempt + "次";
    }

    /**
     * 本地重试的恢复方法
     */
    @Recover
    public String recoverFromLocalRetry(TemporaryException ex, boolean shouldSucceed) {
        log.error("本地服务调用最终失败，执行恢复逻辑: {}", ex.getMessage());
        localCounter.set(0); // 重置计数器
        return "本地服务调用失败后的恢复结果";
    }

    /**
     * 使用自定义远程重试注解的示例
     */
    @RemoteRetryable(retryFor = NetworkException.class, recover = "recoverFromRemoteRetry")
    public String remoteServiceCall(boolean shouldSucceed) {
        int attempt = remoteCounter.incrementAndGet();
        log.info("远程服务调用 - 第{}次尝试", attempt);
        
        if (!shouldSucceed && attempt < 4) {
            throw new NetworkException("网络连接超时 - 第" + attempt + "次尝试");
        }
        
        remoteCounter.set(0); // 重置计数器
        return "远程服务调用成功！总共尝试了" + attempt + "次";
    }

    /**
     * 远程重试的恢复方法
     */
    @Recover
    public String recoverFromRemoteRetry(NetworkException ex, boolean shouldSucceed) {
        log.error("远程服务调用最终失败，执行恢复逻辑: {}", ex.getMessage());
        remoteCounter.set(0); // 重置计数器
        return "远程服务调用失败后的恢复结果";
    }

    /**
     * 条件重试示例 - 只对特定异常重试，对业务异常不重试
     */
    @Retryable(retryFor = {TemporaryException.class, NetworkException.class},
               noRetryFor = BusinessException.class,
               maxAttempts = 3,
               backoff = @Backoff(delay = 500, maxDelay = 5000, multiplier = 2.0))
    public String conditionalRetryExample(String exceptionType) {
        int attempt = networkCounter.incrementAndGet();
        log.info("条件重试示例 - 第{}次尝试，异常类型: {}", attempt, exceptionType);
        
        switch (exceptionType) {
            case "temporary":
                if (attempt < 3) {
                    throw new TemporaryException("临时异常 - 第" + attempt + "次尝试");
                }
                break;
            case "network":
                if (attempt < 3) {
                    throw new NetworkException("网络异常 - 第" + attempt + "次尝试");
                }
                break;
            case "business":
                throw new BusinessException("业务异常 - 不应该重试");
            default:
                break;
        }
        
        networkCounter.set(0); // 重置计数器
        return "条件重试成功！总共尝试了" + attempt + "次";
    }

    /**
     * 条件重试的恢复方法
     */
    @Recover
    public String recoverFromConditionalRetry(Exception ex, String exceptionType) {
        log.error("条件重试最终失败，异常类型: {}，执行恢复逻辑: {}", exceptionType, ex.getMessage());
        networkCounter.set(0); // 重置计数器
        return "条件重试失败后的恢复结果，异常类型: " + exceptionType;
    }

    /**
     * 编程式重试示例 - 使用RetryTemplate
     */
    public String imperativeRetryExample(boolean shouldSucceed) {
        return retryTemplate.execute(
            // RetryCallback - 执行的业务逻辑
            (RetryCallback<String, Exception>) context -> {
                int attempt = (int) context.getRetryCount() + 1;
                log.info("编程式重试示例 - 第{}次尝试", attempt);
                
                if (!shouldSucceed && attempt < 3) {
                    throw new TemporaryException("编程式重试临时异常 - 第" + attempt + "次尝试");
                }
                
                return "编程式重试成功！总共尝试了" + attempt + "次";
            },
            // RecoveryCallback - 重试失败后的恢复逻辑
            (RecoveryCallback<String>) context -> {
                log.error("编程式重试最终失败，执行恢复逻辑");
                return "编程式重试失败后的恢复结果";
            }
        );
    }

    /**
     * SpEL表达式重试示例
     * 根据传入参数动态决定重试次数
     */
    @Retryable(maxAttemptsExpression = "args[1] == 'critical' ? 5 : 2",
               retryFor = TemporaryException.class,
               backoff = @Backoff(delayExpression = "#{100}", 
                                  maxDelayExpression = "#{5000}",
                                  multiplierExpression = "#{2.0}"))
    public String spelRetryExample(boolean shouldSucceed, String priority) {
        int maxAttempts = "critical".equals(priority) ? 5 : 2;
        int attempt = attemptCounter.incrementAndGet();
        log.info("SpEL重试示例 - 第{}次尝试，优先级: {}，最大重试次数: {}", attempt, priority, maxAttempts);
        
        if (!shouldSucceed && attempt < maxAttempts) {
            throw new TemporaryException("SpEL重试异常 - 第" + attempt + "次尝试，优先级: " + priority);
        }
        
        attemptCounter.set(0); // 重置计数器
        return "SpEL重试成功！优先级: " + priority + "，总共尝试了" + attempt + "次";
    }

    /**
     * SpEL重试的恢复方法
     */
    @Recover
    public String recoverFromSpelRetry(TemporaryException ex, boolean shouldSucceed, String priority) {
        log.error("SpEL重试最终失败，优先级: {}，执行恢复逻辑: {}", priority, ex.getMessage());
        attemptCounter.set(0); // 重置计数器
        return "SpEL重试失败后的恢复结果，优先级: " + priority;
    }

    /**
     * 重置所有计数器
     */
    public void resetCounters() {
        attemptCounter.set(0);
        localCounter.set(0);
        remoteCounter.set(0);
        networkCounter.set(0);
        log.info("所有计数器已重置");
    }
}