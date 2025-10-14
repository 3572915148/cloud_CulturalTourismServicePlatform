package com.jingdezhen.tourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI推荐记录实体类
 */
@Data
@TableName("ai_recommendation")
public class AiRecommendation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String query;

    private String context;

    private String response;

    /**
     * 推荐的产品ID列表（JSON数组）
     */
    private String recommendedProducts;

    /**
     * 用户反馈：1-有帮助，0-没帮助
     */
    private Integer feedback;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

