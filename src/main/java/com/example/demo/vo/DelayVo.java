package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;

/**
 *
 * @author chinw
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DelayVo {

    @NotNull
    @Min(value = 1)
    @Max(value = 60)
    private Integer second;
}
