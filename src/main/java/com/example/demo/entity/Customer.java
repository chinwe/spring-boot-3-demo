package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户实体类
 *
 * @author chinwe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private Address address;

    /**
     * 获取完整姓名
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
