package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.dto.UserLoginDTO;
import com.jingdezhen.tourism.dto.UserRegisterDTO;
import com.jingdezhen.tourism.entity.User;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.UserMapper;
import com.jingdezhen.tourism.service.UserService;
import com.jingdezhen.tourism.utils.JwtUtil;
import com.jingdezhen.tourism.utils.PasswordUtil;
import com.jingdezhen.tourism.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public void register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        User existUser = getUserByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(dto, user);

        // 加密密码
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setStatus(1);

        // 保存用户
        userMapper.insert(user);
    }

    @Override
    public UserVO login(UserLoginDTO dto) {
        // 查询用户
        User user = getUserByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "USER");

        // 构建返回结果
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(token);

        return userVO;
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
}

