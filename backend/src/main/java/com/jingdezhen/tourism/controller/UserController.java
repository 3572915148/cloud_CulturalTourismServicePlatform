package com.jingdezhen.tourism.controller;

import com.jingdezhen.tourism.dto.UserLoginDTO;
import com.jingdezhen.tourism.dto.UserRegisterDTO;
import com.jingdezhen.tourism.service.UserService;
import com.jingdezhen.tourism.vo.Result;
import com.jingdezhen.tourism.vo.UserVO;
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
}

