package com.jingdezhen.tourism.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.entity.Orders;

/**
 * 商户订单Service
 */
public interface MerchantOrderService {

    /**
     * 获取商户订单列表
     */
    Page<Orders> getMerchantOrders(Long merchantId, Long current, Long size, Integer status, String orderNo, String contactPhone);

    /**
     * 获取订单详情
     */
    Orders getOrderDetail(Long orderId, Long merchantId);

    /**
     * 确认订单
     */
    void confirmOrder(Long orderId, Long merchantId);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId, Long merchantId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long merchantId, String reason);

    /**
     * 退款订单
     */
    void refundOrder(Long orderId, Long merchantId);
}

