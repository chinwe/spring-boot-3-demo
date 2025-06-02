package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author chinw
 */
@AllArgsConstructor
@Data
public class User {
    private Long id;

    private String name;

    private String email;
}
