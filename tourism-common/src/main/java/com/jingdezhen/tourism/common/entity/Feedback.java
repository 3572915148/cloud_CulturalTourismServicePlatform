package com.jingdezhen.tourism.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈实体类
 */
@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 反馈类型：1-功能建议，2-问题反馈，3-投诉
     */
    private Integer type;

    private String content;

    private String contact;

    /**
     * 反馈图片（JSON数组）
     */
    private String images;

    /**
     * 处理状态：0-待处理，1-处理中，2-已处理
     */
    private Integer status;

    private String reply;

    private LocalDateTime replyTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

