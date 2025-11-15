package com.jingdezhen.tourism.utils;

import com.jingdezhen.tourism.config.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 用于保证高并发场景下的数据一致性
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
public class RedisLockUtil {
    
    @Autowired
    private RedisConfig redisConfig;
    
    private static final String LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else return 0 end";
    
    private static final String LOCK_VALUE_PREFIX = "lock:value:";
    
    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁的key
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁的持有时间（秒）
     * @return 锁的value，如果获取失败返回null
     */
    public String tryLock(String lockKey, long waitTime, long leaseTime) {
        StringRedisTemplate redisTemplate = redisConfig.getStringRedisTemplate();
        if (redisTemplate == null) {
            log.warn("⚠️ Redis未配置，无法获取分布式锁: lockKey={}", lockKey);
            return null;
        }
        
        String lockValue = LOCK_VALUE_PREFIX + Thread.currentThread().getId() + ":" + System.currentTimeMillis();
        String fullLockKey = RedisConfig.KeyPrefix.LOCK + lockKey;
        
        long endTime = System.currentTimeMillis() + waitTime * 1000;
        
        while (System.currentTimeMillis() < endTime) {
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(fullLockKey, lockValue, leaseTime, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("✅ 成功获取分布式锁: lockKey={}, lockValue={}", lockKey, lockValue);
                return lockValue;
            }
            
            // 等待一小段时间后重试
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("⚠️ 获取锁时被中断: lockKey={}", lockKey);
                return null;
            }
        }
        
        log.warn("⚠️ 获取分布式锁超时: lockKey={}, waitTime={}秒", lockKey, waitTime);
        return null;
    }
    
    /**
     * 释放分布式锁
     * 使用Lua脚本保证原子性，只有持有锁的线程才能释放
     * 
     * @param lockKey 锁的key
     * @param lockValue 锁的value（必须与获取时返回的value一致）
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }
        
        StringRedisTemplate redisTemplate = redisConfig.getStringRedisTemplate();
        if (redisTemplate == null) {
            log.warn("⚠️ Redis未配置，无法释放分布式锁: lockKey={}", lockKey);
            return false;
        }
        
        String fullLockKey = RedisConfig.KeyPrefix.LOCK + lockKey;
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, 
                Collections.singletonList(fullLockKey), 
                lockValue);
            
            boolean released = result != null && result > 0;
            if (released) {
                log.debug("✅ 成功释放分布式锁: lockKey={}", lockKey);
            } else {
                log.warn("⚠️ 释放分布式锁失败（可能已过期或被其他线程释放）: lockKey={}", lockKey);
            }
            
            return released;
        } catch (Exception e) {
            log.error("❌ 释放分布式锁时发生异常: lockKey={}, error={}", lockKey, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 执行带锁的操作
     * 
     * @param lockKey 锁的key
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁的持有时间（秒）
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, LockAction<T> action) {
        String lockValue = tryLock(lockKey, waitTime, leaseTime);
        if (lockValue == null) {
            throw new RuntimeException("获取分布式锁失败: " + lockKey);
        }
        
        try {
            return action.execute();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("执行带锁操作时发生异常: " + e.getMessage(), e);
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }
    
    /**
     * 锁操作接口
     */
    @FunctionalInterface
    public interface LockAction<T> {
        T execute() throws Exception;
    }
}

