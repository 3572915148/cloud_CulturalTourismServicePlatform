package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.dto.PasswordChangeDTO;
import com.jingdezhen.tourism.dto.UserLoginDTO;
import com.jingdezhen.tourism.dto.UserRegisterDTO;
import com.jingdezhen.tourism.dto.UserUpdateDTO;
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
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateDTO dto) {
        // 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        updateIfPresent(dto.getNickname(), user::setNickname);
        updateIfPresent(dto.getEmail(), user::setEmail);
        updateIfPresent(dto.getPhone(), user::setPhone);
        updateIfPresent(dto.getAvatar(), user::setAvatar);
        updateIfPresent(dto.getIntroduction(), user::setIntroduction);
        updateIfPresent(dto.getGender(), user::setGender);
        updateBirthdayIfPresent(dto.getBirthday(), user);

        // 保存更新
        userMapper.updateById(user);

        // 返回更新后的用户信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 如果值不为空，则更新字段
     */
    private void updateIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    /**
     * 如果值不为空，则更新字段（Integer类型）
     */
    private void updateIfPresent(Integer value, java.util.function.Consumer<Integer> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * 如果生日不为空，则解析并更新
     */
    private void updateBirthdayIfPresent(String birthday, User user) {
        if (!StringUtils.hasText(birthday)) {
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            user.setBirthday(LocalDate.parse(birthday, formatter));
        } catch (Exception e) {
            throw new BusinessException("生日格式不正确，请使用yyyy-MM-dd格式");
        }
    }

    @Override
    public void changePassword(Long userId, PasswordChangeDTO dto) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证手机号（安全验证）
        if (!StringUtils.hasText(user.getPhone())) {
            throw new BusinessException("您的账号未绑定手机号，无法修改密码");
        }
        if (!user.getPhone().equals(dto.getPhone())) {
            throw new BusinessException("手机号验证失败，请输入正确的手机号");
        }

        // 直接更新密码（通过手机号验证身份，不需要原密码）
        user.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void deleteAccount(Long userId) {
        // 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 逻辑删除用户（MyBatis Plus会自动处理）
        userMapper.deleteById(userId);
    }
}

