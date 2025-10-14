package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.OrderCreateDTO;
import com.jingdezhen.tourism.vo.OrderVO;

/**
 * 订单Service
 */
public interface OrdersService {

    /**
     * 创建订单
     *
     * @param dto 订单信息
     * @param userId 用户ID
     * @return 订单VO
     */
    OrderVO createOrder(OrderCreateDTO dto, Long userId);

    /**
     * 支付订单（模拟支付）
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void payOrder(Long orderId, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单VO
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 获取用户订单列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @param status 订单状态（可选）
     * @return 订单分页列表
     */
    Page<OrderVO> getUserOrders(Long userId, Long current, Long size, Integer status);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 完成订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void finishOrder(Long orderId, Long userId);
}

