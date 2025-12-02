package com.jingdezhen.tourism.user.controller;

import com.jingdezhen.tourism.common.dto.PasswordChangeDTO;
import com.jingdezhen.tourism.common.dto.UserLoginDTO;
import com.jingdezhen.tourism.common.dto.UserRegisterDTO;
import com.jingdezhen.tourism.common.dto.UserUpdateDTO;
import com.jingdezhen.tourism.common.utils.TokenUtil;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.common.vo.UserVO;
import com.jingdezhen.tourism.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户Controller
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenUtil tokenUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Validated @RequestBody UserLoginDTO dto) {
        UserVO userVO = userService.login(dto);
        return Result.success("登录成功", userVO);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserInfo(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 获取用户实体（供其他服务调用）
     */
    @GetMapping("/entity/{id}")
    public Result<com.jingdezhen.tourism.common.entity.User> getUserEntity(@PathVariable Long id) {
        com.jingdezhen.tourism.common.entity.User user = userService.getUserEntityById(id);
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息
     * 需要登录
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo(@RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     * 需要登录
     */
    @PutMapping("/update")
    public Result<UserVO> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @Validated @RequestBody UserUpdateDTO dto) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        UserVO userVO = userService.updateUserInfo(userId, dto);
        return Result.success("更新成功", userVO);
    }

    /**
     * 修改密码
     * 需要登录
     */
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Validated @RequestBody PasswordChangeDTO dto) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        userService.changePassword(userId, dto);
        return Result.success("密码修改成功");
    }

    /**
     * 注销账号
     * 需要登录
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        userService.deleteAccount(userId);
        return Result.success("账号已注销");
    }
}

