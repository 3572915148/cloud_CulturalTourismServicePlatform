package com.jingdezhen.tourism.service;

import com.jingdezhen.tourism.config.RedisConfig;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    
    @Autowired
    @Qualifier("stockSyncExecutor")
    private ThreadPoolTaskExecutor stockSyncExecutor;
    
    /**
     * 定时同步库存到数据库
     * 每5分钟执行一次
     * 优化：使用线程池并行处理，提升性能
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
            long startTime = System.currentTimeMillis();
            
            // 查询所有上架的商品
            List<Product> products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1)
            );
            
            log.info("📦 找到 {} 个上架商品，开始并行同步...", products.size());
            
            // 使用线程池并行处理库存同步
            List<CompletableFuture<SyncResult>> futures = products.stream()
                .map(product -> CompletableFuture.supplyAsync(() -> {
                    try {
                        stockService.forceSyncStockToDB(product.getId());
                        return new SyncResult(product.getId(), true, null);
                    } catch (Exception e) {
                        log.error("❌ 同步商品库存失败: productId={}, error={}", 
                            product.getId(), e.getMessage(), e);
                        return new SyncResult(product.getId(), false, e.getMessage());
                    }
                }, stockSyncExecutor))
                .collect(Collectors.toList());
            
            // 等待所有任务完成并统计结果
            List<SyncResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            int successCount = (int) results.stream().filter(SyncResult::isSuccess).count();
            int failCount = results.size() - successCount;
            long endTime = System.currentTimeMillis();
            
            log.info("✅ 库存同步完成: 成功={}, 失败={}, 总计={}, 耗时={}ms", 
                successCount, failCount, products.size(), (endTime - startTime));
        } catch (Exception e) {
            log.error("❌ 定时同步库存任务执行失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 预热热门商品库存到Redis
     * 每天凌晨2点执行
     * 优化：使用线程池并行处理，提升性能
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
            long startTime = System.currentTimeMillis();
            
            // 查询销量前100的商品（热门商品）
            List<Product> hotProducts = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1)
                    .orderByDesc(Product::getSales)
                    .last("LIMIT 100")
            );
            
            log.info("📦 找到 {} 个热门商品，开始并行预热...", hotProducts.size());
            
            // 使用线程池并行处理库存预热
            List<CompletableFuture<SyncResult>> futures = hotProducts.stream()
                .map(product -> CompletableFuture.supplyAsync(() -> {
                    try {
                        stockService.warmupStock(product.getId());
                        return new SyncResult(product.getId(), true, null);
                    } catch (Exception e) {
                        log.error("❌ 预热商品库存失败: productId={}, error={}", 
                            product.getId(), e.getMessage());
                        return new SyncResult(product.getId(), false, e.getMessage());
                    }
                }, stockSyncExecutor))
                .collect(Collectors.toList());
            
            // 等待所有任务完成并统计结果
            List<SyncResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            int successCount = (int) results.stream().filter(SyncResult::isSuccess).count();
            long endTime = System.currentTimeMillis();
            
            log.info("✅ 库存预热完成: 成功预热{}个商品, 耗时={}ms", 
                successCount, (endTime - startTime));
        } catch (Exception e) {
            log.error("❌ 库存预热任务执行失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 同步结果辅助类
     */
    @SuppressWarnings("unused")
    private static class SyncResult {
        private final Long productId;
        private final boolean success;
        private final String error;
        
        public SyncResult(Long productId, boolean success, String error) {
            this.productId = productId;
            this.success = success;
            this.error = error;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
}

