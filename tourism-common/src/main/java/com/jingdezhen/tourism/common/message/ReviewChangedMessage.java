package com.jingdezhen.tourism.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评论变更消息（创建或删除）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewChangedMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 评论ID
     */
    private Long reviewId;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 变更类型：CREATE-创建，DELETE-删除
     */
    private String changeType;
    
    /**
     * 变更时间戳
     */
    private Long changeTime;
}

