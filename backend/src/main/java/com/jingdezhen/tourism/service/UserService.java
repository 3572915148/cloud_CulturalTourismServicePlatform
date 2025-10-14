package com.jingdezhen.tourism.service;

import com.jingdezhen.tourism.dto.UserLoginDTO;
import com.jingdezhen.tourism.dto.UserRegisterDTO;
import com.jingdezhen.tourism.entity.User;
import com.jingdezhen.tourism.vo.UserVO;

/**
 * 用户Service接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    UserVO login(UserLoginDTO dto);

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
}

