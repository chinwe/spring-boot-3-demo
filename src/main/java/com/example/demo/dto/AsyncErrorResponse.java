package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncErrorResponse {
    private String taskId;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime timestamp;
}