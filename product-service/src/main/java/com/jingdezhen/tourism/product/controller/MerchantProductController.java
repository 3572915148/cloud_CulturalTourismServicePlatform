package com.jingdezhen.tourism.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.common.utils.TokenUtil;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 商户产品管理接口（仅能操作自己的产品）
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ProductMapper productMapper;
    private final TokenUtil tokenUtil;

    /**
     * 获取当前商户的产品列表（分页）
     */
    @GetMapping("/products")
    public Result<Page<Product>> listMyProducts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(request.getHeader("Authorization"));
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId);
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getUpdateTime);
        Page<Product> result = productMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 创建产品（自动绑定当前商户）
     */
    @PostMapping("/product")
    public Result<Void> create(@RequestBody @Validated Product product, HttpServletRequest request) {
        Long merchantId = tokenUtil.getUserIdFromAuth(request.getHeader("Authorization"));
        product.setId(null);
        product.setMerchantId(merchantId);
        if (product.getStatus() == null) {
            product.setStatus(0);
        }
        productMapper.insert(product);
        return Result.success("创建成功");
    }

    /**
     * 更新产品（仅限本人）
     */
    @PutMapping("/product/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product, HttpServletRequest request) {
        Long merchantId = tokenUtil.getUserIdFromAuth(request.getHeader("Authorization"));
        Product db = productMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("产品不存在");
        }
        if (!merchantId.equals(db.getMerchantId())) {
            throw new BusinessException("无权操作他人的产品");
        }
        product.setId(id);
        product.setMerchantId(merchantId);
        productMapper.updateById(product);
        return Result.success("更新成功");
    }

    /**
     * 删除产品（仅限本人）
     */
    @DeleteMapping("/product/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = tokenUtil.getUserIdFromAuth(request.getHeader("Authorization"));
        Product db = productMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("产品不存在");
        }
        if (!merchantId.equals(db.getMerchantId())) {
            throw new BusinessException("无权操作他人的产品");
        }
        productMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

