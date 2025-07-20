package com.example.demo.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskVo {
    
    @NotBlank(message = "Task name is required")
    private String taskName;
    
    @Min(value = 1, message = "Delay must be at least 1 second")
    @Max(value = 30, message = "Delay cannot exceed 30 seconds")
    private Integer delaySeconds;
    
    private Boolean shouldFail = false;
}