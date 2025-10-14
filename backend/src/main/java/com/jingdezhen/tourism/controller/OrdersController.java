package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.OrderCreateDTO;
import com.jingdezhen.tourism.service.OrdersService;
import com.jingdezhen.tourism.utils.JwtUtil;
import com.jingdezhen.tourism.vo.OrderVO;
import com.jingdezhen.tourism.vo.Result;
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
    private final JwtUtil jwtUtil;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderVO> createOrder(
            @Validated @RequestBody OrderCreateDTO dto,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        OrderVO order = ordersService.createOrder(dto, userId);
        return Result.success("订单创建成功", order);
    }

    /**
     * 支付订单（模拟支付）
     */
    @PostMapping("/pay/{orderId}")
    public Result<Void> payOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        ordersService.payOrder(orderId, userId);
        return Result.success("支付成功");
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        OrderVO order = ordersService.getOrderDetail(orderId, userId);
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
            @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<OrderVO> page = ordersService.getUserOrders(userId, current, size, status);
        return Result.success(page);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        ordersService.cancelOrder(orderId, userId);
        return Result.success("订单已取消");
    }

    /**
     * 完成订单
     */
    @PostMapping("/finish/{orderId}")
    public Result<Void> finishOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        ordersService.finishOrder(orderId, userId);
        return Result.success("订单已完成");
    }
}

