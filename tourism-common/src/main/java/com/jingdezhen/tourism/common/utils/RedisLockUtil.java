package com.jingdezhen.tourism.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 */
@Slf4j
@Component
public class RedisLockUtil {
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else return 0 end";
    
    private static final String LOCK_VALUE_PREFIX = "lock:value:";
    private static final String LOCK_KEY_PREFIX = "lock:";
    
    public String tryLock(String lockKey, long waitTime, long leaseTime) {
        if (stringRedisTemplate == null) {
            log.warn("⚠️ Redis未配置，无法获取分布式锁: lockKey={}", lockKey);
            return null;
        }
        
        String lockValue = LOCK_VALUE_PREFIX + Thread.currentThread().getId() + ":" + System.currentTimeMillis();
        String fullLockKey = LOCK_KEY_PREFIX + lockKey;
        
        long endTime = System.currentTimeMillis() + waitTime * 1000;
        
        while (System.currentTimeMillis() < endTime) {
            Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(fullLockKey, lockValue, leaseTime, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("✅ 成功获取分布式锁: lockKey={}", lockKey);
                return lockValue;
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        
        log.warn("⚠️ 获取分布式锁超时: lockKey={}", lockKey);
        return null;
    }
    
    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null || stringRedisTemplate == null) {
            return false;
        }
        
        String fullLockKey = LOCK_KEY_PREFIX + lockKey;
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = stringRedisTemplate.execute(script, 
                Collections.singletonList(fullLockKey), 
                lockValue);
            
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("❌ 释放分布式锁时发生异常: lockKey={}", lockKey, e);
            return false;
        }
    }
    
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
    
    @FunctionalInterface
    public interface LockAction<T> {
        T execute() throws Exception;
    }
}

