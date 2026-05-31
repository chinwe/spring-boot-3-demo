package com.example.demo.logging.controller;

import com.example.demo.logging.desensitize.model.DesensitizeConfig;
import com.example.demo.logging.desensitize.model.DesensitizeRule;
import com.example.demo.logging.desensitize.model.DesensitizeType;
import com.example.demo.logging.dto.DesensitizeTestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LoggingTestController 单元测试
 * 覆盖所有日志脱敏测试端点
 */
@WebMvcTest(LoggingTestController.class)
class LoggingTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DesensitizeConfig desensitizeConfig;

    // ==================== 邮箱脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/email — 邮箱脱敏")
    void testEmail_returnsDesensitizeResponse() throws Exception {
        mockMvc.perform(post("/api/logging/test/email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.original").value("test@example.com"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 手机号脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/phone — 手机号脱敏")
    void testPhone_returnsDesensitizeResponse() throws Exception {
        mockMvc.perform(post("/api/logging/test/phone")
                        .param("phone", "13812345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PHONE"))
                .andExpect(jsonPath("$.original").value("13812345678"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 身份证脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/idcard — 身份证脱敏")
    void testIdCard_returnsDesensitizeResponse() throws Exception {
        mockMvc.perform(post("/api/logging/test/idcard")
                        .param("idCard", "110101199001011234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ID_CARD"))
                .andExpect(jsonPath("$.original").value("110101199001011234"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 银行卡脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/bankcard — 银行卡脱敏")
    void testBankCard_returnsDesensitizeResponse() throws Exception {
        mockMvc.perform(post("/api/logging/test/bankcard")
                        .param("bankCard", "6222021234567890123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("BANK_CARD"))
                .andExpect(jsonPath("$.original").value("6222021234567890123"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 密码脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/password — 密码脱敏")
    void testPassword_returnsDesensitizeResponse() throws Exception {
        mockMvc.perform(post("/api/logging/test/password")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PASSWORD"))
                .andExpect(jsonPath("$.original").value("secret123"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 地址脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/address — 地址脱敏")
    void testAddress_returnsDesensitizeResponse() throws Exception {
        mockMvc.perform(post("/api/logging/test/address")
                        .param("address", "北京市朝阳区建国路88号"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ADDRESS"))
                .andExpect(jsonPath("$.original").value("北京市朝阳区建国路88号"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 键值对脱敏测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/keyvalue — 键值对脱敏")
    void testKeyValue_returnsDesensitizeResponse() throws Exception {
        DesensitizeTestRequest request = DesensitizeTestRequest.builder()
                .username("admin")
                .password("secret123")
                .apiKey("abc123")
                .secret("xyz789")
                .accessToken("token-abc")
                .refreshToken("token-xyz")
                .build();

        mockMvc.perform(post("/api/logging/test/keyvalue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("KEY_VALUE"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 综合测试 ====================

    @Nested
    @DisplayName("综合测试")
    class TestAll {

        @Test
        @DisplayName("POST /api/logging/test/all — 综合脱敏测试")
        void testAll_returnsComprehensiveResult() throws Exception {
            DesensitizeTestRequest request = DesensitizeTestRequest.builder()
                    .email("test@example.com")
                    .phone("13812345678")
                    .idCard("110101199001011234")
                    .bankCard("6222021234567890123")
                    .password("secret123")
                    .address("北京市朝阳区建国路88号")
                    .username("admin")
                    .apiKey("abc123")
                    .secret("xyz789")
                    .build();

            mockMvc.perform(post("/api/logging/test/all")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("All sensitive data logged"))
                    .andExpect(jsonPath("$.instruction").exists())
                    .andExpect(jsonPath("$.types").isArray())
                    .andExpect(jsonPath("$.types.length()").value(7));
        }
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("POST /api/logging/test/performance — 性能测试（使用默认迭代次数）")
    void testPerformance_withDefaultIterations_returnsResult() throws Exception {
        mockMvc.perform(post("/api/logging/test/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iterations").value(1000))
                .andExpect(jsonPath("$.totalTimeMs").exists())
                .andExpect(jsonPath("$.avgTimePerLogUs").exists())
                .andExpect(jsonPath("$.logsPerSecond").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("POST /api/logging/test/performance — 性能测试（自定义迭代次数）")
    void testPerformance_withCustomIterations_returnsResult() throws Exception {
        mockMvc.perform(post("/api/logging/test/performance")
                        .param("iterations", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iterations").value(100))
                .andExpect(jsonPath("$.totalTimeMs").exists())
                .andExpect(jsonPath("$.avgTimePerLogUs").exists());
    }

    // ==================== 状态查询测试 ====================

    @Nested
    @DisplayName("状态查询")
    class StatusTest {

        @Test
        @DisplayName("GET /api/logging/test/status — 获取脱敏规则状态")
        void getStatus_withRules_returnsStatus() throws Exception {
            // Given
            DesensitizeRule emailRule = new DesensitizeRule();
            emailRule.setType(DesensitizeType.EMAIL);
            emailRule.setEnabled(true);

            DesensitizeRule phoneRule = new DesensitizeRule();
            phoneRule.setType(DesensitizeType.PHONE);
            phoneRule.setEnabled(false);

            when(desensitizeConfig.getRules()).thenReturn(List.of(emailRule, phoneRule));

            // When & Then
            mockMvc.perform(get("/api/logging/test/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emailEnabled").value(true))
                    .andExpect(jsonPath("$.phoneEnabled").value(false))
                    .andExpect(jsonPath("$.logFramework").value("Log4j2"))
                    .andExpect(jsonPath("$.configFile").value("log-desensitize.yml"));
        }

        @Test
        @DisplayName("GET /api/logging/test/status — 配置为空时返回基础信息")
        void getStatus_withNullConfig_returnsBasicInfo() throws Exception {
            when(desensitizeConfig.getRules()).thenReturn(null);

            mockMvc.perform(get("/api/logging/test/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.logFramework").value("Log4j2"))
                    .andExpect(jsonPath("$.configFile").value("log-desensitize.yml"));
        }
    }
}
