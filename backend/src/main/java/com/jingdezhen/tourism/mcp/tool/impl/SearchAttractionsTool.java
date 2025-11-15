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
 * 景点搜索工具
 * 根据条件搜索景点信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchAttractionsTool extends AbstractMcpTool {
    
    private final ProductMapper productMapper;
    
    @Override
    public String getName() {
        return "search_attractions";
    }
    
    @Override
    public String getDescription() {
        return "根据关键词、价格范围、评分等条件搜索景点。支持分页查询，返回景点详细信息包括名称、价格、评分、地址、图片等。";
    }
    
    @Override
    public ToolDefinition getDefinition() {
        Map<String, ToolDefinition.PropertySchema> properties = new LinkedHashMap<>();
        
        properties.put("keyword", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("搜索关键词，可以是景点名称或描述中的内容")
                .build());
        
        properties.put("region", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("所在区域，例如：昌江区、珠山区、浮梁县等")
                .build());
        
        properties.put("minPrice", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("最低价格（元）")
                .minimum(0)
                .build());
        
        properties.put("maxPrice", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("最高价格（元）")
                .build());
        
        properties.put("minRating", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("最低评分（1-5分）")
                .minimum(1)
                .maximum(5)
                .build());
        
        properties.put("sortBy", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("排序方式")
                .enumValues(Arrays.asList("price_asc", "price_desc", "rating_desc", "sales_desc", "default"))
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
                .maximum(100)
                .defaultValue(10)
                .build());
        
        ToolDefinition.InputSchema inputSchema = ToolDefinition.InputSchema.builder()
                .type("object")
                .properties(properties)
                .required(Collections.emptyList()) // 所有参数都是可选的
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
        String keyword = getStringParam(params, "keyword", null);
        String region = getStringParam(params, "region", null);
        Double minPrice = getDoubleParam(params, "minPrice", null);
        Double maxPrice = getDoubleParam(params, "maxPrice", null);
        Double minRating = getDoubleParam(params, "minRating", null);
        String sortBy = getStringParam(params, "sortBy", "default");
        Integer page = getIntParam(params, "page", 1);
        Integer pageSize = getIntParam(params, "pageSize", 10);
        
        log.info("搜索景点 - 关键词: {}, 区域: {}, 价格: {}-{}, 评分: {}, 排序: {}, 分页: {}/{}", 
                keyword, region, minPrice, maxPrice, minRating, sortBy, page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询景点（假设景点的category_id为1，或根据实际情况调整）
        queryWrapper.eq(Product::getCategoryId, 1L);
        
        // 只查询上架的产品
        queryWrapper.eq(Product::getStatus, 1);
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Product::getTitle, keyword)
                    .or()
                    .like(Product::getDescription, keyword)
            );
        }
        
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
            case "sales_desc":
                queryWrapper.orderByDesc(Product::getSales);
                break;
            default:
                // 默认排序：推荐优先，然后按创建时间倒序
                queryWrapper.orderByDesc(Product::getRecommend)
                           .orderByDesc(Product::getCreateTime);
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
        
        // 转换产品列表为简化的格式
        List<Map<String, Object>> attractions = new ArrayList<>();
        for (Product product : result.getRecords()) {
            Map<String, Object> attraction = new HashMap<>();
            attraction.put("id", product.getId());
            attraction.put("title", product.getTitle());
            attraction.put("description", product.getDescription());
            attraction.put("coverImage", product.getCoverImage());
            attraction.put("price", product.getPrice());
            attraction.put("originalPrice", product.getOriginalPrice());
            attraction.put("rating", product.getRating());
            attraction.put("sales", product.getSales());
            attraction.put("region", product.getRegion());
            attraction.put("address", product.getAddress());
            attraction.put("tags", product.getTags());
            attraction.put("features", product.getFeatures());
            attraction.put("recommend", product.getRecommend() == 1);
            
            attractions.add(attraction);
        }
        
        response.put("attractions", attractions);
        
        // 添加搜索建议
        if (attractions.isEmpty()) {
            response.put("suggestion", "未找到符合条件的景点，建议：\n" +
                    "1. 尝试修改搜索关键词\n" +
                    "2. 调整价格范围\n" +
                    "3. 降低评分要求");
        }
        
        return response;
    }
    
    @Override
    public void validateParams(Map<String, Object> params) {
        // 验证分页参数
        Integer page = getIntParam(params, "page", 1);
        Integer pageSize = getIntParam(params, "pageSize", 10);
        
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("每页数量必须在1-100之间");
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
        
        // 验证评分范围
        Double minRating = getDoubleParam(params, "minRating", null);
        if (minRating != null && (minRating < 1 || minRating > 5)) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
    }
}

