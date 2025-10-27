package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingdezhen.tourism.entity.ProductCategory;
import com.jingdezhen.tourism.mapper.ProductCategoryMapper;
import com.jingdezhen.tourism.service.ProductCategoryService;
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


