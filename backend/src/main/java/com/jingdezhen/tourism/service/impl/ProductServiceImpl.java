package com.jingdezhen.tourism.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.config.RedisConfig;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 产品Service实现类
 * 已优化：使用Redis缓存产品列表查询结果，提升筛选性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final RedisConfig redisConfig;

    @Override
    public Page<Product> getProductList(Long current, Long size, Long categoryId, String region, String keyword) {
        // 生成缓存Key
        String cacheKey = generateCacheKey(current, size, categoryId, region, keyword);
        
        // 尝试从缓存获取
        StringRedisTemplate redisTemplate = redisConfig.getStringRedisTemplate();
        if (redisTemplate != null) {
            try {
                String cachedData = redisTemplate.opsForValue().get(cacheKey);
                if (cachedData != null) {
                    log.debug("✅ 从缓存获取产品列表: key={}", cacheKey);
                    return JSON.parseObject(cachedData, new TypeReference<Page<Product>>() {});
                }
            } catch (Exception e) {
                log.warn("⚠️ 读取缓存失败，将查询数据库: key={}, error={}", cacheKey, e.getMessage());
            }
        }
        
        // 缓存未命中，查询数据库
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询上架的产品
        wrapper.eq(Product::getStatus, 1);

        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按区域筛选
        if (StringUtils.hasText(region)) {
            wrapper.eq(Product::getRegion, region);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Product::getTitle, keyword)
                    .or()
                    .like(Product::getDescription, keyword)
            );
        }

        // 按推荐和评分排序
        wrapper.orderByDesc(Product::getRecommend)
               .orderByDesc(Product::getRating);

        Page<Product> result = productMapper.selectPage(page, wrapper);
        
        // 写入缓存
        if (redisTemplate != null) {
            try {
                String jsonData = JSON.toJSONString(result);
                redisTemplate.opsForValue().set(
                    cacheKey, 
                    jsonData, 
                    RedisConfig.ExpireTime.PRODUCT_LIST, 
                    TimeUnit.SECONDS
                );
                log.debug("✅ 产品列表已缓存: key={}, size={}", cacheKey, result.getRecords().size());
            } catch (Exception e) {
                log.warn("⚠️ 写入缓存失败: key={}, error={}", cacheKey, e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 生成缓存Key
     * 格式: product:list:{categoryId}:{region}:{keyword}:{current}:{size}
     */
    private String generateCacheKey(Long current, Long size, Long categoryId, String region, String keyword) {
        StringBuilder key = new StringBuilder(RedisConfig.KeyPrefix.PRODUCT_LIST);
        key.append(categoryId != null ? categoryId : "all");
        key.append(":");
        key.append(StringUtils.hasText(region) ? region : "all");
        key.append(":");
        key.append(StringUtils.hasText(keyword) ? keyword.hashCode() : "all");
        key.append(":");
        key.append(current);
        key.append(":");
        key.append(size);
        return key.toString();
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        return product;
    }

    @Override
    public void createProduct(Product product) {
        product.setStatus(1);
        product.setSales(0);
        product.setRating(java.math.BigDecimal.valueOf(5.0));
        productMapper.insert(product);
        // 清除产品列表缓存
        clearProductListCache();
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateById(product);
        // 清除产品列表缓存
        clearProductListCache();
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        // 清除产品列表缓存
        clearProductListCache();
    }
    
    /**
     * 清除产品列表缓存
     * 当产品增删改时，清除所有相关的产品列表缓存
     */
    private void clearProductListCache() {
        StringRedisTemplate redisTemplate = redisConfig.getStringRedisTemplate();
        if (redisTemplate == null) {
            return;
        }
        
        try {
            // 使用通配符删除所有产品列表缓存
            String pattern = RedisConfig.KeyPrefix.PRODUCT_LIST + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("✅ 已清除产品列表缓存: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("⚠️ 清除产品列表缓存失败: error={}", e.getMessage());
        }
    }
}

