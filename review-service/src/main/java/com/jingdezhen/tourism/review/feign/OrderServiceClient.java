package com.jingdezhen.tourism.review.feign;

import com.jingdezhen.tourism.common.entity.Orders;
import com.jingdezhen.tourism.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务Feign客户端
 */
@FeignClient(name = "order-service", path = "/order")
public interface OrderServiceClient {

    @GetMapping("/entity/{orderId}")
    Result<Orders> getOrderById(@PathVariable Long orderId);
}

