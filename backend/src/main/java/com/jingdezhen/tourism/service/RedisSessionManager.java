package com.jingdezhen.tourism.service;

import com.alibaba.fastjson2.JSON;
import com.jingdezhen.tourism.agent.core.ConversationContext;
import com.jingdezhen.tourism.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RedisConfig redisConfig;
    
    /**
     * 获取RedisTemplate（延迟获取，避免启动时Redis未配置导致的问题）
     * 注意：可能返回null，使用前需要检查
     * 
     * @return StringRedisTemplate，如果Redis未配置则返回null
     */
    public StringRedisTemplate getRedisTemplate() {
        return redisConfig.getStringRedisTemplate();
    }
    
    /**
     * 保存会话到Redis
     * 
     * @param sessionId 会话ID
     * @param context 会话上下文
     */
    public void saveSession(String sessionId, ConversationContext context) {
        try {
            StringRedisTemplate redisTemplate = getRedisTemplate();
            // 检查Redis连接和配置
            if (redisTemplate == null || !redisConfig.isRedisAvailable()) {
                log.warn("⚠️ Redis未配置或不可用，无法保存会话到Redis: sessionId={}", sessionId);
                log.warn("   可能原因：Redis配置未在application.yml中配置");
                log.warn("   会话将无法持久化，服务器重启后会丢失");
                return; // 不抛出异常，允许应用继续运行
            }
            
            String key = RedisConfig.KeyPrefix.SESSION + sessionId;
            String value = JSON.toJSONString(context);
            int valueSize = value.length();
            
            log.info("💾 开始保存会话到Redis: sessionId={}, key={}, 数据大小={}字节, 过期时间={}秒", 
                sessionId, key, valueSize, RedisConfig.ExpireTime.SESSION_TIMEOUT);
            
            // 保存到Redis，设置过期时间
            redisTemplate.opsForValue().set(key, value, RedisConfig.ExpireTime.SESSION_TIMEOUT, TimeUnit.SECONDS);
            
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
            StringRedisTemplate redisTemplate = getRedisTemplate();
            // 检查Redis连接和配置
            if (redisTemplate == null || !redisConfig.isRedisAvailable()) {
                log.debug("⚠️ Redis未配置或不可用，无法从Redis获取会话: sessionId={}", sessionId);
                return null;
            }
            
            String key = RedisConfig.KeyPrefix.SESSION + sessionId;
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
            redisTemplate.expire(key, RedisConfig.ExpireTime.SESSION_TIMEOUT, TimeUnit.SECONDS);
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
            StringRedisTemplate redisTemplate = getRedisTemplate();
            if (redisTemplate == null || !redisConfig.isRedisAvailable()) {
                log.warn("⚠️ Redis未配置或不可用，无法删除会话: sessionId={}", sessionId);
                return;
            }
            
            String key = RedisConfig.KeyPrefix.SESSION + sessionId;
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
            StringRedisTemplate redisTemplate = getRedisTemplate();
            if (redisTemplate == null || !redisConfig.isRedisAvailable()) {
                log.warn("⚠️ Redis未配置或不可用，无法检查会话是否存在: sessionId={}", sessionId);
                return false;
            }
            
            String key = RedisConfig.KeyPrefix.SESSION + sessionId;
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
            StringRedisTemplate redisTemplate = getRedisTemplate();
            if (redisTemplate == null || !redisConfig.isRedisAvailable()) {
                log.warn("⚠️ Redis未配置或不可用，无法刷新会话过期时间: sessionId={}", sessionId);
                return;
            }
            
            String key = RedisConfig.KeyPrefix.SESSION + sessionId;
            if (redisTemplate.hasKey(key)) {
                redisTemplate.expire(key, RedisConfig.ExpireTime.SESSION_TIMEOUT, TimeUnit.SECONDS);
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
            StringRedisTemplate redisTemplate = getRedisTemplate();
            if (redisTemplate == null || !redisConfig.isRedisAvailable()) {
                log.warn("⚠️ Redis未配置或不可用，无法获取会话过期时间: sessionId={}", sessionId);
                return -1;
            }
            
            String key = RedisConfig.KeyPrefix.SESSION + sessionId;
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("❌ 获取会话过期时间失败: sessionId={}", sessionId, e);
            return -1;
        }
    }
    
    /**
     * 获取会话的Redis Key
     * 
     * @param sessionId 会话ID
     * @return Redis Key
     */
    public String getSessionKey(String sessionId) {
        return RedisConfig.KeyPrefix.SESSION + sessionId;
    }
    
    /**
     * 检查Redis是否可用
     * 
     * @return true表示Redis可用，false表示不可用
     */
    public boolean isRedisAvailable() {
        return redisConfig.isRedisAvailable();
    }
}

