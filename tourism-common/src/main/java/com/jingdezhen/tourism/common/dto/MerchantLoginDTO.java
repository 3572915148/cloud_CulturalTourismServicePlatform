package com.jingdezhen.tourism.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 商户登录DTO
 */
@Data
public class MerchantLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

