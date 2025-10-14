package com.jingdezhen.tourism.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 设置session为无状态
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置访问权限
                .authorizeHttpRequests(auth -> auth
                        // 允许访问的路径（注意：context-path已经是/api，这里不需要再加/api前缀）
                        .requestMatchers(
                                "/user/register",
                                "/user/login",
                                "/user/{id}",
                                "/merchant/register",
                                "/merchant/login",
                                "/admin/login",
                                "/product/**",
                                "/ceramic/**",
                                "/ai/**",
                                "/review/product/**",  // 允许查看产品评价
                                "/error"
                        ).permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

