package com.jingdezhen.tourism.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Token工具类 - 用于从HTTP请求头中获取用户信息
 */
@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final JwtUtil jwtUtil;

    /**
     * 从Authorization头中获取用户ID
     * @param authorizationHeader Authorization头的值（格式：Bearer token）
     * @return 用户ID
     */
    public Long getUserIdFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 从Authorization头中获取用户名
     * @param authorizationHeader Authorization头的值（格式：Bearer token）
     * @return 用户名
     */
    public String getUsernameFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtUtil.getUsernameFromToken(token);
    }

    /**
     * 从Authorization头中获取用户类型
     * @param authorizationHeader Authorization头的值（格式：Bearer token）
     * @return 用户类型
     */
    public String getUserTypeFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtUtil.getUserTypeFromToken(token);
    }
}

