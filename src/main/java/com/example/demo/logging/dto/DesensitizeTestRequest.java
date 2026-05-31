package com.example.demo.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 脱敏测试请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
