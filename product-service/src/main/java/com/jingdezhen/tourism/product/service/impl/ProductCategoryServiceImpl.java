package com.jingdezhen.tourism.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdezhen.tourism.common.entity.ProductCategory;
import com.jingdezhen.tourism.product.mapper.ProductCategoryMapper;
import com.jingdezhen.tourism.product.service.ProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
    
    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        return baseMapper.getCategoryStatistics();
    }
}

