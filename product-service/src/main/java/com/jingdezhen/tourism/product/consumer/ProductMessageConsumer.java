package com.jingdezhen.tourism.product.consumer;

import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.entity.Review;
import com.jingdezhen.tourism.common.message.OrderCanceledMessage;
import com.jingdezhen.tourism.common.message.OrderPaidMessage;
import com.jingdezhen.tourism.common.message.ReviewChangedMessage;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.product.config.RabbitMQConfig;
import com.jingdezhen.tourism.product.feign.ReviewServiceClient;
import com.jingdezhen.tourism.product.mapper.ProductMapper;
import com.jingdezhen.tourism.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 产品服务消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMessageConsumer {

    private final ProductMapper productMapper;
    private final StockService stockService;
    private final ReviewServiceClient reviewServiceClient;

    /**
     * 消费订单支付消息 - 更新产品销量
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_PAID_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderPaid(OrderPaidMessage message, 
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("📨 收到订单支付消息: orderId={}, productId={}, quantity={}", 
                message.getOrderId(), message.getProductId(), message.getQuantity());

            Product product = productMapper.selectById(message.getProductId());
            if (product == null) {
                log.warn("⚠️ 产品不存在，无法更新销量: productId={}", message.getProductId());
                return;
            }

            // 更新产品销量
            Integer currentSales = product.getSales() != null ? product.getSales() : 0;
            product.setSales(currentSales + message.getQuantity());
            productMapper.updateById(product);

            log.info("✅ 产品销量更新成功: productId={}, 原销量={}, 新增={}, 现销量={}", 
                message.getProductId(), currentSales, message.getQuantity(), product.getSales());

        } catch (Exception e) {
            log.error("❌ 处理订单支付消息失败: orderId={}, productId={}, error={}", 
                message.getOrderId(), message.getProductId(), e.getMessage(), e);
            throw e; // 抛出异常，让RabbitMQ重新投递
        }
    }

    /**
     * 消费订单取消消息 - 恢复库存
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELED_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderCanceled(OrderCanceledMessage message,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("📨 收到订单取消消息: orderId={}, productId={}, quantity={}", 
                message.getOrderId(), message.getProductId(), message.getQuantity());

            // 恢复库存（通过StockService，会同时更新Redis和数据库）
            stockService.increaseStock(message.getProductId(), message.getQuantity());

            log.info("✅ 库存恢复成功: productId={}, quantity={}", 
                message.getProductId(), message.getQuantity());

        } catch (Exception e) {
            log.error("❌ 处理订单取消消息失败: orderId={}, productId={}, error={}", 
                message.getOrderId(), message.getProductId(), e.getMessage(), e);
            throw e; // 抛出异常，让RabbitMQ重新投递
        }
    }

    /**
     * 消费评论变更消息 - 更新产品评分
     */
    @RabbitListener(queues = RabbitMQConfig.REVIEW_CHANGED_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleReviewChanged(ReviewChangedMessage message,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("📨 收到评论变更消息: reviewId={}, productId={}, changeType={}", 
                message.getReviewId(), message.getProductId(), message.getChangeType());

            // 通过Feign调用review-service获取该产品的所有评论
            Result<java.util.List<Review>> reviewResult = reviewServiceClient.getProductAllReviews(message.getProductId());
            
            if (reviewResult.getCode() != 200 || reviewResult.getData() == null || reviewResult.getData().isEmpty()) {
                // 没有评论，设置为默认5.0
                Product product = productMapper.selectById(message.getProductId());
                if (product != null) {
                    product.setRating(new BigDecimal("5.0"));
                    productMapper.updateById(product);
                    log.info("✅ 产品评分更新为默认值: productId={}, rating=5.0", message.getProductId());
                }
                return;
            }

            // 计算平均评分
            java.util.List<Review> reviews = reviewResult.getData();
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(5.0);

            // 更新产品评分
            Product product = productMapper.selectById(message.getProductId());
            if (product != null) {
                product.setRating(new BigDecimal(avgRating).setScale(1, RoundingMode.HALF_UP));
                productMapper.updateById(product);
                log.info("✅ 产品评分更新成功: productId={}, 评论数={}, 平均评分={}", 
                    message.getProductId(), reviews.size(), product.getRating());
            } else {
                log.warn("⚠️ 产品不存在，无法更新评分: productId={}", message.getProductId());
            }

        } catch (Exception e) {
            log.error("❌ 处理评论变更消息失败: reviewId={}, productId={}, error={}", 
                message.getReviewId(), message.getProductId(), e.getMessage(), e);
            throw e; // 抛出异常，让RabbitMQ重新投递
        }
    }
}

