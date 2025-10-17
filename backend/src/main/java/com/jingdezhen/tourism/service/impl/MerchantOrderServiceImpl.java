package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Orders;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.OrdersMapper;
import com.jingdezhen.tourism.service.MerchantOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 商户订单Service实现类
 */
@Service
@RequiredArgsConstructor
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private final OrdersMapper ordersMapper;

    @Override
    public Page<Orders> getMerchantOrders(Long merchantId, Long current, Long size, Integer status, String orderNo, String contactPhone) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getMerchantId, merchantId);
        
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Orders::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(contactPhone)) {
            wrapper.like(Orders::getContactPhone, contactPhone);
        }
        
        wrapper.orderByDesc(Orders::getCreateTime);
        
        Page<Orders> page = new Page<>(current, size);
        return ordersMapper.selectPage(page, wrapper);
    }

    @Override
    public Orders getOrderDetail(Long orderId, Long merchantId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权查看该订单");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long orderId, Long merchantId) {
        Orders order = getOrderDetail(orderId, merchantId);
        
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，无法确认");
        }
        
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long merchantId) {
        Orders order = getOrderDetail(orderId, merchantId);
        
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，无法完成");
        }
        
        order.setStatus(2);
        order.setCompleteTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long merchantId, String reason) {
        Orders order = getOrderDetail(orderId, merchantId);
        
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，无法取消");
        }
        
        order.setStatus(3);
        order.setCancelTime(LocalDateTime.now());
        if (StringUtils.hasText(reason)) {
            order.setRemark((order.getRemark() != null ? order.getRemark() + "\n" : "") + "取消原因：" + reason);
        }
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId, Long merchantId) {
        Orders order = getOrderDetail(orderId, merchantId);
        
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，无法退款");
        }
        
        order.setStatus(4);
        ordersMapper.updateById(order);
    }
}

