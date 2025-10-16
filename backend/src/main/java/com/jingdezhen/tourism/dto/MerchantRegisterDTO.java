package com.jingdezhen.tourism.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 商户注册DTO
 */
@Data
public class MerchantRegisterDTO {

    /**
     * 商户账号（用户名）
     */
    @NotBlank(message = "商户账号不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 店铺地址
     */
    private String address;

    /**
     * 店铺介绍
     */
    private String shopIntroduction;
}

