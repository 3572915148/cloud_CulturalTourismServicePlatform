package com.jingdezhen.tourism.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单支付消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 支付时间戳
     */
    private Long payTime;
}

