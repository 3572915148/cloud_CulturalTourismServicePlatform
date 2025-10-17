package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.entity.ProductCategory;
import com.jingdezhen.tourism.service.ProductCategoryService;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品分类Controller
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 获取分类列表（顶级与全部都支持）
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
}
