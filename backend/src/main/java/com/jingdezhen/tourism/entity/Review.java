package com.jingdezhen.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long userId;

    private Long productId;

    private Long merchantId;

    /**
     * 评分：1-5星
     */
    private Integer rating;

    private String content;

    /**
     * 评价图片（JSON数组）
     */
    private String images;

    private String replyContent;

    private LocalDateTime replyTime;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

