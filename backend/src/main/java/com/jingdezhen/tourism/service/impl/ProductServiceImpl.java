package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 产品Service实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> getProductList(Long current, Long size, Long categoryId, String region, String keyword) {
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询上架的产品
        wrapper.eq(Product::getStatus, 1);

        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按区域筛选
        if (StringUtils.hasText(region)) {
            wrapper.eq(Product::getRegion, region);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Product::getTitle, keyword)
                    .or()
                    .like(Product::getDescription, keyword)
            );
        }

        // 按推荐和评分排序
        wrapper.orderByDesc(Product::getRecommend, Product::getRating);

        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        return product;
    }

    @Override
    public void createProduct(Product product) {
        product.setStatus(1);
        product.setSales(0);
        product.setRating(java.math.BigDecimal.valueOf(5.0));
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}

