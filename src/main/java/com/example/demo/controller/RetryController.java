package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RetryService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Retry示例控制器
 * 提供各种重试场景的测试接口
 */
@Slf4j
@RestController
@RequestMapping("/retry")
public class RetryController {

    @Resource
    private RetryService retryService;

    @GetMapping("/basic")
    public String basicRetry(@RequestParam(defaultValue = "false") boolean shouldSucceed) {
        log.info("调用基本重试示例，shouldSucceed: {}", shouldSucceed);
        try {
            return retryService.basicRetryExample(shouldSucceed);
        } catch (Exception e) {
            log.error("基本重试示例执行失败", e);
            return "基本重试示例执行失败: " + e.getMessage();
        }
    }

    @GetMapping("/local")
    public String localRetry(@RequestParam(defaultValue = "false") boolean shouldSucceed) {
        log.info("调用本地服务重试示例，shouldSucceed: {}", shouldSucceed);
        try {
            return retryService.localServiceCall(shouldSucceed);
        } catch (Exception e) {
            log.error("本地服务重试示例执行失败", e);
            return "本地服务重试示例执行失败: " + e.getMessage();
        }
    }

    @GetMapping("/remote")
    public String remoteRetry(@RequestParam(defaultValue = "false") boolean shouldSucceed) {
        log.info("调用远程服务重试示例，shouldSucceed: {}", shouldSucceed);
        try {
            return retryService.remoteServiceCall(shouldSucceed);
        } catch (Exception e) {
            log.error("远程服务重试示例执行失败", e);
            return "远程服务重试示例执行失败: " + e.getMessage();
        }
    }

    @GetMapping("/conditional")
    public String conditionalRetry(@RequestParam(defaultValue = "temporary") String exceptionType) {
        log.info("调用条件重试示例，exceptionType: {}", exceptionType);
        try {
            return retryService.conditionalRetryExample(exceptionType);
        } catch (Exception e) {
            log.error("条件重试示例执行失败", e);
            return "条件重试示例执行失败: " + e.getMessage();
        }
    }

    @GetMapping("/imperative")
    public String imperativeRetry(@RequestParam(defaultValue = "false") boolean shouldSucceed) {
        log.info("调用编程式重试示例，shouldSucceed: {}", shouldSucceed);
        try {
            return retryService.imperativeRetryExample(shouldSucceed);
        } catch (Exception e) {
            log.error("编程式重试示例执行失败", e);
            return "编程式重试示例执行失败: " + e.getMessage();
        }
    }

    @GetMapping("/spel")
    public String spelRetry(
            @RequestParam(defaultValue = "false") boolean shouldSucceed,
            @RequestParam(defaultValue = "normal") String priority) {
        log.info("调用SpEL重试示例，shouldSucceed: {}, priority: {}", shouldSucceed, priority);
        try {
            return retryService.spelRetryExample(shouldSucceed, priority);
        } catch (Exception e) {
            log.error("SpEL重试示例执行失败", e);
            return "SpEL重试示例执行失败: " + e.getMessage();
        }
    }

    @PostMapping("/reset")
    public String resetCounters() {
        log.info("重置所有计数器");
        retryService.resetCounters();
        return "所有计数器已重置";
    }

    @GetMapping("/all-examples")
    public String allExamples() {
        log.info("执行所有重试示例");
        StringBuilder result = new StringBuilder();
        
        try {
            // 重置计数器
            retryService.resetCounters();
            
            result.append("=== Spring Retry 示例结果 ===\n\n");
            
            // 1. 基本重试示例
            result.append("1. 基本重试示例（成功）: ");
            result.append(retryService.basicRetryExample(true)).append("\n");
            
            // 2. 本地服务重试示例
            result.append("2. 本地服务重试示例（成功）: ");
            result.append(retryService.localServiceCall(true)).append("\n");
            
            // 3. 远程服务重试示例
            result.append("3. 远程服务重试示例（成功）: ");
            result.append(retryService.remoteServiceCall(true)).append("\n");
            
            // 4. 条件重试示例
            result.append("4. 条件重试示例（临时异常）: ");
            result.append(retryService.conditionalRetryExample("temporary")).append("\n");
            
            // 5. 编程式重试示例
            result.append("5. 编程式重试示例（成功）: ");
            result.append(retryService.imperativeRetryExample(true)).append("\n");
            
            // 6. SpEL重试示例
            result.append("6. SpEL重试示例（普通优先级）: ");
            result.append(retryService.spelRetryExample(true, "normal")).append("\n");
            
            result.append("\n=== 所有示例执行完成 ===");
            
        } catch (Exception e) {
            log.error("执行所有示例时发生错误", e);
            result.append("执行过程中发生错误: ").append(e.getMessage());
        }
        
        return result.toString();
    }
}