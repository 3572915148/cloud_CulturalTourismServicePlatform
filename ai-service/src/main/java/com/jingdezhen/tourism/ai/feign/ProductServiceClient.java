package com.jingdezhen.tourism.ai.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "product-service", path = "/product")
public interface ProductServiceClient {

    @GetMapping("/list")
    Result<Page<Product>> getProductList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "100") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String keyword
    );
}

