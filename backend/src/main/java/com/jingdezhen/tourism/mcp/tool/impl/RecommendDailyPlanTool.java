package com.jingdezhen.tourism.mcp.tool.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mcp.model.ToolDefinition;
import com.jingdezhen.tourism.mcp.tool.AbstractMcpTool;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 每日行程推荐工具
 * 根据用户偏好和预算生成合理的每日旅游行程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendDailyPlanTool extends AbstractMcpTool {
    
    private final ProductMapper productMapper;
    
    @Override
    public String getName() {
        return "recommend_daily_plan";
    }
    
    @Override
    public String getDescription() {
        return "根据用户的天数、预算、兴趣偏好等条件，智能生成每日旅游行程计划。包括景点、餐饮、住宿的合理安排和时间规划。";
    }
    
    @Override
    public ToolDefinition getDefinition() {
        Map<String, ToolDefinition.PropertySchema> properties = new LinkedHashMap<>();
        
        properties.put("days", ToolDefinition.PropertySchema.builder()
                .type("integer")
                .description("旅游天数")
                .minimum(1)
                .maximum(30)
                .build());
        
        properties.put("budget", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("总预算（元）")
                .minimum(0)
                .build());
        
        properties.put("interests", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("兴趣偏好，多个用逗号分隔，例如：陶瓷文化,历史古迹,自然风光")
                .build());
        
        properties.put("pace", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("旅游节奏")
                .enumValues(Arrays.asList("relaxed", "moderate", "intense"))
                .defaultValue("moderate")
                .build());
        
        properties.put("startDate", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("开始日期，格式：YYYY-MM-DD")
                .build());
        
        ToolDefinition.InputSchema inputSchema = ToolDefinition.InputSchema.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("days"))
                .build();
        
        return ToolDefinition.builder()
                .name(getName())
                .description(getDescription())
                .inputSchema(inputSchema)
                .build();
    }
    
    @Override
    protected Object doExecute(Map<String, Object> params) {
        // 解析参数
        Integer days = getIntParam(params, "days", 3);
        Double budget = getDoubleParam(params, "budget", null);
        String interests = getStringParam(params, "interests", null);
        String pace = getStringParam(params, "pace", "moderate");
        String startDate = getStringParam(params, "startDate", null);
        
        log.info("生成每日行程 - 天数: {}, 预算: {}, 兴趣: {}, 节奏: {}", 
                days, budget, interests, pace);
        
        // 根据节奏确定每天的景点数量
        int attractionsPerDay;
        switch (pace) {
            case "relaxed":
                attractionsPerDay = 2;
                break;
            case "intense":
                attractionsPerDay = 4;
                break;
            default:
                attractionsPerDay = 3;
        }
        
        // 获取推荐景点
        List<Product> attractions = getRecommendedAttractions(interests, budget, days * attractionsPerDay);
        
        // 获取餐厅
        List<Product> restaurants = getRecommendedRestaurants(budget, days * 2); // 每天两餐
        
        // 获取酒店
        List<Product> hotels = getRecommendedHotels(budget, days - 1); // 需要days-1晚住宿
        
        // 生成行程计划
        List<Map<String, Object>> dailyPlans = new ArrayList<>();
        
        for (int day = 1; day <= days; day++) {
            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("day", day);
            dayPlan.put("date", calculateDate(startDate, day - 1));
            
            // 上午景点
            List<Map<String, Object>> morningActivities = new ArrayList<>();
            int startIdx = (day - 1) * attractionsPerDay;
            if (startIdx < attractions.size()) {
                morningActivities.add(createActivity("09:00-11:30", attractions.get(startIdx), "景点游览"));
            }
            
            // 午餐
            int lunchIdx = (day - 1) * 2;
            if (lunchIdx < restaurants.size()) {
                morningActivities.add(createActivity("11:30-13:00", restaurants.get(lunchIdx), "午餐"));
            }
            
            // 下午景点
            List<Map<String, Object>> afternoonActivities = new ArrayList<>();
            if (attractionsPerDay >= 2 && startIdx + 1 < attractions.size()) {
                afternoonActivities.add(createActivity("13:30-16:00", attractions.get(startIdx + 1), "景点游览"));
            }
            
            // 晚餐
            int dinnerIdx = (day - 1) * 2 + 1;
            if (dinnerIdx < restaurants.size()) {
                afternoonActivities.add(createActivity("18:00-19:30", restaurants.get(dinnerIdx), "晚餐"));
            }
            
            // 晚上景点或活动
            List<Map<String, Object>> eveningActivities = new ArrayList<>();
            if (attractionsPerDay >= 3 && startIdx + 2 < attractions.size()) {
                eveningActivities.add(createActivity("20:00-21:30", attractions.get(startIdx + 2), "夜游"));
            } else {
                Map<String, Object> freeActivity = new HashMap<>();
                freeActivity.put("time", "20:00-22:00");
                freeActivity.put("type", "自由活动");
                freeActivity.put("suggestion", "可在酒店附近散步或体验当地夜生活");
                eveningActivities.add(freeActivity);
            }
            
            // 住宿
            if (day < days && day - 1 < hotels.size()) {
                Map<String, Object> accommodation = new HashMap<>();
                accommodation.put("hotel", hotels.get(day - 1).getTitle());
                accommodation.put("address", hotels.get(day - 1).getAddress());
                accommodation.put("price", hotels.get(day - 1).getPrice());
                accommodation.put("rating", hotels.get(day - 1).getRating());
                dayPlan.put("accommodation", accommodation);
            }
            
            dayPlan.put("morning", morningActivities);
            dayPlan.put("afternoon", afternoonActivities);
            dayPlan.put("evening", eveningActivities);
            
            // 计算当日费用
            double dayCost = calculateDayCost(morningActivities, afternoonActivities, eveningActivities);
            if (day < days && day - 1 < hotels.size()) {
                dayCost += hotels.get(day - 1).getPrice().doubleValue();
            }
            dayPlan.put("estimatedCost", dayCost);
            
            dailyPlans.add(dayPlan);
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalDays", days);
        result.put("startDate", startDate);
        result.put("pace", pace);
        result.put("dailyPlans", dailyPlans);
        
        // 计算总费用
        double totalCost = dailyPlans.stream()
                .mapToDouble(plan -> (Double) plan.get("estimatedCost"))
                .sum();
        result.put("totalEstimatedCost", totalCost);
        
        if (budget != null) {
            result.put("budget", budget);
            result.put("budgetRemaining", budget - totalCost);
            result.put("budgetStatus", totalCost <= budget ? "在预算内" : "超出预算");
        }
        
        // 添加温馨提示
        List<String> tips = new ArrayList<>();
        tips.add("建议提前预订景点门票和酒店，可享受优惠");
        tips.add("景德镇以陶瓷文化闻名，建议至少安排一天参观陶瓷博物馆和古窑");
        tips.add("注意天气变化，备好雨具和防晒用品");
        tips.add("尝试当地特色美食：饺子粑、碱水粑、冷粉等");
        result.put("tips", tips);
        
        return result;
    }
    
    /**
     * 获取推荐的景点
     */
    private List<Product> getRecommendedAttractions(String interests, Double budget, int limit) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, 1L) // 景点分类
                .eq(Product::getStatus, 1);
        
        // 如果指定了兴趣，尝试匹配标签
        if (StringUtils.hasText(interests)) {
            String[] interestArray = interests.split(",");
            queryWrapper.and(w -> {
                for (int i = 0; i < interestArray.length; i++) {
                    if (i > 0) {
                        w.or();
                    }
                    w.like(Product::getTags, interestArray[i].trim());
                }
            });
        }
        
        queryWrapper.orderByDesc(Product::getRecommend)
                .orderByDesc(Product::getRating)
                .last("LIMIT " + limit);
        
        return productMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取推荐的餐厅
     */
    private List<Product> getRecommendedRestaurants(Double budget, int limit) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, 3L) // 餐饮分类
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getRating)
                .last("LIMIT " + limit);
        
        return productMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取推荐的酒店
     */
    private List<Product> getRecommendedHotels(Double budget, int limit) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, 2L) // 酒店分类
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getRating)
                .last("LIMIT " + limit);
        
        return productMapper.selectList(queryWrapper);
    }
    
    /**
     * 创建活动项
     */
    private Map<String, Object> createActivity(String time, Product product, String type) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("time", time);
        activity.put("type", type);
        activity.put("name", product.getTitle());
        activity.put("description", product.getDescription());
        activity.put("address", product.getAddress());
        activity.put("price", product.getPrice());
        activity.put("rating", product.getRating());
        activity.put("tags", product.getTags());
        return activity;
    }
    
    /**
     * 计算日期
     */
    private String calculateDate(String startDate, int daysToAdd) {
        if (!StringUtils.hasText(startDate)) {
            return null;
        }
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(startDate);
            return date.plusDays(daysToAdd).toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 计算当日费用
     */
    @SafeVarargs
    private final double calculateDayCost(List<Map<String, Object>>... activityLists) {
        double total = 0;
        for (List<Map<String, Object>> activities : activityLists) {
            for (Map<String, Object> activity : activities) {
                Object price = activity.get("price");
                if (price instanceof BigDecimal) {
                    total += ((BigDecimal) price).doubleValue();
                } else if (price instanceof Number) {
                    total += ((Number) price).doubleValue();
                }
            }
        }
        return total;
    }
    
    @Override
    public void validateParams(Map<String, Object> params) {
        Integer days = getIntParam(params, "days", null);
        if (days == null || days < 1 || days > 30) {
            throw new IllegalArgumentException("旅游天数必须在1-30天之间");
        }
        
        Double budget = getDoubleParam(params, "budget", null);
        if (budget != null && budget < 0) {
            throw new IllegalArgumentException("预算不能为负数");
        }
    }
}

