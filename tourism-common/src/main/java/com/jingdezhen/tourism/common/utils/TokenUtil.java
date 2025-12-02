package com.jingdezhen.tourism.common.utils;

import com.jingdezhen.tourism.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Token工具类
 */
@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final JwtUtil jwtUtil;

    /**
     * 从请求头中获取并验证Token，返回用户ID
     * 
     * @param authHeader Authorization请求头
     * @return 用户ID
     * @throws BusinessException 如果Token无效或不存在
     */
    public Long getUserIdFromAuth(String authHeader) {
        // 检查Authorization头是否存在
        if (!StringUtils.hasText(authHeader)) {
            throw new BusinessException("未登录，请先登录");
        }

        // 去掉 "Bearer " 前缀
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 验证Token是否有效
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("登录已过期，请重新登录");
        }

        // 获取用户ID
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new BusinessException("Token解析失败，请重新登录");
        }
    }

    /**
     * 从请求头中获取Token（不验证，允许为空）
     * 用于可选登录的场景
     * 
     * @param authHeader Authorization请求头
     * @return 用户ID，如果未登录返回null
     */
    public Long getUserIdFromAuthOptional(String authHeader) {
        try {
            if (!StringUtils.hasText(authHeader)) {
                return null;
            }

            String token = authHeader;
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            if (!jwtUtil.validateToken(token)) {
                return null;
            }

            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}

