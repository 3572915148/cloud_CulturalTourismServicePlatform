package com.jingdezhen.tourism.product.service;

/**
 * 库存服务接口
 */
public interface StockService {

    /**
     * 获取商品库存（优先从Redis读取）
     */
    Integer getStock(Long productId);

    /**
     * 扣减库存（原子操作，防止超卖）
     */
    boolean decreaseStock(Long productId, Integer quantity);

    /**
     * 增加库存（用于取消订单时恢复库存）
     */
    void increaseStock(Long productId, Integer quantity);

    /**
     * 初始化商品库存到Redis
     */
    Integer initStock(Long productId);
}

