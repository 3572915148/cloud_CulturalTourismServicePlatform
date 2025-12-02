package com.jingdezhen.tourism.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单取消消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCanceledMessage implements Serializable {
    
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
     * 需要恢复的库存数量
     */
    private Integer quantity;
    
    /**
     * 取消时间戳
     */
    private Long cancelTime;
}

