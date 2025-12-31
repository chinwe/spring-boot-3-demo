package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址数据传输对象
 *
 * @author chinwe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private String fullAddress;

    private String city;

    private String country;

    private String postalCode;
}
