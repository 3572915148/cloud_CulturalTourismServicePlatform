package com.jingdezhen.tourism.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.OrderCreateDTO;
import com.jingdezhen.tourism.common.vo.OrderVO;

/**
 * 订单Service
 */
public interface OrdersService {

    /**
     * 创建订单
     */
    OrderVO createOrder(OrderCreateDTO dto, Long userId);

    /**
     * 支付订单（模拟支付）
     */
    void payOrder(Long orderId, Long userId);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 获取用户订单列表
     */
    Page<OrderVO> getUserOrders(Long userId, Long current, Long size, Integer status);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 完成订单
     */
    void finishOrder(Long orderId, Long userId);

    /**
     * 获取订单实体（供其他服务调用）
     */
    com.jingdezhen.tourism.common.entity.Orders getOrderEntity(Long orderId);
}

