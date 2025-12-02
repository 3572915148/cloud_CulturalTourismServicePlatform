package com.jingdezhen.tourism.favorite.feign;

import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/product")
public interface ProductServiceClient {
    @GetMapping("/{id}")
    Result<Product> getProductById(@PathVariable Long id);
}

