package com.example.demo.circuitbreaker.service;

import com.example.demo.circuitbreaker.dto.CircuitBreakerResultDto;
import com.example.demo.circuitbreaker.dto.ExternalApiRequestDto;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 熔断器服务单元测试
 */
@SpringBootTest
class CircuitBreakerServiceTest {

    @Autowired
    private CircuitBreakerService circuitBreakerService;

    @MockBean
    private ExternalApiService externalApiService;

    @BeforeEach
    void setUp() {
        // 重置熔断器状态
        io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry registry =
                io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.ofDefaults();
        registry.getAllCircuitBreakers().forEach(cb -> cb.reset());
    }

    @Test
    void testCallExternalApiWithCircuitBreaker_Success() {
        // Given
        ExternalApiRequestDto request = ExternalApiRequestDto.builder()
                .endpoint("/api/users")
                .simulateFailure(false)
                .build();

        when(externalApiService.callExternalApi(any()))
                .thenReturn("Success response");

        // When
        CircuitBreakerResultDto result = circuitBreakerService.callExternalApiWithCircuitBreaker(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResiliencePattern()).isEqualTo("CIRCUIT_BREAKER");
        assertThat(result.getMessage()).contains("Success response");
    }

    @Test
    void testCallExternalApiWithCircuitBreaker_Failure() {
        // Given
        ExternalApiRequestDto request = ExternalApiRequestDto.builder()
                .endpoint("/api/users")
                .simulateFailure(true)
                .build();

        when(externalApiService.callExternalApi(any()))
                .thenThrow(new RuntimeException("Simulated failure"));

        // When
        CircuitBreakerResultDto result = circuitBreakerService.callExternalApiWithCircuitBreaker(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotEmpty();
    }

    @Test
    void testCallApiWithRateLimit() {
        // Given
        String endpoint = "/api/products";

        // When
        CircuitBreakerResultDto result = circuitBreakerService.callApiWithRateLimit(endpoint);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResiliencePattern()).isEqualTo("RATE_LIMITER");
    }

    @Test
    void testCallApiWithBulkhead() {
        // Given
        String endpoint = "/api/orders";

        // When
        CircuitBreakerResultDto result = circuitBreakerService.callApiWithBulkhead(endpoint);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResiliencePattern()).isEqualTo("BULKHEAD");
    }

    @Test
    void testCallApiWithTimeout_Success() throws Exception {
        // Given
        String endpoint = "/api/timeout";
        long delayMs = 100;

        when(externalApiService.callSlowApi(endpoint, delayMs))
                .thenReturn("Slow API response from /api/timeout after 100ms");

        // When
        String result = circuitBreakerService.callApiWithTimeout(endpoint, delayMs).get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("api/timeout");
    }

    @Test
    void testGetCircuitBreakerState() {
        // Given
        String name = "externalApi";

        // When
        String state = circuitBreakerService.getCircuitBreakerState(name);

        // Then
        assertThat(state).isNotNull();
        assertThat(state).isIn("CLOSED", "OPEN", "HALF_OPEN");
    }

    @Test
    void testGetAllCircuitBreakerNames() {
        // Given - 先创建一个熔断器
        String state = circuitBreakerService.getCircuitBreakerState("externalApi");

        // When
        var names = circuitBreakerService.getAllCircuitBreakerNames();

        // Then
        assertThat(names).isNotNull();
        assertThat(names).isNotEmpty();
        assertThat(names).contains("externalApi");
    }

    @Test
    void testGetDefaultRateLimitConfig() {
        // When
        var config = circuitBreakerService.getDefaultRateLimitConfig();

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getCaller()).isEqualTo("default");
        assertThat(config.getLimitForPeriod()).isEqualTo(10);
    }

    @Test
    void testCreateCustomRateLimit() {
        // Given
        String caller = "testCaller";
        int limit = 100;
        int refreshPeriod = 1;
        int timeout = 5;

        // When
        var config = circuitBreakerService.createCustomRateLimit(caller, limit, refreshPeriod, timeout);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getCaller()).isEqualTo(caller);
        assertThat(config.getLimitForPeriod()).isEqualTo(limit);
        assertThat(config.getLimitRefreshPeriodInSeconds()).isEqualTo(refreshPeriod);
        assertThat(config.getTimeoutDurationInSeconds()).isEqualTo(timeout);
    }
}
