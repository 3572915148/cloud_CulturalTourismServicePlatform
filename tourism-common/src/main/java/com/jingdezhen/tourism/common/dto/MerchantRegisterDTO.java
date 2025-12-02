package com.jingdezhen.tourism.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 商户注册DTO
 */
@Data
public class MerchantRegisterDTO {

    @NotBlank(message = "商户账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    private String contactEmail;
    private String address;
    private String shopIntroduction;
}

