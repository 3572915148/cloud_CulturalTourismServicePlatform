package com.jingdezhen.tourism.service;

import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.utils.RedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 库存服务
 * 使用Redis缓存库存，提升性能并防止超卖
 * 
 * @author AI Assistant
 */
@Slf4j
@Service
public class StockService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    @Autowired
    private com.jingdezhen.tourism.config.RedisConfig redisConfig;
    
    /**
     * 获取RedisTemplate
     */
    private StringRedisTemplate getRedisTemplate() {
        return redisConfig.getStringRedisTemplate();
    }
    
    private static final String STOCK_KEY_PREFIX = "stock:product:";
    private static final long STOCK_CACHE_EXPIRE = 7 * 24 * 60 * 60; // 7天过期
    
    /**
     * 扣减库存的Lua脚本（原子操作）
     * 先检查库存是否充足，再扣减
     */
    private static final String DECREASE_STOCK_SCRIPT = 
        "local stock = tonumber(redis.call('get', KEYS[1]) or '0') " +
        "local quantity = tonumber(ARGV[1]) " +
        "if stock >= quantity then " +
        "  redis.call('decrby', KEYS[1], quantity) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";
    
    /**
     * 增加库存的Lua脚本（原子操作）
     */
    private static final String INCREASE_STOCK_SCRIPT = 
        "local stock = tonumber(redis.call('get', KEYS[1]) or '0') " +
        "local quantity = tonumber(ARGV[1]) " +
        "redis.call('incrby', KEYS[1], quantity) " +
        "return 1";
    
    /**
     * 初始化商品库存到Redis
     * 如果Redis中不存在，从数据库加载
     * 
     * @param productId 商品ID
     * @return 库存数量，如果商品不存在返回-1
     */
    public Integer initStock(Long productId) {
        StringRedisTemplate redisTemplate = getRedisTemplate();
        if (redisTemplate == null) {
            log.warn("⚠️ Redis未配置，无法初始化库存缓存: productId={}", productId);
            return null;
        }
        
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        // 先检查Redis中是否已有库存
        String cachedStock = redisTemplate.opsForValue().get(stockKey);
        if (cachedStock != null) {
            return Integer.parseInt(cachedStock);
        }
        
        // 使用分布式锁，防止并发初始化
        return redisLockUtil.executeWithLock("stock:init:" + productId, 3, 10, () -> {
            // 双重检查
            String cached = redisTemplate.opsForValue().get(stockKey);
            if (cached != null) {
                return Integer.parseInt(cached);
            }
            
            // 从数据库加载
            Product product = productMapper.selectById(productId);
            if (product == null) {
                log.warn("⚠️ 商品不存在: productId={}", productId);
                return -1;
            }
            
            Integer stock = product.getStock() != null ? product.getStock() : 0;
            
            // 写入Redis
            redisTemplate.opsForValue().set(stockKey, String.valueOf(stock), 
                STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
            
            log.info("✅ 初始化商品库存到Redis: productId={}, stock={}", productId, stock);
            return stock;
        });
    }
    
    /**
     * 获取商品库存（优先从Redis读取）
     * 
     * @param productId 商品ID
     * @return 库存数量
     */
    public Integer getStock(Long productId) {
        StringRedisTemplate redisTemplate = getRedisTemplate();
        if (redisTemplate == null) {
            // Redis不可用时，直接从数据库读取
            Product product = productMapper.selectById(productId);
            return product != null && product.getStock() != null ? product.getStock() : 0;
        }
        
        String stockKey = STOCK_KEY_PREFIX + productId;
        String cachedStock = redisTemplate.opsForValue().get(stockKey);
        
        if (cachedStock != null) {
            return Integer.parseInt(cachedStock);
        }
        
        // Redis中没有，初始化
        return initStock(productId);
    }
    
    /**
     * 扣减库存（原子操作，防止超卖）
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return true表示扣减成功，false表示库存不足
     */
    public boolean decreaseStock(Long productId, Integer quantity) {
        StringRedisTemplate redisTemplate = getRedisTemplate();
        if (redisTemplate == null) {
            log.warn("⚠️ Redis未配置，使用数据库扣减库存: productId={}, quantity={}", productId, quantity);
            return decreaseStockFromDB(productId, quantity);
        }
        
        // 先确保库存已初始化
        Integer currentStock = getStock(productId);
        if (currentStock == null || currentStock < 0) {
            log.warn("⚠️ 商品不存在或库存初始化失败: productId={}", productId);
            return false;
        }
        
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        // 使用Lua脚本原子性扣减库存
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(DECREASE_STOCK_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Collections.singletonList(stockKey), 
            String.valueOf(quantity));
        
        boolean success = result != null && result == 1;
        
        if (success) {
            log.info("✅ Redis库存扣减成功: productId={}, quantity={}, 剩余库存={}", 
                productId, quantity, getStock(productId));
            
            // 异步同步到数据库（不阻塞主流程）
            syncStockToDBAsync(productId);
        } else {
            log.warn("⚠️ Redis库存扣减失败（库存不足）: productId={}, quantity={}, 当前库存={}", 
                productId, quantity, currentStock);
        }
        
        return success;
    }
    
    /**
     * 增加库存（用于取消订单时恢复库存）
     * 
     * @param productId 商品ID
     * @param quantity 增加数量
     */
    public void increaseStock(Long productId, Integer quantity) {
        StringRedisTemplate redisTemplate = getRedisTemplate();
        if (redisTemplate == null) {
            log.warn("⚠️ Redis未配置，使用数据库增加库存: productId={}, quantity={}", productId, quantity);
            increaseStockFromDB(productId, quantity);
            return;
        }
        
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        // 使用Lua脚本原子性增加库存
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(INCREASE_STOCK_SCRIPT);
        script.setResultType(Long.class);
        
        redisTemplate.execute(script, 
            Collections.singletonList(stockKey), 
            String.valueOf(quantity));
        
        log.info("✅ Redis库存增加成功: productId={}, quantity={}, 当前库存={}", 
            productId, quantity, getStock(productId));
        
        // 异步同步到数据库
        syncStockToDBAsync(productId);
    }
    
    /**
     * 从数据库扣减库存（Redis不可用时的降级方案）
     */
    private boolean decreaseStockFromDB(Long productId, Integer quantity) {
        // 使用分布式锁保证并发安全
        return redisLockUtil.executeWithLock("stock:db:decrease:" + productId, 3, 10, () -> {
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStock() == null) {
                return false;
            }
            
            if (product.getStock() < quantity) {
                return false;
            }
            
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);
            return true;
        });
    }
    
    /**
     * 从数据库增加库存（Redis不可用时的降级方案）
     */
    private void increaseStockFromDB(Long productId, Integer quantity) {
        redisLockUtil.executeWithLock("stock:db:increase:" + productId, 3, 10, () -> {
            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setStock((product.getStock() == null ? 0 : product.getStock()) + quantity);
                productMapper.updateById(product);
            }
            return null;
        });
    }
    
    /**
     * 异步同步库存到数据库
     * 使用分布式锁防止并发同步
     */
    private void syncStockToDBAsync(Long productId) {
        // 使用异步方式同步，不阻塞主流程
        new Thread(() -> {
            try {
                StringRedisTemplate redisTemplate = getRedisTemplate();
                if (redisTemplate == null) {
                    return;
                }
                
                String stockKey = STOCK_KEY_PREFIX + productId;
                String stockStr = redisTemplate.opsForValue().get(stockKey);
                
                if (stockStr == null) {
                    return;
                }
                
                Integer stock = Integer.parseInt(stockStr);
                
                // 使用分布式锁同步到数据库
                redisLockUtil.executeWithLock("stock:sync:" + productId, 3, 10, () -> {
                    Product product = productMapper.selectById(productId);
                    if (product != null) {
                        product.setStock(stock);
                        productMapper.updateById(product);
                        log.debug("✅ 库存已同步到数据库: productId={}, stock={}", productId, stock);
                    }
                    return null;
                });
            } catch (Exception e) {
                log.error("❌ 同步库存到数据库失败: productId={}, error={}", productId, e.getMessage(), e);
            }
        }).start();
    }
    
    /**
     * 强制同步库存到数据库（用于定时任务或手动同步）
     * 
     * @param productId 商品ID
     */
    public void forceSyncStockToDB(Long productId) {
        StringRedisTemplate redisTemplate = getRedisTemplate();
        if (redisTemplate == null) {
            return;
        }
        
        String stockKey = STOCK_KEY_PREFIX + productId;
        String stockStr = redisTemplate.opsForValue().get(stockKey);
        
        if (stockStr == null) {
            // Redis中没有，从数据库初始化
            initStock(productId);
            return;
        }
        
        Integer stock = Integer.parseInt(stockStr);
        
        redisLockUtil.executeWithLock("stock:sync:" + productId, 3, 10, () -> {
            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setStock(stock);
                productMapper.updateById(product);
                log.info("✅ 强制同步库存到数据库: productId={}, stock={}", productId, stock);
            }
            return null;
        });
    }
    
    /**
     * 预热商品库存到Redis（用于系统启动或定时任务）
     * 
     * @param productId 商品ID
     */
    public void warmupStock(Long productId) {
        initStock(productId);
    }
}

