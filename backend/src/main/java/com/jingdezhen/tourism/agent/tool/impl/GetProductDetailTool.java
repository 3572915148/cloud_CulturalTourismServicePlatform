package com.jingdezhen.tourism.agent.tool.impl;

import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取产品详情工具
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductDetailTool implements AgentTool {
    
    private final ProductMapper productMapper;
    
    @Override
    public String getName() {
        return "get_product_detail";
    }
    
    @Override
    public String getDescription() {
        return "获取指定产品的完整详细信息，包括完整描述、价格、库存、地址、特色、预订须知、图片等全部信息。";
    }
    
    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "productId": {
                    "type": "integer",
                    "description": "产品ID"
                }
            },
            "required": ["productId"]
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
            Long productId = ((Number) parameters.get("productId")).longValue();
            log.info("📖 获取产品详情: userId={}, productId={}", userId, productId);
            
            Product product = productMapper.selectById(productId);
            
            if (product == null || product.getStatus() != 1) {
                log.warn("⚠️ 产品不存在或已下架: productId={}", productId);
                return ToolResult.builder()
                    .success(false)
                    .message("产品不存在或已下架")
                    .errorCode("PRODUCT_NOT_FOUND")
                    .build();
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
            detail.put("merchantId", product.getMerchantId());
            detail.put("categoryId", product.getCategoryId());
            
            log.info("✅ 成功获取产品详情: {}", product.getTitle());
            
            return ToolResult.builder()
                .success(true)
                .data(detail)
                .message("获取产品详情成功")
                .build();
                
        } catch (Exception e) {
            log.error("❌ 获取产品详情失败", e);
            return ToolResult.builder()
                .success(false)
                .message("获取失败：" + e.getMessage())
                .errorCode("GET_DETAIL_ERROR")
                .build();
        }
    }
}

