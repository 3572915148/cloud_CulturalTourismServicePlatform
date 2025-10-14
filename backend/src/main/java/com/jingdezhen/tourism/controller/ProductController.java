package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.service.ProductService;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 产品Controller
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询产品列表
     */
    @GetMapping("/list")
    public Result<Page<Product>> getProductList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String keyword
    ) {
        Page<Product> page = productService.getProductList(current, size, categoryId, region, keyword);
        return Result.success(page);
    }

    /**
     * 获取产品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }

    /**
     * 创建产品
     */
    @PostMapping
    public Result<Void> createProduct(@RequestBody Product product) {
        productService.createProduct(product);
        return Result.success("创建成功");
    }

    /**
     * 更新产品
     */
    @PutMapping
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success("更新成功");
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }
}

