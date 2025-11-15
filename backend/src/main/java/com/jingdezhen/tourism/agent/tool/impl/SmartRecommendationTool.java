package com.jingdezhen.tourism.agent.tool.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.entity.*;
import com.jingdezhen.tourism.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐工具
 * 基于用户行为数据（订单、收藏、评价）进行个性化推荐
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmartRecommendationTool implements AgentTool {
    
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final OrdersMapper ordersMapper;
    private final FavoriteMapper favoriteMapper;
    private final ReviewMapper reviewMapper;
    
    @Override
    public String getName() {
        return "smart_recommendation";
    }
    
    @Override
    public String getDescription() {
        return "基于用户历史行为（订单、收藏、评价）进行智能个性化推荐。" +
               "适用于：\"为我推荐\"、\"有什么好推荐\"、\"根据我的喜好推荐\" 等场景。" +
               "会分析用户的购买历史、收藏偏好、评价记录，结合协同过滤算法推荐相似产品。";
    }
    
    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "count": {
                    "type": "integer",
                    "description": "推荐数量，默认5个，最多10个",
                    "default": 5
                },
                "categoryName": {
                    "type": "string",
                    "description": "限定推荐的分类名称（可选），如：景点门票、酒店住宿等"
                },
                "priceRange": {
                    "type": "string",
                    "description": "价格范围（可选），如：100-500"
                }
            },
            "required": []
        }
        """;
    }
    
    @Override
    public String getCategory() {
        return "recommendation";
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("🎯 执行智能推荐工具: userId={}, params={}", userId, parameters);
            
            Integer count = extractInteger(parameters, "count", 5);
            if (count > 10) count = 10;
            
            String categoryName = (String) parameters.get("categoryName");
            String priceRange = (String) parameters.get("priceRange");
            
            // 1. 分析用户行为数据
            UserBehaviorProfile profile = analyzeUserBehavior(userId);
            log.info("用户行为分析: 订单{}个, 收藏{}个, 评价{}个", 
                profile.orderCount, profile.favoriteCount, profile.reviewCount);
            
            // 2. 获取候选产品池
            List<Product> candidates = getCandidateProducts(categoryName, priceRange);
            log.info("候选产品池: {}个产品", candidates.size());
            
            // 3. 计算推荐分数并排序
            List<RecommendationScore> scores = calculateRecommendationScores(
                candidates, profile, userId);
            
            // 4. 选择Top N推荐
            List<RecommendationScore> topRecommendations = scores.stream()
                .limit(count)
                .collect(Collectors.toList());
            
            // 5. 转换为返回格式
            List<Map<String, Object>> result = topRecommendations.stream()
                .map(score -> convertToResult(score, profile))
                .collect(Collectors.toList());
            
            log.info("✅ 智能推荐完成，推荐{}个产品", result.size());
            
            // 6. 构建推荐理由
            String reason = buildRecommendationReason(profile, topRecommendations);
            
            return ToolResult.builder()
                .success(true)
                .data(result)
                .message(reason)
                .build();
                
        } catch (Exception e) {
            log.error("❌ 智能推荐失败: userId={}, params={}", userId, parameters, e);
            return ToolResult.builder()
                .success(false)
                .message("智能推荐失败：" + e.getMessage())
                .errorCode("SMART_RECOMMENDATION_ERROR")
                .build();
        }
    }
    
    /**
     * 分析用户行为数据
     */
    private UserBehaviorProfile analyzeUserBehavior(Long userId) {
        UserBehaviorProfile profile = new UserBehaviorProfile();
        
        // 1. 分析订单历史
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Orders::getUserId, userId)
                   .and(w -> w.eq(Orders::getStatus, 1).or().eq(Orders::getStatus, 2)); // 已支付、已完成
        List<Orders> orders = ordersMapper.selectList(orderWrapper);
        profile.orderCount = orders.size();
        
        // 提取购买过的产品ID和分类
        profile.purchasedProductIds = orders.stream()
            .map(Orders::getProductId)
            .collect(Collectors.toSet());
        
        if (!profile.purchasedProductIds.isEmpty()) {
            List<Product> purchasedProducts = productMapper.selectBatchIds(profile.purchasedProductIds);
            profile.purchasedCategoryIds = purchasedProducts.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            // 计算平均购买价格
            double avgPrice = orders.stream()
                .map(Orders::getTotalAmount)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);
            profile.avgPurchasePrice = avgPrice;
        }
        
        // 2. 分析收藏记录
        LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(Favorite::getUserId, userId);
        List<Favorite> favorites = favoriteMapper.selectList(favoriteWrapper);
        profile.favoriteCount = favorites.size();
        
        profile.favoritedProductIds = favorites.stream()
            .map(Favorite::getProductId)
            .collect(Collectors.toSet());
        
        if (!profile.favoritedProductIds.isEmpty()) {
            List<Product> favoritedProducts = productMapper.selectBatchIds(profile.favoritedProductIds);
            profile.favoritedCategoryIds = favoritedProducts.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
        
        // 3. 分析评价记录
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getUserId, userId);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);
        profile.reviewCount = reviews.size();
        
        // 分析高评分产品（4星及以上）
        profile.highRatedProductIds = reviews.stream()
            .filter(r -> r.getRating() != null && r.getRating() >= 4)
            .map(Review::getProductId)
            .collect(Collectors.toSet());
        
        return profile;
    }
    
    /**
     * 获取候选产品池
     */
    private List<Product> getCandidateProducts(String categoryName, String priceRange) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查询上架的产品
        
        // 分类筛选
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            LambdaQueryWrapper<ProductCategory> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.like(ProductCategory::getName, categoryName.trim());
            List<ProductCategory> categories = categoryMapper.selectList(categoryWrapper);
            
            if (!categories.isEmpty()) {
                List<Long> categoryIds = categories.stream()
                    .map(ProductCategory::getId)
                    .collect(Collectors.toList());
                wrapper.in(Product::getCategoryId, categoryIds);
            }
        }
        
        // 价格范围筛选
        if (priceRange != null && priceRange.contains("-")) {
            String[] parts = priceRange.split("-");
            try {
                Double minPrice = Double.parseDouble(parts[0].trim());
                Double maxPrice = Double.parseDouble(parts[1].trim());
                wrapper.between(Product::getPrice, minPrice, maxPrice);
            } catch (NumberFormatException e) {
                log.warn("价格范围格式错误: {}", priceRange);
            }
        }
        
        // 限制数量，避免计算量过大
        wrapper.last("LIMIT 100");
        wrapper.orderByDesc(Product::getRecommend, Product::getRating, Product::getSales);
        
        return productMapper.selectList(wrapper);
    }
    
    /**
     * 计算推荐分数
     */
    private List<RecommendationScore> calculateRecommendationScores(
            List<Product> candidates, UserBehaviorProfile profile, Long userId) {
        
        List<RecommendationScore> scores = new ArrayList<>();
        
        for (Product product : candidates) {
            // 跳过已购买的产品
            if (profile.purchasedProductIds.contains(product.getId())) {
                continue;
            }
            
            RecommendationScore score = new RecommendationScore();
            score.product = product;
            score.totalScore = 0;
            score.reasons = new ArrayList<>();
            
            // 1. 基于分类偏好（30分）
            if (profile.purchasedCategoryIds.contains(product.getCategoryId())) {
                score.totalScore += 30;
                score.reasons.add("与您购买过的产品同类");
            } else if (profile.favoritedCategoryIds.contains(product.getCategoryId())) {
                score.totalScore += 20;
                score.reasons.add("与您收藏的产品同类");
            }
            
            // 2. 基于价格偏好（20分）
            if (profile.avgPurchasePrice > 0) {
                double priceScore = calculatePriceScore(
                    product.getPrice().doubleValue(), 
                    profile.avgPurchasePrice);
                score.totalScore += priceScore;
                if (priceScore > 10) {
                    score.reasons.add("价格符合您的消费习惯");
                }
            }
            
            // 3. 基于产品质量（25分）
            double qualityScore = 0;
            if (product.getRating() != null) {
                qualityScore += product.getRating().doubleValue() * 3; // 最高15分
            }
            if (product.getSales() != null && product.getSales() > 10) {
                qualityScore += Math.min(10, Math.log(product.getSales()) * 2); // 最高10分
            }
            score.totalScore += qualityScore;
            if (product.getRating() != null && product.getRating().doubleValue() >= 4.5) {
                score.reasons.add("高评分产品");
            }
            if (product.getSales() != null && product.getSales() > 50) {
                score.reasons.add("热门产品");
            }
            
            // 4. 协同过滤 - 查找相似用户喜欢的产品（15分）
            if (hasCollaborativeSignal(product.getId(), profile)) {
                score.totalScore += 15;
                score.reasons.add("与您兴趣相似的用户也喜欢");
            }
            
            // 5. 是否被收藏（10分）
            if (profile.favoritedProductIds.contains(product.getId())) {
                score.totalScore += 10;
                score.reasons.add("您已收藏");
            }
            
            // 6. 推荐标记加成（10分）
            if (product.getRecommend() != null && product.getRecommend() == 1) {
                score.totalScore += 10;
                score.reasons.add("平台推荐");
            }
            
            scores.add(score);
        }
        
        // 排序：分数高的在前
        scores.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));
        
        return scores;
    }
    
    /**
     * 计算价格匹配分数
     */
    private double calculatePriceScore(double productPrice, double avgPrice) {
        double ratio = productPrice / avgPrice;
        // 价格在平均价格的0.5-1.5倍内，得分最高
        if (ratio >= 0.5 && ratio <= 1.5) {
            return 20;
        } else if (ratio >= 0.3 && ratio <= 2.0) {
            return 10;
        } else if (ratio >= 0.1 && ratio <= 3.0) {
            return 5;
        }
        return 0;
    }
    
    /**
     * 检查协同过滤信号
     * 简化版：检查购买过同类产品的用户是否也购买/收藏了当前产品
     */
    private boolean hasCollaborativeSignal(Long productId, UserBehaviorProfile profile) {
        // 如果用户没有历史行为，无法协同过滤
        if (profile.purchasedProductIds.isEmpty()) {
            return false;
        }
        
        // 查找购买过相同产品的其他用户
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Orders::getProductId, profile.purchasedProductIds)
               .and(w -> w.eq(Orders::getStatus, 1).or().eq(Orders::getStatus, 2))
               .select(Orders::getUserId)
               .groupBy(Orders::getUserId);
        
        List<Orders> similarUserOrders = ordersMapper.selectList(wrapper);
        Set<Long> similarUserIds = similarUserOrders.stream()
            .map(Orders::getUserId)
            .filter(uid -> !uid.equals(profile.userId))
            .collect(Collectors.toSet());
        
        if (similarUserIds.isEmpty()) {
            return false;
        }
        
        // 检查这些用户是否购买/收藏了当前产品
        LambdaQueryWrapper<Orders> productOrderWrapper = new LambdaQueryWrapper<>();
        productOrderWrapper.eq(Orders::getProductId, productId)
                          .in(Orders::getUserId, similarUserIds)
                          .and(w -> w.eq(Orders::getStatus, 1).or().eq(Orders::getStatus, 2));
        
        long count = ordersMapper.selectCount(productOrderWrapper);
        return count > 0;
    }
    
    /**
     * 转换为返回格式
     */
    private Map<String, Object> convertToResult(RecommendationScore score, UserBehaviorProfile profile) {
        Product product = score.product;
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("title", product.getTitle());
        map.put("price", product.getPrice());
        map.put("originalPrice", product.getOriginalPrice());
        map.put("region", product.getRegion());
        map.put("category", getCategoryName(product.getCategoryId()));
        map.put("rating", product.getRating());
        map.put("sales", product.getSales());
        map.put("coverImage", product.getCoverImage());
        map.put("tags", product.getTags());
        
        String description = product.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        map.put("description", description);
        
        // 推荐分数和理由
        map.put("recommendScore", Math.round(score.totalScore));
        map.put("recommendReasons", score.reasons);
        
        map.put("_source", "smart_recommendation");
        
        return map;
    }
    
    /**
     * 构建推荐理由
     */
    private String buildRecommendationReason(UserBehaviorProfile profile, 
                                            List<RecommendationScore> recommendations) {
        StringBuilder reason = new StringBuilder();
        
        if (profile.orderCount > 0 || profile.favoriteCount > 0 || profile.reviewCount > 0) {
            reason.append("根据您的");
            List<String> behaviors = new ArrayList<>();
            if (profile.orderCount > 0) {
                behaviors.add(profile.orderCount + "次购买记录");
            }
            if (profile.favoriteCount > 0) {
                behaviors.add(profile.favoriteCount + "个收藏");
            }
            if (profile.reviewCount > 0) {
                behaviors.add(profile.reviewCount + "条评价");
            }
            reason.append(String.join("、", behaviors));
            reason.append("，为您智能推荐以下产品");
        } else {
            reason.append("为您推荐以下热门优质产品");
        }
        
        return reason.toString();
    }
    
    /**
     * 获取分类名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "其他";
        try {
            ProductCategory category = categoryMapper.selectById(categoryId);
            if (category != null && (category.getDeleted() == null || category.getDeleted() == 0)) {
                return category.getName();
            }
        } catch (Exception e) {
            log.warn("查询分类名称失败: categoryId={}", categoryId, e);
        }
        return "其他";
    }
    
    // 辅助方法
    private Integer extractInteger(Map<String, Object> params, String key, Integer defaultValue) {
        Object value = params.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        return defaultValue;
    }
    
    /**
     * 用户行为画像
     */
    private static class UserBehaviorProfile {
        Long userId;
        int orderCount = 0;
        int favoriteCount = 0;
        int reviewCount = 0;
        
        Set<Long> purchasedProductIds = new HashSet<>();
        Set<Long> favoritedProductIds = new HashSet<>();
        Set<Long> highRatedProductIds = new HashSet<>();
        
        Set<Long> purchasedCategoryIds = new HashSet<>();
        Set<Long> favoritedCategoryIds = new HashSet<>();
        
        double avgPurchasePrice = 0;
    }
    
    /**
     * 推荐分数
     */
    private static class RecommendationScore {
        Product product;
        double totalScore;
        List<String> reasons;
    }
}

