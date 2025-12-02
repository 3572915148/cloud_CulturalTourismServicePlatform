package com.jingdezhen.tourism.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.common.entity.ProductCategory;

import java.util.List;
import java.util.Map;

/**
 * 产品分类Mapper
 */
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    
    /**
     * 获取分类统计信息（包含每个分类的产品数量）
     */
    List<Map<String, Object>> getCategoryStatistics();
}

