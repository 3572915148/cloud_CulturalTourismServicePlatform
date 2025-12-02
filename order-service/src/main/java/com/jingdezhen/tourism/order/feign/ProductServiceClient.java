package com.jingdezhen.tourism.order.feign;

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

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    Result<Product> getProductById(@PathVariable Long id);

    /**
     * 更新商品（用于更新销量）
     */
    @PutMapping
    Result<Void> updateProduct(@RequestBody Product product);

    /**
     * 扣减库存
     */
    @PutMapping("/stock/decrease")
    Result<Boolean> decreaseStock(@RequestBody StockRequest request);

    /**
     * 增加库存
     */
    @PutMapping("/stock/increase")
    Result<Void> increaseStock(@RequestBody StockRequest request);

    /**
     * 获取库存
     */
    @GetMapping("/stock/{productId}")
    Result<Integer> getStock(@PathVariable Long productId);

    /**
     * 库存请求DTO
     */
    static class StockRequest {
        private Long productId;
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}

