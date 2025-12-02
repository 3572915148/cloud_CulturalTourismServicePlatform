package com.jingdezhen.tourism.review.feign;

import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "product-service", path = "/product")
public interface ProductServiceClient {

    @GetMapping("/{id}")
    Result<Product> getProductById(@PathVariable Long id);

    @PutMapping
    Result<Void> updateProduct(@RequestBody Product product);
}

