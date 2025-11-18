package com.jingdezhen.tourism.langchain.tools;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.entity.*;
import com.jingdezhen.tourism.mapper.*;
import dev.langchain4j.agent.tool.Tool;
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
public class RecommendationTools {
    
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final OrdersMapper ordersMapper;
    private final FavoriteMapper favoriteMapper;
    private final ReviewMapper reviewMapper;
    
    /**
     * 智能推荐工具
     * 基于用户历史行为进行个性化推荐
     */
    @Tool("基于用户历史行为（订单、收藏、评价）进行智能个性化推荐。适用于\"为我推荐\"、\"根据我的喜好推荐\"等场景")
    public String smartRecommendation(
            Long userId,            // 用户ID（必需）
            Integer count,          // 推荐数量（可选，默认5个）
            String categoryName,    // 限定分类（可选）
            String priceRange) {    // 价格范围（可选，如：100-500）
        
        try {
            log.info("🎯 [LangChain4j] 智能推荐: userId={}, count={}", userId, count);
            
            if (count == null || count <= 0) count = 5;
            if (count > 10) count = 10;
            
            // 1. 分析用户行为
            UserBehaviorProfile profile = analyzeUserBehavior(userId);
            log.info("用户行为: 订单{}个, 收藏{}个, 评价{}个", 
                profile.orderCount, profile.favoriteCount, profile.reviewCount);
            
            // 2. 获取候选产品
            List<Product> candidates = getCandidateProducts(categoryName, priceRange);
            log.info("候选产品: {}个", candidates.size());
            
            // 3. 计算推荐分数
            List<RecommendationScore> scores = calculateScores(candidates, profile, userId);
            
            // 4. 选择Top N
            List<RecommendationScore> topRecommendations = scores.stream()
                .limit(count)
                .collect(Collectors.toList());
            
            // 5. 转换为返回格式
            List<Map<String, Object>> result = topRecommendations.stream()
                .map(score -> convertToResult(score, profile))
                .collect(Collectors.toList());
            
            log.info("✅ 推荐完成，共{}个产品", result.size());
            
            // 6. 构建推荐理由
            String reason = buildReason(profile, topRecommendations);
            
            return JSON.toJSONString(Map.of(
                    "success", true,
                    "data", result,
                    "message", reason
            ));
            
        } catch (Exception e) {
            log.error("❌ 智能推荐失败", e);
            return JSON.toJSONString(Map.of(
                    "success", false,
                    "message", "智能推荐失败：" + e.getMessage(),
                    "errorCode", "SMART_RECOMMENDATION_ERROR"
            ));
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    private UserBehaviorProfile analyzeUserBehavior(Long userId) {
        UserBehaviorProfile profile = new UserBehaviorProfile();
        profile.userId = userId;
        
        // 分析订单
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Orders::getUserId, userId)
                   .and(w -> w.eq(Orders::getStatus, 1).or().eq(Orders::getStatus, 2));
        List<Orders> orders = ordersMapper.selectList(orderWrapper);
        profile.orderCount = orders.size();
        
        profile.purchasedProductIds = orders.stream()
            .map(Orders::getProductId)
            .collect(Collectors.toSet());
        
        if (!profile.purchasedProductIds.isEmpty()) {
            List<Product> purchasedProducts = productMapper.selectBatchIds(profile.purchasedProductIds);
            profile.purchasedCategoryIds = purchasedProducts.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            double avgPrice = orders.stream()
                .map(Orders::getTotalAmount)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);
            profile.avgPurchasePrice = avgPrice;
        }
        
        // 分析收藏
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
        
        // 分析评价
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getUserId, userId);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);
        profile.reviewCount = reviews.size();
        
        profile.highRatedProductIds = reviews.stream()
            .filter(r -> r.getRating() != null && r.getRating() >= 4)
            .map(Review::getProductId)
            .collect(Collectors.toSet());
        
        return profile;
    }
    
    private List<Product> getCandidateProducts(String categoryName, String priceRange) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        
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
        
        wrapper.last("LIMIT 100");
        wrapper.orderByDesc(Product::getRecommend, Product::getRating, Product::getSales);
        
        return productMapper.selectList(wrapper);
    }
    
    private List<RecommendationScore> calculateScores(
            List<Product> candidates, UserBehaviorProfile profile, Long userId) {
        
        List<RecommendationScore> scores = new ArrayList<>();
        
        for (Product product : candidates) {
            if (profile.purchasedProductIds.contains(product.getId())) {
                continue;
            }
            
            RecommendationScore score = new RecommendationScore();
            score.product = product;
            score.totalScore = 0;
            score.reasons = new ArrayList<>();
            
            // 1. 分类偏好（30分）
            if (profile.purchasedCategoryIds.contains(product.getCategoryId())) {
                score.totalScore += 30;
                score.reasons.add("与您购买过的产品同类");
            } else if (profile.favoritedCategoryIds.contains(product.getCategoryId())) {
                score.totalScore += 20;
                score.reasons.add("与您收藏的产品同类");
            }
            
            // 2. 价格偏好（20分）
            if (profile.avgPurchasePrice > 0) {
                double priceScore = calculatePriceScore(
                    product.getPrice().doubleValue(), 
                    profile.avgPurchasePrice);
                score.totalScore += priceScore;
                if (priceScore > 10) {
                    score.reasons.add("价格符合您的消费习惯");
                }
            }
            
            // 3. 产品质量（25分）
            double qualityScore = 0;
            if (product.getRating() != null) {
                qualityScore += product.getRating().doubleValue() * 3;
            }
            if (product.getSales() != null && product.getSales() > 10) {
                qualityScore += Math.min(10, Math.log(product.getSales()) * 2);
            }
            score.totalScore += qualityScore;
            
            if (product.getRating() != null && product.getRating().doubleValue() >= 4.5) {
                score.reasons.add("高评分产品");
            }
            if (product.getSales() != null && product.getSales() > 50) {
                score.reasons.add("热门产品");
            }
            
            // 4. 协同过滤（15分）
            if (hasCollaborativeSignal(product.getId(), profile)) {
                score.totalScore += 15;
                score.reasons.add("与您兴趣相似的用户也喜欢");
            }
            
            // 5. 已收藏（10分）
            if (profile.favoritedProductIds.contains(product.getId())) {
                score.totalScore += 10;
                score.reasons.add("您已收藏");
            }
            
            // 6. 推荐标记（10分）
            if (product.getRecommend() != null && product.getRecommend() == 1) {
                score.totalScore += 10;
                score.reasons.add("平台推荐");
            }
            
            scores.add(score);
        }
        
        scores.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));
        
        return scores;
    }
    
    private double calculatePriceScore(double productPrice, double avgPrice) {
        double ratio = productPrice / avgPrice;
        if (ratio >= 0.5 && ratio <= 1.5) return 20;
        if (ratio >= 0.3 && ratio <= 2.0) return 10;
        if (ratio >= 0.1 && ratio <= 3.0) return 5;
        return 0;
    }
    
    private boolean hasCollaborativeSignal(Long productId, UserBehaviorProfile profile) {
        if (profile.purchasedProductIds.isEmpty()) return false;
        
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
        
        if (similarUserIds.isEmpty()) return false;
        
        LambdaQueryWrapper<Orders> productOrderWrapper = new LambdaQueryWrapper<>();
        productOrderWrapper.eq(Orders::getProductId, productId)
                          .in(Orders::getUserId, similarUserIds)
                          .and(w -> w.eq(Orders::getStatus, 1).or().eq(Orders::getStatus, 2));
        
        long count = ordersMapper.selectCount(productOrderWrapper);
        return count > 0;
    }
    
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
        
        map.put("recommendScore", Math.round(score.totalScore));
        map.put("recommendReasons", score.reasons);
        map.put("_source", "smart_recommendation");
        
        return map;
    }
    
    private String buildReason(UserBehaviorProfile profile, List<RecommendationScore> recommendations) {
        StringBuilder reason = new StringBuilder();
        
        if (profile.orderCount > 0 || profile.favoriteCount > 0 || profile.reviewCount > 0) {
            reason.append("根据您的");
            List<String> behaviors = new ArrayList<>();
            if (profile.orderCount > 0) behaviors.add(profile.orderCount + "次购买记录");
            if (profile.favoriteCount > 0) behaviors.add(profile.favoriteCount + "个收藏");
            if (profile.reviewCount > 0) behaviors.add(profile.reviewCount + "条评价");
            reason.append(String.join("、", behaviors));
            reason.append("，为您智能推荐以下产品");
        } else {
            reason.append("为您推荐以下热门优质产品");
        }
        
        return reason.toString();
    }
    
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "其他";
        try {
            ProductCategory category = categoryMapper.selectById(categoryId);
            if (category != null) return category.getName();
        } catch (Exception e) {
            log.warn("查询分类名称失败: categoryId={}", categoryId, e);
        }
        return "其他";
    }
    
    // ==================== 内部类 ====================
    
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
    
    private static class RecommendationScore {
        Product product;
        double totalScore;
        List<String> reasons;
    }
}

