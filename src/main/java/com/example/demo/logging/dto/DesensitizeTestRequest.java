package com.example.demo.logging.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 脱敏测试请求 DTO
 */
@Data
@Builder
public class DesensitizeTestRequest {
    private String email;
    private String phone;
    private String idCard;
    private String bankCard;
    private String password;
    private String address;
    private String username;
    private String apiKey;
    private String secret;
    private String accessToken;
    private String refreshToken;
}
