package com.jingdezhen.tourism.product.controller;

import com.jingdezhen.tourism.common.utils.RedisLockUtil;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.product.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理Controller
 */
@RestController
@RequestMapping("/product/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 获取库存
     */
    @GetMapping("/{productId}")
    public Result<Integer> getStock(@PathVariable Long productId) {
        Integer stock = stockService.getStock(productId);
        return Result.success(stock);
    }

    /**
     * 扣减库存
     */
    @PutMapping("/decrease")
    public Result<Boolean> decreaseStock(@RequestBody StockRequest request) {
        boolean success = stockService.decreaseStock(request.getProductId(), request.getQuantity());
        return Result.success(success);
    }

    /**
     * 增加库存
     */
    @PutMapping("/increase")
    public Result<Void> increaseStock(@RequestBody StockRequest request) {
        stockService.increaseStock(request.getProductId(), request.getQuantity());
        return Result.success("库存增加成功");
    }

    /**
     * 库存请求DTO
     */
    public static class StockRequest {
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

