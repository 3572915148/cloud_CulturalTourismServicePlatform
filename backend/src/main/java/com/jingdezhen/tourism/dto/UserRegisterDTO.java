package com.jingdezhen.tourism.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,16}$", message = "用户名必须是4-16位字母、数字或下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    private String password;

    private String nickname;

    private String phone;

    private String email;
}

