package com.jingdezhen.tourism.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Redis配置类
 * 统一管理Redis相关配置和操作，实现解耦
 * 
 * @author AI Assistant
 */
@Slf4j
@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisConfig {
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired
    private Environment environment;
    
    @Value("${spring.data.redis.host:}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:-1}")
    private int redisPort;
    
    /**
     * Redis Key前缀常量
     */
    public static class KeyPrefix {
        /** 会话前缀 */
        public static final String SESSION = "agent:session:";
        /** 推荐记录映射前缀 */
        public static final String RECOMMENDATION_SESSION_MAPPING = "recommendation:session:";
        /** 连接测试前缀 */
        public static final String CONNECTION_TEST = "redis:connection:test:";
        /** 空值缓存前缀 */
        public static final String NULL_CACHE = "null:cache:";
        /** 分布式锁前缀 */
        public static final String LOCK = "lock:";
    }
    
    /**
     * Redis过期时间常量（秒）
     */
    public static class ExpireTime {
        /** 会话超时时间：30分钟 */
        public static final long SESSION_TIMEOUT = 30 * 60;
        /** 推荐记录映射过期时间：7天 */
        public static final long RECOMMENDATION_MAPPING = 7 * 24 * 60 * 60;
        /** 连接测试过期时间：10秒 */
        public static final long CONNECTION_TEST = 10;
        /** 空值缓存过期时间：5分钟 */
        public static final long NULL_CACHE = 5 * 60;
        /** 分布式锁过期时间：10秒 */
        public static final long LOCK = 10;
    }
    
    /**
     * 初始化后检查Redis连接
     */
    @PostConstruct
    public void init() {
        if (stringRedisTemplate == null) {
            log.warn("═══════════════════════════════════════════════════════════");
            log.warn("⚠️ StringRedisTemplate未注入，Redis功能将不可用");
            log.warn("   可能原因：");
            log.warn("   1. application.yml中Redis配置被注释或不存在");
            log.warn("   2. pom.xml中未添加spring-boot-starter-data-redis依赖");
            log.warn("   3. Redis自动配置被禁用");
            log.warn("═══════════════════════════════════════════════════════════");
            return;
        }
        
        // 显示配置信息
        logRedisConfig();
        
        // 测试连接
        testConnection();
    }
    
    /**
     * 显示Redis配置信息
     */
    private void logRedisConfig() {
        String actualHost = environment.getProperty("spring.data.redis.host", "localhost");
        String actualPort = environment.getProperty("spring.data.redis.port", "6379");
        String actualPassword = environment.getProperty("spring.data.redis.password");
        String hasPassword = actualPassword != null ? "已设置" : "未设置";
        String passwordDisplay = actualPassword != null && !actualPassword.isEmpty() 
            ? (actualPassword.length() > 4 ? actualPassword.substring(0, 2) + "***" + actualPassword.substring(actualPassword.length() - 1) : "***")
            : "未设置";
        String actualDatabase = environment.getProperty("spring.data.redis.database", "0");
        
        log.info("═══════════════════════════════════════════════════════════");
        log.info("📋 Redis配置信息：");
        log.info("   host: {}", actualHost);
        log.info("   port: {}", actualPort);
        log.info("   password: {} ({})", hasPassword, passwordDisplay);
        log.info("   database: {}", actualDatabase);
        log.info("═══════════════════════════════════════════════════════════");
    }
    
    /**
     * 测试Redis连接
     */
    private void testConnection() {
        try {
            // 先关闭并重置连接，确保使用最新的配置
            if (redisConnectionFactory != null) {
                try {
                    var connection = redisConnectionFactory.getConnection();
                    if (connection != null) {
                        connection.close();
                        log.info("🔄 已测试连接工厂，将使用最新配置重新连接");
                    }
                } catch (Exception e) {
                    log.debug("测试连接工厂时出现异常（将在后续测试中验证）: {}", e.getMessage());
                }
            }
            
            String testKey = KeyPrefix.CONNECTION_TEST + System.currentTimeMillis();
            String testValue = "test";
            
            log.info("🔍 开始测试Redis连接（使用最新配置）...");
            
            // 测试写入
            stringRedisTemplate.opsForValue().set(testKey, testValue, ExpireTime.CONNECTION_TEST, TimeUnit.SECONDS);
            log.info("✅ Redis写入测试成功");
            
            // 测试读取
            String readValue = stringRedisTemplate.opsForValue().get(testKey);
            if (testValue.equals(readValue)) {
                log.info("✅ Redis读取测试成功");
            } else {
                log.warn("⚠️ Redis读取测试异常：期望值={}, 实际值={}", testValue, readValue);
            }
            
            // 测试过期时间
            Long ttl = stringRedisTemplate.getExpire(testKey, TimeUnit.SECONDS);
            if (ttl != null && ttl > 0) {
                log.info("✅ Redis过期时间设置测试成功，剩余时间={}秒", ttl);
            }
            
            // 清理测试数据
            stringRedisTemplate.delete(testKey);
            log.info("✅ Redis删除操作测试成功");
            
            // 测试PING
            if (redisConnectionFactory != null) {
                try {
                    redisConnectionFactory.getConnection().ping();
                    log.info("✅ Redis PING测试成功，连接正常");
                } catch (Exception e) {
                    log.warn("⚠️ Redis PING测试失败: {}", e.getMessage());
                }
            }
            
            log.info("═══════════════════════════════════════════════════════════");
            log.info("✅ Redis连接检查完成，所有测试通过！");
            log.info("═══════════════════════════════════════════════════════════");
            
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            log.error("═══════════════════════════════════════════════════════════");
            log.error("❌ Redis连接失败");
            log.error("   错误信息: {}", e.getMessage());
            log.error("   可能的原因：");
            log.error("   1. Redis服务未启动（请检查Redis服务状态）");
            log.error("   2. Redis连接配置错误（请检查application.yml中的spring.data.redis配置）");
            log.error("   3. Redis密码错误（请检查password配置是否正确）");
            log.error("   4. 网络连接问题（请检查host和port配置）");
            log.error("   5. 防火墙阻止连接");
            log.error("═══════════════════════════════════════════════════════════");
            log.error("⚠️ 应用将继续启动，但Redis功能将不可用");
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            boolean isAuthError = errorMessage.contains("auth") || 
                                 errorMessage.contains("password") || 
                                 errorMessage.contains("authentication") ||
                                 errorMessage.contains("noauth");
            
            if (isAuthError) {
                log.error("═══════════════════════════════════════════════════════════");
                log.error("❌ Redis连接失败");
                log.error("   错误信息: {}", e.getMessage());
                log.error("   原因：Redis认证失败（密码错误或未设置密码）");
                log.error("   请检查application.yml中的spring.data.redis.password配置");
                log.error("═══════════════════════════════════════════════════════════");
            } else {
                log.error("═══════════════════════════════════════════════════════════");
                log.error("❌ Redis连接失败");
                log.error("   错误类型: {}", e.getClass().getSimpleName());
                log.error("   错误信息: {}", e.getMessage());
                log.error("═══════════════════════════════════════════════════════════");
            }
            log.error("⚠️ 应用将继续启动，但Redis功能将不可用");
        }
    }
    
    /**
     * 检查Redis是否可用
     * 
     * @return true表示Redis可用，false表示不可用
     */
    public boolean isRedisAvailable() {
        return stringRedisTemplate != null;
    }
    
    /**
     * 获取StringRedisTemplate
     * 注意：可能返回null，使用前需要检查
     * 
     * @return StringRedisTemplate，如果Redis未配置则返回null
     */
    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }
    
    /**
     * 获取Redis连接工厂
     * 
     * @return RedisConnectionFactory，如果Redis未配置则返回null
     */
    public RedisConnectionFactory getRedisConnectionFactory() {
        return redisConnectionFactory;
    }
}

