package com.jingdezhen.tourism.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.common.entity.ProductCategory;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品分类Controller
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public Result<List<ProductCategory>> list(@RequestParam(required = false) Long parentId) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            wrapper.eq(ProductCategory::getParentId, parentId);
        }
        wrapper.orderByAsc(ProductCategory::getSortOrder, ProductCategory::getId);
        List<ProductCategory> list = categoryService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 获取分类统计信息
     */
    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        List<Map<String, Object>> statistics = categoryService.getCategoryStatistics();
        return Result.success(statistics);
    }
}

