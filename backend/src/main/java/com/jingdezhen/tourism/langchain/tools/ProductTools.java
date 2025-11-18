package com.jingdezhen.tourism.langchain.tools;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.entity.ProductCategory;
import com.jingdezhen.tourism.mapper.ProductCategoryMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品相关工具
 * 使用 LangChain4j 的 @Tool 注解定义工具
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTools {
    
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    
    /**
     * 搜索产品工具
     * LangChain4j 会自动将方法参数转换为工具参数
     */
    @Tool("搜索旅游产品。支持按关键词、分类名称、区域、价格范围搜索产品。")
    public String searchProducts(
            String query,           // 搜索关键词（可选）
            String categoryName,    // 分类名称（可选）
            String region,          // 区域（可选）
            Double minPrice,        // 最低价格（可选）
            Double maxPrice,        // 最高价格（可选）
            Integer maxResults) {   // 最多返回结果数（可选，默认10）
        
        try {
            log.info("🔍 [LangChain4j] 搜索产品: query={}, categoryName={}, region={}, minPrice={}, maxPrice={}", 
                    query, categoryName, region, minPrice, maxPrice);
            
            // 设置默认值
            if (maxResults == null || maxResults <= 0) {
                maxResults = 10;
            }
            if (maxResults > 30) {
                maxResults = 30;
            }
            
            // 查找分类ID
            Long categoryId = null;
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                categoryId = findCategoryIdByName(categoryName);
            }
            
            // 如果没有分类但有查询词，尝试从查询词识别分类
            if (categoryId == null && query != null && !query.trim().isEmpty()) {
                categoryId = inferCategoryFromQuery(query);
            }
            
            // 构建查询条件
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1); // 只查询上架的产品
            
            // 关键词搜索
            if (query != null && !query.trim().isEmpty()) {
                String searchQuery = query.trim();
                wrapper.and(w -> w.like(Product::getTitle, searchQuery)
                                  .or()
                                  .like(Product::getDescription, searchQuery)
                                  .or()
                                  .like(Product::getTags, searchQuery));
            }
            
            // 分类筛选
            if (categoryId != null) {
                wrapper.eq(Product::getCategoryId, categoryId);
            }
            
            // 区域筛选
            if (region != null && !region.trim().isEmpty()) {
                wrapper.eq(Product::getRegion, region);
            }
            
            // 价格范围筛选
            if (minPrice != null) {
                wrapper.ge(Product::getPrice, minPrice);
            }
            if (maxPrice != null) {
                wrapper.le(Product::getPrice, maxPrice);
            }
            
            // 排序和限制数量
            wrapper.orderByDesc(Product::getRecommend)
                   .orderByDesc(Product::getRating)
                   .orderByDesc(Product::getSales)
                   .last("LIMIT " + maxResults);
            
            // 查询产品
            List<Product> products = productMapper.selectList(wrapper);
            
            log.info("✅ 找到 {} 个产品", products.size());
            
            if (products.isEmpty()) {
                return JSON.toJSONString(Map.of(
                        "success", true,
                        "data", List.of(),
                        "message", "未找到符合条件的产品，建议放宽搜索条件"
                ));
            }
            
            // 转换为简化格式
            List<Map<String, Object>> result = products.stream()
                    .map(this::convertToSimpleFormat)
                    .collect(Collectors.toList());
            
            return JSON.toJSONString(Map.of(
                    "success", true,
                    "data", result,
                    "message", String.format("找到%d个产品", result.size())
            ));
            
        } catch (Exception e) {
            log.error("❌ 搜索产品失败", e);
            return JSON.toJSONString(Map.of(
                    "success", false,
                    "message", "搜索失败：" + e.getMessage(),
                    "errorCode", "SEARCH_ERROR"
            ));
        }
    }
    
    /**
     * 获取产品详情工具
     */
    @Tool("获取指定产品的完整详细信息，包括完整描述、价格、库存、地址等")
    public String getProductDetail(Long productId) {
        try {
            log.info("📖 [LangChain4j] 获取产品详情: productId={}", productId);
            
            Product product = productMapper.selectById(productId);
            
            if (product == null || product.getStatus() != 1) {
                return JSON.toJSONString(Map.of(
                        "success", false,
                        "message", "产品不存在或已下架",
                        "errorCode", "PRODUCT_NOT_FOUND"
                ));
            }
            
            // 构建详细信息
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", product.getId());
            detail.put("title", product.getTitle());
            detail.put("description", product.getDescription());
            detail.put("price", product.getPrice());
            detail.put("originalPrice", product.getOriginalPrice());
            detail.put("stock", product.getStock());
            detail.put("sales", product.getSales());
            detail.put("rating", product.getRating());
            detail.put("region", product.getRegion());
            detail.put("address", product.getAddress());
            detail.put("tags", product.getTags());
            detail.put("features", product.getFeatures());
            detail.put("notice", product.getNotice());
            detail.put("coverImage", product.getCoverImage());
            detail.put("images", product.getImages());
            detail.put("categoryName", getCategoryName(product.getCategoryId()));
            
            return JSON.toJSONString(Map.of(
                    "success", true,
                    "data", detail,
                    "message", "获取产品详情成功"
            ));
            
        } catch (Exception e) {
            log.error("❌ 获取产品详情失败", e);
            return JSON.toJSONString(Map.of(
                    "success", false,
                    "message", "获取失败：" + e.getMessage(),
                    "errorCode", "GET_DETAIL_ERROR"
            ));
        }
    }
    
    /**
     * 获取产品分类列表工具
     */
    @Tool("获取所有可用的产品分类列表，包括分类ID和名称")
    public String getProductCategories() {
        try {
            log.info("📋 [LangChain4j] 获取产品分类列表");
            
            LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(ProductCategory::getSortOrder);
            
            List<ProductCategory> categories = categoryMapper.selectList(wrapper);
            
            List<Map<String, Object>> result = categories.stream()
                    .map(category -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", category.getId());
                        map.put("name", category.getName());
                        map.put("icon", category.getIcon());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return JSON.toJSONString(Map.of(
                    "success", true,
                    "data", result,
                    "message", "获取分类列表成功"
            ));
            
        } catch (Exception e) {
            log.error("❌ 获取分类列表失败", e);
            return JSON.toJSONString(Map.of(
                    "success", false,
                    "message", "获取失败：" + e.getMessage(),
                    "errorCode", "GET_CATEGORIES_ERROR"
            ));
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 根据分类名称查找分类ID
     */
    private Long findCategoryIdByName(String categoryName) {
        try {
            LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductCategory::getName, categoryName);
            
            ProductCategory category = categoryMapper.selectOne(wrapper);
            
            if (category == null) {
                // 尝试模糊匹配
                wrapper = new LambdaQueryWrapper<>();
                wrapper.like(ProductCategory::getName, categoryName);
                List<ProductCategory> categories = categoryMapper.selectList(wrapper);
                if (!categories.isEmpty()) {
                    category = categories.get(0);
                }
            }
            
            if (category != null) {
                log.info("✅ 找到分类: {} -> ID {}", categoryName, category.getId());
                return category.getId();
            }
        } catch (Exception e) {
            log.warn("查询分类失败: {}", categoryName, e);
        }
        return null;
    }
    
    /**
     * 从查询词推断分类
     */
    private Long inferCategoryFromQuery(String query) {
        String queryLower = query.toLowerCase();
        
        // 分类关键词映射
        Map<String, String> categoryKeywords = new HashMap<>();
        categoryKeywords.put("景点", "景点门票");
        categoryKeywords.put("门票", "景点门票");
        categoryKeywords.put("景区", "景点门票");
        categoryKeywords.put("酒店", "酒店住宿");
        categoryKeywords.put("住宿", "酒店住宿");
        categoryKeywords.put("美食", "特色餐饮");
        categoryKeywords.put("餐饮", "特色餐饮");
        categoryKeywords.put("体验", "文化体验");
        categoryKeywords.put("陶瓷", "陶瓷购物");
        categoryKeywords.put("购物", "陶瓷购物");
        
        // 按关键词长度降序排序（优先匹配更具体的关键词）
        List<String> sortedKeys = new ArrayList<>(categoryKeywords.keySet());
        sortedKeys.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String key : sortedKeys) {
            if (queryLower.contains(key)) {
                String categoryName = categoryKeywords.get(key);
                Long categoryId = findCategoryIdByName(categoryName);
                if (categoryId != null) {
                    log.info("✅ 从查询词推断分类: '{}' -> '{}'", query, categoryName);
                    return categoryId;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 转换为简化格式
     */
    private Map<String, Object> convertToSimpleFormat(Product product) {
        String description = product.getDescription();
        if (description != null && description.length() > 150) {
            description = description.substring(0, 150) + "...";
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("title", product.getTitle());
        map.put("price", product.getPrice());
        map.put("originalPrice", product.getOriginalPrice());
        map.put("region", product.getRegion());
        map.put("category", getCategoryName(product.getCategoryId()));
        map.put("rating", product.getRating());
        map.put("sales", product.getSales());
        map.put("stock", product.getStock());
        map.put("tags", product.getTags());
        map.put("description", description);
        map.put("coverImage", product.getCoverImage());
        
        return map;
    }
    
    /**
     * 获取分类名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "其他";
        
        try {
            ProductCategory category = categoryMapper.selectById(categoryId);
            if (category != null) {
                return category.getName();
            }
        } catch (Exception e) {
            log.warn("查询分类名称失败: categoryId={}", categoryId, e);
        }
        
        return "其他";
    }
}

