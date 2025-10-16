package com.jingdezhen.tourism.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 商户登录DTO
 */
@Data
public class MerchantLoginDTO {

    /**
     * 商户账号
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

