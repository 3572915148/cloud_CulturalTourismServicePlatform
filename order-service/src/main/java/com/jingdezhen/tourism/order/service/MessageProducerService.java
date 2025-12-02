package com.jingdezhen.tourism.order.service;

import com.jingdezhen.tourism.common.message.OrderCanceledMessage;
import com.jingdezhen.tourism.common.message.OrderPaidMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息生产者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final RabbitTemplate rabbitTemplate;

    // 交换机名称
    private static final String PRODUCT_EXCHANGE = "product.exchange";

    // 路由键
    private static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    private static final String ORDER_CANCELED_ROUTING_KEY = "order.canceled";

    /**
     * 发送订单支付消息
     */
    public void sendOrderPaidMessage(Long orderId, Long productId, Integer quantity) {
        try {
            OrderPaidMessage message = new OrderPaidMessage();
            message.setOrderId(orderId);
            message.setProductId(productId);
            message.setQuantity(quantity);
            message.setPayTime(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, ORDER_PAID_ROUTING_KEY, message);
            log.info("✅ 订单支付消息发送成功: orderId={}, productId={}, quantity={}", 
                orderId, productId, quantity);
        } catch (Exception e) {
            log.error("❌ 订单支付消息发送失败: orderId={}, productId={}, error={}", 
                orderId, productId, e.getMessage(), e);
        }
    }

    /**
     * 发送订单取消消息
     */
    public void sendOrderCanceledMessage(Long orderId, Long productId, Integer quantity) {
        try {
            OrderCanceledMessage message = new OrderCanceledMessage();
            message.setOrderId(orderId);
            message.setProductId(productId);
            message.setQuantity(quantity);
            message.setCancelTime(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, ORDER_CANCELED_ROUTING_KEY, message);
            log.info("✅ 订单取消消息发送成功: orderId={}, productId={}, quantity={}", 
                orderId, productId, quantity);
        } catch (Exception e) {
            log.error("❌ 订单取消消息发送失败: orderId={}, productId={}, error={}", 
                orderId, productId, e.getMessage(), e);
        }
    }
}

