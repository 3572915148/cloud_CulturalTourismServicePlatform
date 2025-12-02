package com.jingdezhen.tourism.product.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String PRODUCT_EXCHANGE = "product.exchange";

    // 队列名称
    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    public static final String ORDER_CANCELED_QUEUE = "order.canceled.queue";
    public static final String REVIEW_CHANGED_QUEUE = "review.changed.queue";

    // 路由键
    public static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    public static final String ORDER_CANCELED_ROUTING_KEY = "order.canceled";
    public static final String REVIEW_CHANGED_ROUTING_KEY = "review.changed";

    /**
     * 配置消息转换器（JSON格式）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        // 设置消息确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("消息发送失败: " + cause);
            }
        });
        // 设置消息返回回调
        template.setReturnsCallback(returned -> {
            System.err.println("消息被退回: " + returned.getMessage());
        });
        return template;
    }

    /**
     * 配置监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    /**
     * 创建交换机（主题交换机）
     */
    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE, true, false);
    }

    /**
     * 创建订单支付队列
     */
    @Bean
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(ORDER_PAID_QUEUE).build();
    }

    /**
     * 创建订单取消队列
     */
    @Bean
    public Queue orderCanceledQueue() {
        return QueueBuilder.durable(ORDER_CANCELED_QUEUE).build();
    }

    /**
     * 创建评论变更队列
     */
    @Bean
    public Queue reviewChangedQueue() {
        return QueueBuilder.durable(REVIEW_CHANGED_QUEUE).build();
    }

    /**
     * 绑定订单支付队列到交换机
     */
    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder
                .bind(orderPaidQueue())
                .to(productExchange())
                .with(ORDER_PAID_ROUTING_KEY);
    }

    /**
     * 绑定订单取消队列到交换机
     */
    @Bean
    public Binding orderCanceledBinding() {
        return BindingBuilder
                .bind(orderCanceledQueue())
                .to(productExchange())
                .with(ORDER_CANCELED_ROUTING_KEY);
    }

    /**
     * 绑定评论变更队列到交换机
     */
    @Bean
    public Binding reviewChangedBinding() {
        return BindingBuilder
                .bind(reviewChangedQueue())
                .to(productExchange())
                .with(REVIEW_CHANGED_ROUTING_KEY);
    }
}

