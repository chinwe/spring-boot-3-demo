package com.example.demo.service;

import java.util.concurrent.atomic.AtomicInteger;

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
               backoff = @Backoff(delay = 1000),
               recover="recoverFromBasicRetry")
    public String basicRetryExample(boolean shouldSucceed) {
        int attempt = attemptCounter.incrementAndGet();
        log.info("Basic retry example - attempt {}", attempt);
        
        if (!shouldSucceed) {
            throw new TemporaryException("Simulated temporary exception - attempt " + attempt);
        }
        
        attemptCounter.set(0); // 重置计数器
        return "Basic retry succeeded! Total attempts: " + attempt;
    }

    /**
     * 基本重试的恢复方法
     */
    @Recover
    public String recoverFromBasicRetry(TemporaryException ex) {
        log.error("Basic retry finally failed, executing recovery logic: {}", ex.getMessage());
        attemptCounter.set(0); // 重置计数器
        return "Recovery result after basic retry failure";
    }

    /**
     * 使用自定义本地重试注解的示例
     */
    @LocalRetryable(retryFor = TemporaryException.class, recover = "recoverFromLocalRetry")
    public String localServiceCall(boolean shouldSucceed) {
        int attempt = localCounter.incrementAndGet();
        log.info("Local service call - attempt {}", attempt);

        if (!shouldSucceed) {
            throw new TemporaryException("Local service temporarily unavailable - attempt " + attempt);
        }

        localCounter.set(0); // 重置计数器
        return "Local service call succeeded! Total attempts: " + attempt;
    }

    /**
     * 本地重试的恢复方法
     */
    @Recover
    public String recoverFromLocalRetry(TemporaryException ex, boolean shouldSucceed) {
        log.error("Local service call finally failed, executing recovery logic: {}", ex.getMessage());
        localCounter.set(0); // 重置计数器
        return "Recovery result after local service call failure";
    }

    /**
     * 使用自定义远程重试注解的示例
     */
    @RemoteRetryable(retryFor = NetworkException.class, recover = "recoverFromRemoteRetry")
    public String remoteServiceCall(boolean shouldSucceed) {
        int attempt = remoteCounter.incrementAndGet();
        log.info("Remote service call - attempt {}", attempt);

        if (!shouldSucceed) {
            throw new NetworkException("Network connection timeout - attempt " + attempt);
        }

        remoteCounter.set(0); // 重置计数器
        return "Remote service call succeeded! Total attempts: " + attempt;
    }

    /**
     * 远程重试的恢复方法
     */
    @Recover
    public String recoverFromRemoteRetry(NetworkException ex, boolean shouldSucceed) {
        log.error("Remote service call finally failed, executing recovery logic: {}", ex.getMessage());
        remoteCounter.set(0); // 重置计数器
        return "Recovery result after remote service call failure";
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
        log.info("Conditional retry example - attempt {}, exception type: {}", attempt, exceptionType);
        
        switch (exceptionType) {
            case "temporary":
                if (attempt < 3) {
                    throw new TemporaryException("Temporary exception - attempt " + attempt);
                }
                break;
            case "network":
                if (attempt < 3) {
                    throw new NetworkException("Network exception - attempt " + attempt);
                }
                break;
            case "business":
                throw new BusinessException("Business exception - should not retry");
            default:
                break;
        }
        
        networkCounter.set(0); // 重置计数器
        return "Conditional retry succeeded! Total attempts: " + attempt;
    }

    /**
     * 条件重试的恢复方法
     */
    @Recover
    public String recoverFromConditionalRetry(Exception ex, String exceptionType) {
        log.error("Conditional retry finally failed, exception type: {}, executing recovery logic: {}", exceptionType, ex.getMessage());
        networkCounter.set(0); // 重置计数器

        // 业务异常不应该被恢复，重新抛出
        if (ex instanceof BusinessException) {
            throw (BusinessException) ex;
        }

        return "Recovery result after conditional retry failure, exception type: " + exceptionType;
    }

    /**
     * 编程式重试示例 - 使用RetryTemplate
     */
    public String imperativeRetryExample(boolean shouldSucceed) throws Exception {
        return retryTemplate.execute(
            // RetryCallback - 执行的业务逻辑
            (RetryCallback<String, Exception>) context -> {
                int attempt = (int) context.getRetryCount() + 1;
                log.info("Imperative retry example - attempt {}", attempt);

                if (!shouldSucceed) {
                    throw new TemporaryException("Imperative retry temporary exception - attempt " + attempt);
                }

                return "Imperative retry succeeded! Total attempts: " + attempt;
            },
            // RecoveryCallback - 重试失败后的恢复逻辑
            (RecoveryCallback<String>) context -> {
                log.error("Imperative retry finally failed, executing recovery logic");
                return "Recovery result after imperative retry failure";
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
        log.info("SpEL retry example - attempt {}, priority: {}, max attempts: {}", attempt, priority, maxAttempts);

        if (!shouldSucceed) {
            throw new TemporaryException("SpEL retry exception - attempt " + attempt + ", priority: " + priority);
        }

        attemptCounter.set(0); // 重置计数器
        return "SpEL retry succeeded! Priority: " + priority + ", total attempts: " + attempt;
    }

    /**
     * SpEL重试的恢复方法
     */
    @Recover
    public String recoverFromSpelRetry(TemporaryException ex, boolean shouldSucceed, String priority) {
        log.error("SpEL retry finally failed, priority: {}, executing recovery logic: {}", priority, ex.getMessage());
        attemptCounter.set(0); // 重置计数器
        return "Recovery result after SpEL retry failure, priority: " + priority;
    }

    /**
     * 重置所有计数器
     */
    public void resetCounters() {
        attemptCounter.set(0);
        localCounter.set(0);
        remoteCounter.set(0);
        networkCounter.set(0);
        log.info("All counters have been reset");
    }
}