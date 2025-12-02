package com.jingdezhen.tourism.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdezhen.tourism.common.entity.ProductCategory;

import java.util.List;
import java.util.Map;

public interface ProductCategoryService extends IService<ProductCategory> {
    
    /**
     * 获取分类统计信息（包含每个分类的产品数量）
     */
    List<Map<String, Object>> getCategoryStatistics();
}

