package com.example.demo.circuitbreaker.service;

import com.example.demo.circuitbreaker.dto.ExternalApiRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟外部服务调用
 * 用于测试熔断器、限流器等容错模式
 */
@Service
@Slf4j
public class ExternalApiService {

    /**
     * 模拟调用外部 API
     *
     * @param request 请求参数
     * @return API 响应结果
     * @throws RuntimeException 当 simulateFailure 为 true 时抛出异常
     */
    public String callExternalApi(ExternalApiRequestDto request) {
        log.info("Calling external API: {}", request.getEndpoint());

        // 模拟延迟
        if (request.getDelayMs() != null && request.getDelayMs() > 0) {
            try {
                Thread.sleep(request.getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request interrupted", e);
            }
        }

        // 模拟失败
        if (request.isSimulateFailure()) {
            log.error("Simulated failure for API: {}", request.getEndpoint());
            throw new RuntimeException("Simulated API failure for: " + request.getEndpoint());
        }

        // 模拟随机失败（10% 概率）
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            log.error("Random failure for API: {}", request.getEndpoint());
            throw new RuntimeException("Random API failure for: " + request.getEndpoint());
        }

        String response = String.format("Success response from %s", request.getEndpoint());
        log.info("API call successful: {}", response);
        return response;
    }

    /**
     * 模拟调用慢速 API
     *
     * @param endpoint API 端点
     * @param delayMs 延迟时间（毫秒）
     * @return API 响应结果
     */
    public String callSlowApi(String endpoint, long delayMs) {
        log.info("Calling slow API: {} with delay: {}ms", endpoint, delayMs);

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Slow API call interrupted", e);
        }

        return String.format("Slow API response from %s after %dms", endpoint, delayMs);
    }

    /**
     * 模拟调用不稳定的 API
     * 有 30% 的概率失败
     *
     * @param endpoint API 端点
     * @return API 响应结果
     */
    public String callUnstableApi(String endpoint) {
        log.info("Calling unstable API: {}", endpoint);

        double random = ThreadLocalRandom.current().nextDouble();
        if (random < 0.3) {
            log.error("Unstable API failed: {}", endpoint);
            throw new RuntimeException("Unstable API failure for: " + endpoint);
        }

        return String.format("Unstable API success response from %s", endpoint);
    }

    /**
     * 模拟调用快速 API
     *
     * @param endpoint API 端点
     * @return API 响应结果
     */
    public String callFastApi(String endpoint) {
        log.debug("Calling fast API: {}", endpoint);
        return String.format("Fast API response from %s", endpoint);
    }

    /**
     * 模拟处理业务逻辑
     *
     * @param data 输入数据
     * @return 处理结果
     */
    public String processBusinessLogic(String data) {
        log.info("Processing business logic with data: {}", data);

        // 模拟处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return String.format("Processed: %s", data);
    }
}
