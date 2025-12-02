package com.jingdezhen.tourism.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.product.mapper.ProductMapper;
import com.jingdezhen.tourism.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 产品Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String PRODUCT_LIST_PREFIX = "product:list:";
    private static final long PRODUCT_LIST_EXPIRE = 60 * 60 * 24; // 1天

    @Override
    public Page<Product> getProductList(Long current, Long size, Long categoryId, String region, String keyword) {
        // 生成缓存Key
        String cacheKey = generateCacheKey(current, size, categoryId, region, keyword);
        
        // 尝试从缓存获取
        if (stringRedisTemplate != null) {
            try {
                String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
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
        if (stringRedisTemplate != null) {
            try {
                String jsonData = JSON.toJSONString(result);
                stringRedisTemplate.opsForValue().set(
                    cacheKey, 
                    jsonData, 
                    PRODUCT_LIST_EXPIRE, 
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
     */
    private String generateCacheKey(Long current, Long size, Long categoryId, String region, String keyword) {
        StringBuilder key = new StringBuilder(PRODUCT_LIST_PREFIX);
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
        clearProductListCache();
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateById(product);
        clearProductListCache();
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        clearProductListCache();
    }
    
    /**
     * 清除产品列表缓存
     */
    private void clearProductListCache() {
        if (stringRedisTemplate == null) {
            return;
        }
        
        try {
            String pattern = PRODUCT_LIST_PREFIX + "*";
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.debug("✅ 已清除产品列表缓存: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("⚠️ 清除产品列表缓存失败: error={}", e.getMessage());
        }
    }
}

