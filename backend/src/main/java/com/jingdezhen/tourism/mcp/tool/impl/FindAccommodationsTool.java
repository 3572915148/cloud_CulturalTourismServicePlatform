package com.jingdezhen.tourism.mcp.tool.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mcp.model.ToolDefinition;
import com.jingdezhen.tourism.mcp.tool.AbstractMcpTool;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 住宿推荐工具
 * 根据位置、价格、评分等条件推荐合适的酒店
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FindAccommodationsTool extends AbstractMcpTool {
    
    private final ProductMapper productMapper;
    
    @Override
    public String getName() {
        return "find_accommodations";
    }
    
    @Override
    public String getDescription() {
        return "根据位置、价格区间、评分、设施等条件搜索并推荐合适的住宿。支持按距离、价格、评分排序。";
    }
    
    @Override
    public ToolDefinition getDefinition() {
        Map<String, ToolDefinition.PropertySchema> properties = new LinkedHashMap<>();
        
        properties.put("region", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("所在区域，例如：昌江区、珠山区、浮梁县等")
                .build());
        
        properties.put("checkInDate", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("入住日期，格式：YYYY-MM-DD")
                .build());
        
        properties.put("checkOutDate", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("退房日期，格式：YYYY-MM-DD")
                .build());
        
        properties.put("minPrice", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("最低价格（元/晚）")
                .minimum(0)
                .build());
        
        properties.put("maxPrice", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("最高价格（元/晚）")
                .build());
        
        properties.put("minRating", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("最低评分（1-5分）")
                .minimum(1)
                .maximum(5)
                .build());
        
        properties.put("facilities", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("设施要求，多个用逗号分隔，例如：免费WiFi,停车场,早餐")
                .build());
        
        properties.put("hotelType", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("酒店类型")
                .enumValues(Arrays.asList("经济型", "舒适型", "高档型", "豪华型", "民宿", "客栈"))
                .build());
        
        properties.put("sortBy", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("排序方式")
                .enumValues(Arrays.asList("price_asc", "price_desc", "rating_desc", "default"))
                .defaultValue("default")
                .build());
        
        properties.put("page", ToolDefinition.PropertySchema.builder()
                .type("integer")
                .description("页码（从1开始）")
                .minimum(1)
                .defaultValue(1)
                .build());
        
        properties.put("pageSize", ToolDefinition.PropertySchema.builder()
                .type("integer")
                .description("每页数量")
                .minimum(1)
                .maximum(50)
                .defaultValue(10)
                .build());
        
        ToolDefinition.InputSchema inputSchema = ToolDefinition.InputSchema.builder()
                .type("object")
                .properties(properties)
                .required(Collections.emptyList())
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
        String region = getStringParam(params, "region", null);
        String checkInDate = getStringParam(params, "checkInDate", null);
        String checkOutDate = getStringParam(params, "checkOutDate", null);
        Double minPrice = getDoubleParam(params, "minPrice", null);
        Double maxPrice = getDoubleParam(params, "maxPrice", null);
        Double minRating = getDoubleParam(params, "minRating", null);
        String facilities = getStringParam(params, "facilities", null);
        String hotelType = getStringParam(params, "hotelType", null);
        String sortBy = getStringParam(params, "sortBy", "default");
        Integer page = getIntParam(params, "page", 1);
        Integer pageSize = getIntParam(params, "pageSize", 10);
        
        log.info("搜索住宿 - 区域: {}, 入住: {}, 退房: {}, 价格: {}-{}, 评分: {}, 类型: {}", 
                region, checkInDate, checkOutDate, minPrice, maxPrice, minRating, hotelType);
        
        // 计算住宿天数
        Integer nights = calculateNights(checkInDate, checkOutDate);
        
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询酒店（假设酒店的category_id为2）
        queryWrapper.eq(Product::getCategoryId, 2L);
        
        // 只查询上架的产品
        queryWrapper.eq(Product::getStatus, 1);
        
        // 区域筛选
        if (StringUtils.hasText(region)) {
            queryWrapper.eq(Product::getRegion, region);
        }
        
        // 价格范围
        if (minPrice != null) {
            queryWrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le(Product::getPrice, maxPrice);
        }
        
        // 评分筛选
        if (minRating != null) {
            queryWrapper.ge(Product::getRating, minRating);
        }
        
        // 酒店类型筛选
        if (StringUtils.hasText(hotelType)) {
            queryWrapper.like(Product::getTags, hotelType);
        }
        
        // 设施筛选
        if (StringUtils.hasText(facilities)) {
            String[] facilityArray = facilities.split(",");
            for (String facility : facilityArray) {
                queryWrapper.like(Product::getFeatures, facility.trim());
            }
        }
        
        // 排序
        switch (sortBy) {
            case "price_asc":
                queryWrapper.orderByAsc(Product::getPrice);
                break;
            case "price_desc":
                queryWrapper.orderByDesc(Product::getPrice);
                break;
            case "rating_desc":
                queryWrapper.orderByDesc(Product::getRating);
                break;
            default:
                queryWrapper.orderByDesc(Product::getRecommend)
                           .orderByDesc(Product::getRating);
        }
        
        // 执行分页查询
        Page<Product> pageObj = new Page<>(page, pageSize);
        IPage<Product> result = productMapper.selectPage(pageObj, queryWrapper);
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("pageSize", result.getSize());
        response.put("totalPages", result.getPages());
        
        if (nights != null) {
            response.put("nights", nights);
        }
        
        // 转换酒店列表
        List<Map<String, Object>> accommodations = new ArrayList<>();
        for (Product product : result.getRecords()) {
            Map<String, Object> accommodation = new HashMap<>();
            accommodation.put("id", product.getId());
            accommodation.put("name", product.getTitle());
            accommodation.put("description", product.getDescription());
            accommodation.put("coverImage", product.getCoverImage());
            accommodation.put("pricePerNight", product.getPrice());
            accommodation.put("rating", product.getRating());
            accommodation.put("region", product.getRegion());
            accommodation.put("address", product.getAddress());
            accommodation.put("tags", product.getTags());
            accommodation.put("features", product.getFeatures());
            accommodation.put("sales", product.getSales());
            
            // 计算总价（如果提供了入住天数）
            if (nights != null) {
                double totalPrice = product.getPrice().doubleValue() * nights;
                accommodation.put("totalPrice", totalPrice);
            }
            
            accommodations.add(accommodation);
        }
        
        response.put("accommodations", accommodations);
        
        // 添加搜索建议
        if (accommodations.isEmpty()) {
            response.put("suggestion", "未找到符合条件的住宿，建议：\n" +
                    "1. 扩大搜索区域\n" +
                    "2. 调整价格范围\n" +
                    "3. 降低评分或设施要求");
        } else {
            // 提供预订建议
            List<String> bookingTips = new ArrayList<>();
            bookingTips.add("建议提前3-7天预订，可享受更优惠的价格");
            bookingTips.add("注意查看酒店的取消政策和入住时间");
            bookingTips.add("旺季（节假日）建议至少提前2周预订");
            response.put("bookingTips", bookingTips);
        }
        
        return response;
    }
    
    /**
     * 计算住宿天数
     */
    private Integer calculateNights(String checkInDate, String checkOutDate) {
        if (!StringUtils.hasText(checkInDate) || !StringUtils.hasText(checkOutDate)) {
            return null;
        }
        
        try {
            java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInDate);
            java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutDate);
            
            long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            return (int) days;
            
        } catch (Exception e) {
            log.warn("日期格式错误: checkIn={}, checkOut={}", checkInDate, checkOutDate);
            return null;
        }
    }
    
    @Override
    public void validateParams(Map<String, Object> params) {
        // 验证分页参数
        Integer page = getIntParam(params, "page", 1);
        Integer pageSize = getIntParam(params, "pageSize", 10);
        
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        
        if (pageSize < 1 || pageSize > 50) {
            throw new IllegalArgumentException("每页数量必须在1-50之间");
        }
        
        // 验证价格范围
        Double minPrice = getDoubleParam(params, "minPrice", null);
        Double maxPrice = getDoubleParam(params, "maxPrice", null);
        
        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("最低价格不能为负数");
        }
        
        if (maxPrice != null && maxPrice < 0) {
            throw new IllegalArgumentException("最高价格不能为负数");
        }
        
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("最低价格不能大于最高价格");
        }
        
        // 验证日期
        String checkInDate = getStringParam(params, "checkInDate", null);
        String checkOutDate = getStringParam(params, "checkOutDate", null);
        
        if (StringUtils.hasText(checkInDate) && StringUtils.hasText(checkOutDate)) {
            try {
                java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInDate);
                java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutDate);
                
                if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                    throw new IllegalArgumentException("退房日期必须晚于入住日期");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("日期格式错误，请使用YYYY-MM-DD格式");
            }
        }
    }
}

