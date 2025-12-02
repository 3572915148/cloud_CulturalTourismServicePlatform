package com.jingdezhen.tourism.review.service;

import com.jingdezhen.tourism.common.message.ReviewChangedMessage;
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
    private static final String REVIEW_CHANGED_ROUTING_KEY = "review.changed";

    // 变更类型
    public static final String CHANGE_TYPE_CREATE = "CREATE";
    public static final String CHANGE_TYPE_DELETE = "DELETE";

    /**
     * 发送评论变更消息
     */
    public void sendReviewChangedMessage(Long reviewId, Long productId, String changeType) {
        try {
            ReviewChangedMessage message = new ReviewChangedMessage();
            message.setReviewId(reviewId);
            message.setProductId(productId);
            message.setChangeType(changeType);
            message.setChangeTime(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, REVIEW_CHANGED_ROUTING_KEY, message);
            log.info("✅ 评论变更消息发送成功: reviewId={}, productId={}, changeType={}", 
                reviewId, productId, changeType);
        } catch (Exception e) {
            log.error("❌ 评论变更消息发送失败: reviewId={}, productId={}, changeType={}, error={}", 
                reviewId, productId, changeType, e.getMessage(), e);
        }
    }
}

