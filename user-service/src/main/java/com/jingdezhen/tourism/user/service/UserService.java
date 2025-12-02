package com.jingdezhen.tourism.user.service;

import com.jingdezhen.tourism.common.dto.PasswordChangeDTO;
import com.jingdezhen.tourism.common.dto.UserLoginDTO;
import com.jingdezhen.tourism.common.dto.UserRegisterDTO;
import com.jingdezhen.tourism.common.dto.UserUpdateDTO;
import com.jingdezhen.tourism.common.entity.User;
import com.jingdezhen.tourism.common.vo.UserVO;

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
     * 更新用户信息（通过DTO）
     */
    UserVO updateUserInfo(Long userId, UserUpdateDTO dto);

    /**
     * 修改密码
     */
    void changePassword(Long userId, PasswordChangeDTO dto);

    /**
     * 注销账号
     */
    void deleteAccount(Long userId);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 根据ID获取用户实体（供其他服务调用）
     */
    User getUserEntityById(Long id);
}

