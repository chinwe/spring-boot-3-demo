package com.example.demo.dto.jooq;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JOOQ 用户 DTO
 *
 * @author chinwe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JooqUserDto {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
