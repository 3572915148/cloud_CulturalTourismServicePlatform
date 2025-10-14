package com.jingdezhen.tourism.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈视图对象
 * @author shirenan
 */
@Data
public class FeedbackVO {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 反馈类型
     */
    private String type;

    /**
     * 反馈类型描述
     */
    private String typeText;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 截图图片
     */
    private String images;

    /**
     * 处理状态：0-待处理，1-处理中，2-已处理，3-已关闭
     */
    private Integer status;

    /**
     * 处理状态描述
     */
    private String statusText;

    /**
     * 平台回复
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

