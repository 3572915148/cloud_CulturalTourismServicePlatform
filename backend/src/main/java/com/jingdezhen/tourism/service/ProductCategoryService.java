package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdezhen.tourism.entity.ProductCategory;

import java.util.List;
import java.util.Map;

public interface ProductCategoryService extends IService<ProductCategory> {
    
    /**
     * 获取分类统计信息（包含每个分类的产品数量）
     */
    List<Map<String, Object>> getCategoryStatistics();
}


