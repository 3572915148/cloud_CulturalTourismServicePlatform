package com.jingdezhen.tourism.agent.tool.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.entity.ProductCategory;
import com.jingdezhen.tourism.mapper.ProductCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取产品分类工具
 * 用于查询系统中的产品分类信息
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductCategoriesTool implements AgentTool {
    
    private final ProductCategoryMapper categoryMapper;
    
    @Override
    public String getName() {
        return "get_product_categories";
    }
    
    @Override
    public String getDescription() {
        return "获取产品分类列表。可以获取所有分类，或根据父分类ID获取子分类。分类信息包括ID、名称、图标等。";
    }
    
    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "parentId": {
                    "type": "integer",
                    "description": "父分类ID，0或不传表示获取顶级分类，传具体ID表示获取该分类的子分类"
                },
                "includeChildren": {
                    "type": "boolean",
                    "description": "是否包含子分类信息，默认false",
                    "default": false
                }
            },
            "required": []
        }
        """;
    }
    
    @Override
    public String getCategory() {
        return "product";
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("📂 获取产品分类: userId={}, params={}", userId, parameters);
            
            // 提取参数
            Long parentId = extractLong(parameters, "parentId");
            Boolean includeChildren = extractBoolean(parameters, "includeChildren", false);
            
            // 构建查询条件
            LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
            
            // 如果指定了parentId，查询该父分类下的子分类；否则查询顶级分类（parentId=0）
            if (parentId != null) {
                wrapper.eq(ProductCategory::getParentId, parentId);
            } else {
                wrapper.eq(ProductCategory::getParentId, 0L);
            }
            
            // 排序：按sortOrder升序，然后按ID升序
            wrapper.orderByAsc(ProductCategory::getSortOrder)
                   .orderByAsc(ProductCategory::getId);
            
            // 查询分类
            List<ProductCategory> categories = categoryMapper.selectList(wrapper);
            
            if (categories.isEmpty()) {
                log.info("❌ 未找到分类");
                return ToolResult.builder()
                    .success(true)
                    .data(List.of())
                    .message("未找到符合条件的分类")
                    .build();
            }
            
            // 转换为简化格式
            List<Map<String, Object>> result = categories.stream()
                .map(category -> convertToSimpleFormat(category, includeChildren))
                .collect(Collectors.toList());
            
            log.info("✅ 找到{}个分类", result.size());
            
            return ToolResult.builder()
                .success(true)
                .data(result)
                .message(String.format("找到%d个分类", result.size()))
                .build();
                
        } catch (Exception e) {
            log.error("❌ 获取产品分类失败: userId={}, params={}", userId, parameters, e);
            return ToolResult.builder()
                .success(false)
                .message("获取分类失败：" + e.getMessage())
                .errorCode("GET_CATEGORIES_ERROR")
                .build();
        }
    }
    
    /**
     * 转换为简化格式
     */
    private Map<String, Object> convertToSimpleFormat(ProductCategory category, boolean includeChildren) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("parentId", category.getParentId());
        map.put("sortOrder", category.getSortOrder());
        map.put("icon", category.getIcon());
        
        // 如果需要包含子分类，查询子分类
        if (includeChildren) {
            LambdaQueryWrapper<ProductCategory> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(ProductCategory::getParentId, category.getId())
                       .orderByAsc(ProductCategory::getSortOrder)
                       .orderByAsc(ProductCategory::getId);
            
            List<ProductCategory> children = categoryMapper.selectList(childWrapper);
            if (!children.isEmpty()) {
                List<Map<String, Object>> childrenList = children.stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("id", child.getId());
                        childMap.put("name", child.getName());
                        childMap.put("sortOrder", child.getSortOrder());
                        childMap.put("icon", child.getIcon());
                        return childMap;
                    })
                    .collect(Collectors.toList());
                map.put("children", childrenList);
            }
        }
        
        return map;
    }
    
    // 辅助方法：提取Long类型参数
    private Long extractLong(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
    
    // 辅助方法：提取Boolean类型参数（带默认值）
    private Boolean extractBoolean(Map<String, Object> params, String key, Boolean defaultValue) {
        Object value = params.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return defaultValue;
    }
}

