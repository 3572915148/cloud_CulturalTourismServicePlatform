package com.jingdezhen.tourism.service;

import com.alibaba.fastjson2.JSON;
import com.jingdezhen.tourism.agent.core.ConversationContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis会话管理器
 * 用于管理Agent会话的存储和检索，支持分布式部署
 * 
 * @author AI Assistant
 */
@Slf4j
@Service
public class RedisSessionManager {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private Environment environment;
    
    @Value("${spring.data.redis.host:}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:-1}")
    private int redisPort;
    
    /**
     * Redis Key前缀
     */
    private static final String SESSION_PREFIX = "agent:session:";
    
    /**
     * 会话超时时间（30分钟）
     */
    private static final long SESSION_TIMEOUT_SECONDS = 30 * 60;
    
    /**
     * 初始化后检查Redis连接
     * 在应用启动时执行，确保Redis连接正常
     */
    @PostConstruct
    public void init() {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🔍 开始检查Redis连接状态...");
        log.info("═══════════════════════════════════════════════════════════");
        
        // 1. 检查RedisTemplate是否注入成功
        // 如果RedisTemplate为null，说明Redis自动配置未生效（可能是配置被注释或依赖未添加）
        if (redisTemplate == null) {
            log.warn("⚠️ RedisTemplate未注入");
            log.warn("   可能原因：");
            log.warn("   1. application.yml中Redis配置被注释或不存在");
            log.warn("   2. pom.xml中未添加spring-boot-starter-data-redis依赖");
            log.warn("   3. Redis自动配置被禁用");
            log.warn("═══════════════════════════════════════════════════════════");
            log.warn("⚠️ Redis会话管理功能将不可用");
            log.warn("   如果使用AI Agent功能，会话将无法持久化");
            log.warn("═══════════════════════════════════════════════════════════");
            return; // RedisTemplate不存在，直接返回
        }
        
        // 2. 显示当前Redis配置信息（从Environment读取实际配置值）
        String actualHost = environment.getProperty("spring.data.redis.host", "localhost");
        String actualPort = environment.getProperty("spring.data.redis.port", "6379");
        String hasPassword = environment.getProperty("spring.data.redis.password") != null ? "已设置" : "未设置";
        String actualDatabase = environment.getProperty("spring.data.redis.database", "0");
        
        log.info("📋 当前Redis配置信息：");
        log.info("   host: {}", actualHost);
        log.info("   port: {}", actualPort);
        log.info("   password: {}", hasPassword);
        log.info("   database: {}", actualDatabase);
        log.info("✅ RedisTemplate注入成功");
        
        // 3. 测试Redis连接（执行实际读写操作）
        try {
            String testKey = "redis:connection:test:" + System.currentTimeMillis();
            String testValue = "test";
            
            // 测试写入操作
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            log.info("✅ Redis写入测试成功");
            
            // 测试读取操作
            String readValue = redisTemplate.opsForValue().get(testKey);
            if (testValue.equals(readValue)) {
                log.info("✅ Redis读取测试成功");
            } else {
                log.warn("⚠️ Redis读取测试异常：期望值={}, 实际值={}", testValue, readValue);
            }
            
            // 测试过期时间设置
            Long ttl = redisTemplate.getExpire(testKey, TimeUnit.SECONDS);
            if (ttl != null && ttl > 0) {
                log.info("✅ Redis过期时间设置测试成功，剩余时间={}秒", ttl);
            }
            
            // 清理测试数据
            redisTemplate.delete(testKey);
            log.info("✅ Redis删除操作测试成功");
            
            // 4. 执行PING测试（验证连接）
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                log.info("✅ Redis PING测试成功，连接正常");
            } catch (Exception e) {
                log.warn("⚠️ Redis PING测试失败: {}", e.getMessage());
                // PING失败不影响整体判断，因为前面的读写测试已经验证了连接
            }
            
            log.info("═══════════════════════════════════════════════════════════");
            log.info("✅ Redis连接检查完成，所有测试通过！");
            log.info("   Redis会话管理功能已就绪");
            log.info("   会话过期时间: {}秒 ({}分钟)", 
                SESSION_TIMEOUT_SECONDS, SESSION_TIMEOUT_SECONDS / 60);
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
            log.error("   当前配置：host={}, port={}, password={}", 
                actualHost, actualPort, hasPassword);
            log.error("═══════════════════════════════════════════════════════════");
            log.error("⚠️ 应用将继续启动，但Redis会话管理功能将不可用");
            log.error("   如果使用AI Agent功能，会话将无法持久化");
            // 不抛出异常，允许应用继续启动（但功能会受影响）
        } catch (Exception e) {
            // 检查是否是认证相关的异常
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
                log.error("   当前配置：host={}, port={}, password={}", 
                    actualHost, actualPort, hasPassword);
                log.error("   请检查application.yml中的spring.data.redis.password配置");
                log.error("═══════════════════════════════════════════════════════════");
            } else {
                log.error("═══════════════════════════════════════════════════════════");
                log.error("❌ Redis连接失败");
                log.error("   错误类型: {}", e.getClass().getSimpleName());
                log.error("   错误信息: {}", e.getMessage());
                log.error("   当前配置：host={}, port={}, password={}", 
                    actualHost, actualPort, hasPassword);
                log.error("═══════════════════════════════════════════════════════════");
            }
            log.error("⚠️ 应用将继续启动，但Redis会话管理功能将不可用");
            log.error("   如果使用AI Agent功能，会话将无法持久化");
            // 不抛出异常，允许应用继续启动（但功能会受影响）
        }
    }
    
    
    
    /**
     * 保存会话到Redis
     * 
     * @param sessionId 会话ID
     * @param context 会话上下文
     */
    public void saveSession(String sessionId, ConversationContext context) {
        try {
            // 检查Redis连接和配置
            if (redisTemplate == null) {
                log.warn("⚠️ RedisTemplate未初始化，无法保存会话到Redis: sessionId={}", sessionId);
                log.warn("   可能原因：Redis配置未在application.yml中配置");
                log.warn("   会话将无法持久化，服务器重启后会丢失");
                return; // 不抛出异常，允许应用继续运行
            }
            
            // RedisTemplate存在就说明配置已加载，直接使用
            
            String key = SESSION_PREFIX + sessionId;
            String value = JSON.toJSONString(context);
            int valueSize = value.length();
            
            log.info("💾 开始保存会话到Redis: sessionId={}, key={}, 数据大小={}字节, 过期时间={}秒", 
                sessionId, key, valueSize, SESSION_TIMEOUT_SECONDS);
            
            // 保存到Redis，设置过期时间
            redisTemplate.opsForValue().set(key, value, SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            // 验证是否保存成功
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                log.info("✅ 会话已成功保存到Redis: sessionId={}, key={}, 剩余过期时间={}秒, 数据大小={}字节", 
                    sessionId, key, ttl, valueSize);
            } else {
                log.error("❌ 会话保存后验证失败，Redis中不存在该key: sessionId={}, key={}", sessionId, key);
                throw new RuntimeException("会话保存验证失败");
            }
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            log.error("❌ Redis连接失败，无法保存会话: sessionId={}, 错误信息={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Redis连接失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ 保存会话到Redis失败: sessionId={}, 错误类型={}, 错误信息={}", 
                sessionId, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("保存会话失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从Redis获取会话
     * 
     * @param sessionId 会话ID
     * @return 会话上下文，如果不存在或已过期则返回null
     */
    public ConversationContext getSession(String sessionId) {
        try {
            // 检查Redis连接和配置
            if (redisTemplate == null) {
                log.debug("⚠️ RedisTemplate未初始化，无法从Redis获取会话: sessionId={}", sessionId);
                return null;
            }
            
            // RedisTemplate存在就说明配置已加载，直接使用
            
            String key = SESSION_PREFIX + sessionId;
            log.info("🔍 从Redis获取会话: sessionId={}, key={}", sessionId, key);
            
            // 先检查key是否存在
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(exists)) {
                log.info("ℹ️ 会话不存在或已过期: sessionId={}, key={}", sessionId, key);
                return null;
            }
            
            // 获取剩余过期时间
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.info("📖 会话存在，剩余过期时间={}秒，开始读取数据: sessionId={}, key={}", ttl, sessionId, key);
            
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                log.warn("⚠️ 会话key存在但值为null: sessionId={}, key={}", sessionId, key);
                return null;
            }
            
            int valueSize = value.length();
            log.info("📦 读取到会话数据，大小={}字节，开始解析: sessionId={}", valueSize, sessionId);
            
            ConversationContext context = JSON.parseObject(value, ConversationContext.class);
            
            // 刷新过期时间（每次访问时延长过期时间）
            redisTemplate.expire(key, SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Long newTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            
            log.info("✅ 从Redis获取会话成功: sessionId={}, key={}, 历史消息数={}, 刷新后过期时间={}秒", 
                sessionId, key, 
                context.getHistory() != null ? context.getHistory().size() : 0, 
                newTtl);
            
            return context;
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            log.error("❌ Redis连接失败，无法获取会话: sessionId={}, 错误信息={}", sessionId, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("❌ 从Redis获取会话失败: sessionId={}, 错误类型={}, 错误信息={}", 
                sessionId, e.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     */
    public void deleteSession(String sessionId) {
        try {
            String key = SESSION_PREFIX + sessionId;
            redisTemplate.delete(key);
            log.info("🗑️ 会话已从Redis删除: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("❌ 从Redis删除会话失败: sessionId={}", sessionId, e);
        }
    }
    
    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean exists(String sessionId) {
        try {
            String key = SESSION_PREFIX + sessionId;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("❌ 检查会话是否存在失败: sessionId={}", sessionId, e);
            return false;
        }
    }
    
    /**
     * 刷新会话过期时间
     * 
     * @param sessionId 会话ID
     */
    public void refreshSession(String sessionId) {
        try {
            String key = SESSION_PREFIX + sessionId;
            if (redisTemplate.hasKey(key)) {
                redisTemplate.expire(key, SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.debug("✅ 会话过期时间已刷新: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("❌ 刷新会话过期时间失败: sessionId={}", sessionId, e);
        }
    }
    
    /**
     * 获取会话剩余过期时间（秒）
     * 
     * @param sessionId 会话ID
     * @return 剩余过期时间（秒），如果不存在则返回-1
     */
    public long getSessionTtl(String sessionId) {
        try {
            String key = SESSION_PREFIX + sessionId;
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("❌ 获取会话过期时间失败: sessionId={}", sessionId, e);
            return -1;
        }
    }
    
    /**
     * 获取RedisTemplate（供其他服务使用）
     * 
     * @return StringRedisTemplate
     */
    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }
    
    /**
     * 获取会话的Redis Key
     * 
     * @param sessionId 会话ID
     * @return Redis Key
     */
    public String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }
}

