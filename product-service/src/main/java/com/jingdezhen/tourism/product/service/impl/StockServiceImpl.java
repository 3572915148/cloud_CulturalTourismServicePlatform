package com.jingdezhen.tourism.product.service.impl;

import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.utils.RedisLockUtil;
import com.jingdezhen.tourism.product.mapper.ProductMapper;
import com.jingdezhen.tourism.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 库存服务实现
 * 使用Redis缓存库存，提升性能并防止超卖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductMapper productMapper;
    private final RedisLockUtil redisLockUtil;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_KEY_PREFIX = "stock:product:";
    private static final long STOCK_CACHE_EXPIRE = 7 * 24 * 60 * 60; // 7天过期

    /**
     * 扣减库存的Lua脚本（原子操作）
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

    @Override
    public Integer initStock(Long productId) {
        if (stringRedisTemplate == null) {
            log.warn("⚠️ Redis未配置，无法初始化库存缓存: productId={}", productId);
            return null;
        }

        String stockKey = STOCK_KEY_PREFIX + productId;

        // 先检查Redis中是否已有库存
        String cachedStock = stringRedisTemplate.opsForValue().get(stockKey);
        if (cachedStock != null) {
            return Integer.parseInt(cachedStock);
        }

        // 使用分布式锁，防止并发初始化
        return redisLockUtil.executeWithLock("stock:init:" + productId, 3, 10, () -> {
            // 双重检查
            String cached = stringRedisTemplate.opsForValue().get(stockKey);
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
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock), 
                STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);

            log.info("✅ 初始化商品库存到Redis: productId={}, stock={}", productId, stock);
            return stock;
        });
    }

    @Override
    public Integer getStock(Long productId) {
        if (stringRedisTemplate == null) {
            // Redis不可用时，直接从数据库读取
            Product product = productMapper.selectById(productId);
            return product != null && product.getStock() != null ? product.getStock() : 0;
        }

        String stockKey = STOCK_KEY_PREFIX + productId;
        String cachedStock = stringRedisTemplate.opsForValue().get(stockKey);

        if (cachedStock != null) {
            return Integer.parseInt(cachedStock);
        }

        // Redis中没有，初始化
        return initStock(productId);
    }

    @Override
    public boolean decreaseStock(Long productId, Integer quantity) {
        if (stringRedisTemplate == null) {
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

        Long result = stringRedisTemplate.execute(script, 
            Collections.singletonList(stockKey), 
            String.valueOf(quantity));

        boolean success = result != null && result == 1;

        if (success) {
            log.info("✅ Redis库存扣减成功: productId={}, quantity={}, 剩余库存={}", 
                productId, quantity, getStock(productId));

            // 异步同步到数据库
            syncStockToDBAsync(productId);
        } else {
            log.warn("⚠️ Redis库存扣减失败（库存不足）: productId={}, quantity={}, 当前库存={}", 
                productId, quantity, currentStock);
        }

        return success;
    }

    @Override
    public void increaseStock(Long productId, Integer quantity) {
        if (stringRedisTemplate == null) {
            log.warn("⚠️ Redis未配置，使用数据库增加库存: productId={}, quantity={}", productId, quantity);
            increaseStockFromDB(productId, quantity);
            return;
        }

        String stockKey = STOCK_KEY_PREFIX + productId;

        // 使用Lua脚本原子性增加库存
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(INCREASE_STOCK_SCRIPT);
        script.setResultType(Long.class);

        stringRedisTemplate.execute(script, 
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
     */
    private void syncStockToDBAsync(Long productId) {
        new Thread(() -> {
            try {
                if (stringRedisTemplate == null) {
                    return;
                }

                String stockKey = STOCK_KEY_PREFIX + productId;
                String stockStr = stringRedisTemplate.opsForValue().get(stockKey);

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
}

