package com.jingdezhen.tourism.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户VO
 * @author shirenan
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private LocalDate birthday;

    private String introduction;

    private Integer status;

    private LocalDateTime createTime;

    private String token;
}

