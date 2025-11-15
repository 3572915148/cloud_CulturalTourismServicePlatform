package com.jingdezhen.tourism.service;

import com.jingdezhen.tourism.config.RedisConfig;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存同步定时任务
 * 定期将Redis中的库存同步到MySQL，保证数据一致性
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "stock.sync.enabled", havingValue = "true", matchIfMissing = true)
public class StockSyncScheduler {
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private RedisConfig redisConfig;
    
    /**
     * 定时同步库存到数据库
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncStockToDatabase() {
        StringRedisTemplate redisTemplate = redisConfig.getStringRedisTemplate();
        if (redisTemplate == null) {
            log.debug("Redis未配置，跳过库存同步");
            return;
        }
        
        try {
            log.info("🔄 开始定时同步库存到数据库...");
            
            // 查询所有上架的商品
            List<Product> products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1)
            );
            
            int successCount = 0;
            int failCount = 0;
            
            for (Product product : products) {
                try {
                    stockService.forceSyncStockToDB(product.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("❌ 同步商品库存失败: productId={}, error={}", 
                        product.getId(), e.getMessage(), e);
                    failCount++;
                }
            }
            
            log.info("✅ 库存同步完成: 成功={}, 失败={}, 总计={}", 
                successCount, failCount, products.size());
        } catch (Exception e) {
            log.error("❌ 定时同步库存任务执行失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 预热热门商品库存到Redis
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void warmupHotProducts() {
        StringRedisTemplate redisTemplate = redisConfig.getStringRedisTemplate();
        if (redisTemplate == null) {
            log.debug("Redis未配置，跳过库存预热");
            return;
        }
        
        try {
            log.info("🔥 开始预热热门商品库存...");
            
            // 查询销量前100的商品（热门商品）
            List<Product> hotProducts = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1)
                    .orderByDesc(Product::getSales)
                    .last("LIMIT 100")
            );
            
            int successCount = 0;
            for (Product product : hotProducts) {
                try {
                    stockService.warmupStock(product.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("❌ 预热商品库存失败: productId={}, error={}", 
                        product.getId(), e.getMessage());
                }
            }
            
            log.info("✅ 库存预热完成: 成功预热{}个商品", successCount);
        } catch (Exception e) {
            log.error("❌ 库存预热任务执行失败: error={}", e.getMessage(), e);
        }
    }
}

