package com.jingdezhen.tourism.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价视图对象
 * @author shirenan
 */
@Data
public class ReviewVO {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品标题
     */
    private String productTitle;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 评分：1-5星
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片（JSON数组）
     */
    private String images;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 商户回复
     */
    private String merchantReply;

    /**
     * 商户回复时间
     */
    private LocalDateTime merchantReplyTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

