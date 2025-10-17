package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Orders;
import com.jingdezhen.tourism.service.MerchantOrderService;
import com.jingdezhen.tourism.utils.TokenUtil;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商户订单管理Controller
 */
@RestController
@RequestMapping("/merchant/orders")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;
    private final TokenUtil tokenUtil;

    /**
     * 获取商户订单列表
     */
    @GetMapping
    public Result<Page<Orders>> getMerchantOrders(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String contactPhone,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        Page<Orders> page = merchantOrderService.getMerchantOrders(merchantId, current, size, status, orderNo, contactPhone);
        return Result.success(page);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<Orders> getOrderDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        Orders order = merchantOrderService.getOrderDetail(id, merchantId);
        return Result.success(order);
    }

    /**
     * 确认订单（商户确认已支付）
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        merchantOrderService.confirmOrder(id, merchantId);
        return Result.success("订单已确认");
    }

    /**
     * 完成订单
     */
    @PostMapping("/{id}/complete")
    public Result<Void> completeOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        merchantOrderService.completeOrder(id, merchantId);
        return Result.success("订单已完成");
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) String reason,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        merchantOrderService.cancelOrder(id, merchantId, reason);
        return Result.success("订单已取消");
    }

    /**
     * 退款订单
     */
    @PostMapping("/{id}/refund")
    public Result<Void> refundOrder(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(authHeader);
        merchantOrderService.refundOrder(id, merchantId);
        return Result.success("退款成功");
    }
}

