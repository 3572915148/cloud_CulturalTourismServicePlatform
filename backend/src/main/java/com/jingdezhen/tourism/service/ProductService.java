package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Product;

/**
 * 产品Service接口
 */
public interface ProductService {

    /**
     * 分页查询产品列表
     */
    Page<Product> getProductList(Long current, Long size, Long categoryId, String region, String keyword);

    /**
     * 根据ID获取产品详情
     */
    Product getProductById(Long id);

    /**
     * 创建产品
     */
    void createProduct(Product product);

    /**
     * 更新产品
     */
    void updateProduct(Product product);

    /**
     * 删除产品
     */
    void deleteProduct(Long id);
}

