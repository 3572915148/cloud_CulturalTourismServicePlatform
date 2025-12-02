package com.jingdezhen.tourism.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.OrderCreateDTO;
import com.jingdezhen.tourism.common.utils.TokenUtil;
import com.jingdezhen.tourism.common.vo.OrderVO;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.order.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final TokenUtil tokenUtil;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderVO> createOrder(
            @Validated @RequestBody OrderCreateDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        OrderVO order = ordersService.createOrder(dto, userId);
        return Result.success("订单创建成功", order);
    }

    /**
     * 支付订单（模拟支付）
     */
    @PostMapping("/pay/{orderId}")
    public Result<Void> payOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        ordersService.payOrder(orderId, userId);
        return Result.success("支付成功");
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        OrderVO order = ordersService.getOrderDetail(orderId, userId);
        return Result.success(order);
    }

    /**
     * 获取订单实体（供其他服务调用，不需要认证）
     */
    @GetMapping("/entity/{orderId}")
    public Result<com.jingdezhen.tourism.common.entity.Orders> getOrderEntity(@PathVariable Long orderId) {
        com.jingdezhen.tourism.common.entity.Orders order = ordersService.getOrderEntity(orderId);
        return Result.success(order);
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping("/my")
    public Result<Page<OrderVO>> getMyOrders(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        Page<OrderVO> page = ordersService.getUserOrders(userId, current, size, status);
        return Result.success(page);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        ordersService.cancelOrder(orderId, userId);
        return Result.success("订单已取消");
    }

    /**
     * 完成订单
     */
    @PostMapping("/finish/{orderId}")
    public Result<Void> finishOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        ordersService.finishOrder(orderId, userId);
        return Result.success("订单已完成");
    }
}

