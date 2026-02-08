package com.example.demo.logging.controller;

import com.example.demo.logging.dto.DesensitizeTestRequest;
import com.example.demo.logging.dto.DesensitizeTestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志脱敏测试控制器
 * 提供各种脱敏功能的测试 API
 */
@Slf4j
@Tag(name = "日志脱敏测试", description = "测试日志敏感信息脱敏功能")
@RestController
@RequestMapping("/api/logging/test")
public class LoggingTestController {

    /**
     * 测试邮箱脱敏
     */
    @Operation(summary = "测试邮箱脱敏", description = "记录包含邮箱的日志并验证脱敏效果")
    @PostMapping("/email")
    public DesensitizeTestResponse testEmail(@RequestParam String email) {
        log.info("User email: {}", email);
        log.info("Multiple emails: {}, {}, {}", email, "admin@example.com", "test@test.org");
        log.warn("Email warning for user: {}", email);
        log.error("Email error occurred: {}", email);

        return DesensitizeTestResponse.builder()
            .type("EMAIL")
            .original(email)
            .message("Email logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 测试手机号脱敏
     */
    @Operation(summary = "测试手机号脱敏", description = "记录包含手机号的日志并验证脱敏效果")
    @PostMapping("/phone")
    public DesensitizeTestResponse testPhone(@RequestParam String phone) {
        log.info("User phone: {}", phone);
        log.info("Multiple phones: {}, {}, {}", phone, "13812345678", "15987654321");
        log.warn("Phone warning: {}", phone);

        return DesensitizeTestResponse.builder()
            .type("PHONE")
            .original(phone)
            .message("Phone logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 测试身份证号脱敏
     */
    @Operation(summary = "测试身份证脱敏", description = "记录包含身份证号的日志并验证脱敏效果")
    @PostMapping("/idcard")
    public DesensitizeTestResponse testIdCard(@RequestParam String idCard) {
        log.info("User ID card: {}", idCard);
        log.info("ID cards: {}, {}", idCard, "110101199001011234");
        log.warn("ID card verification: {}", idCard);

        return DesensitizeTestResponse.builder()
            .type("ID_CARD")
            .original(idCard)
            .message("ID card logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 测试银行卡号脱敏
     */
    @Operation(summary = "测试银行卡脱敏", description = "记录包含银行卡号的日志并验证脱敏效果")
    @PostMapping("/bankcard")
    public DesensitizeTestResponse testBankCard(@RequestParam String bankCard) {
        log.info("User bank card: {}", bankCard);
        log.info("Bank cards: {}, {}", bankCard, "6222021234567890123");
        log.warn("Bank card payment: {}", bankCard);

        return DesensitizeTestResponse.builder()
            .type("BANK_CARD")
            .original(bankCard)
            .message("Bank card logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 测试密码脱敏（键值对）
     */
    @Operation(summary = "测试密码脱敏", description = "记录包含密码的日志并验证脱敏效果")
    @PostMapping("/password")
    public DesensitizeTestResponse testPassword(@RequestParam String password) {
        log.info("Login credentials: username=admin, password={}", password);
        log.info("API request: apiKey=abc123, token=xyz789");
        log.warn("Password reset requested for: password={}", password);

        return DesensitizeTestResponse.builder()
            .type("PASSWORD")
            .original(password)
            .message("Password logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 测试地址脱敏
     */
    @Operation(summary = "测试地址脱敏", description = "记录包含地址的日志并验证脱敏效果")
    @PostMapping("/address")
    public DesensitizeTestResponse testAddress(@RequestParam String address) {
        log.info("Delivery address: {}", address);
        log.info("Addresses: {}, {}", address, "北京市朝阳区建国路88号");
        log.warn("Address verification: {}", address);

        return DesensitizeTestResponse.builder()
            .type("ADDRESS")
            .original(address)
            .message("Address logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 测试键值对脱敏
     */
    @Operation(summary = "测试键值对脱敏", description = "记录包含敏感键值对的日志并验证脱敏效果")
    @PostMapping("/keyvalue")
    public DesensitizeTestResponse testKeyValue(@RequestBody DesensitizeTestRequest request) {
        log.info("User login: username={}, password={}", request.getUsername(), request.getPassword());
        log.info("API config: apiKey={}, secret={}", request.getApiKey(), request.getSecret());
        log.warn("Token refresh: accessToken={}, refreshToken={}",
            request.getAccessToken(), request.getRefreshToken());

        return DesensitizeTestResponse.builder()
            .type("KEY_VALUE")
            .original(request.toString())
            .message("Key-value logged, check console/log file for desensitized output")
            .build();
    }

    /**
     * 综合测试（包含所有类型的敏感信息）
     */
    @Operation(summary = "综合测试", description = "记录包含所有类型敏感信息的日志")
    @PostMapping("/all")
    public Map<String, Object> testAll(@RequestBody DesensitizeTestRequest request) {
        log.info("=== Comprehensive Desensitization Test ===");
        log.info("Email: {}", request.getEmail());
        log.info("Phone: {}", request.getPhone());
        log.info("ID Card: {}", request.getIdCard());
        log.info("Bank Card: {}", request.getBankCard());
        log.info("Password: {}", request.getPassword());
        log.info("Address: {}", request.getAddress());
        log.info("Login: username={}, password={}", request.getUsername(), request.getPassword());
        log.info("API: apiKey={}, secret={}", request.getApiKey(), request.getSecret());
        log.warn("Sensitive data operation detected");
        log.error("Critical: User credentials exposed in potential breach");
        log.info("=== Test Complete ===");

        Map<String, Object> result = new HashMap<>();
        result.put("message", "All sensitive data logged");
        result.put("instruction", "Check console/log file for desensitized output");
        result.put("types", List.of("EMAIL", "PHONE", "ID_CARD", "BANK_CARD", "PASSWORD", "ADDRESS", "KEY_VALUE"));
        return result;
    }

    /**
     * 性能测试
     */
    @Operation(summary = "性能测试", description = "测试脱敏功能对日志性能的影响")
    @PostMapping("/performance")
    public Map<String, Object> testPerformance(@RequestParam(defaultValue = "1000") int iterations) {
        long startTime = System.nanoTime();
        String testData = "User email: test@example.com, phone: 13812345678, password: secret123";

        for (int i = 0; i < iterations; i++) {
            log.info("Performance test iteration {}: {}", i, testData);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTimeNs = (double) duration / iterations;
        double avgTimeUs = avgTimeNs / 1000;

        Map<String, Object> result = new HashMap<>();
        result.put("iterations", iterations);
        result.put("totalTimeMs", duration / 1_000_000);
        result.put("avgTimePerLogUs", avgTimeUs);
        result.put("logsPerSecond", 1_000_000_000.0 / avgTimeNs);
        result.put("status", avgTimeUs < 100 ? "PASS" : "SLOW");

        return result;
    }

    /**
     * 获取脱敏规则状态
     */
    @Operation(summary = "获取脱敏规则状态", description = "获取当前脱敏规则的启用状态")
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("emailEnabled", true);
        status.put("phoneEnabled", true);
        status.put("idCardEnabled", true);
        status.put("bankCardEnabled", true);
        status.put("passwordEnabled", true);
        status.put("addressEnabled", true);
        status.put("keyValueEnabled", true);
        status.put("logFramework", "Log4j2");
        status.put("configFile", "log-desensitize.yml");
        return status;
    }
}
