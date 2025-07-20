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
public class AsyncTaskDto {
    private String taskId;
    private TaskStatus status;
    private Object result;
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    private String errorMessage;

    public enum TaskStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}