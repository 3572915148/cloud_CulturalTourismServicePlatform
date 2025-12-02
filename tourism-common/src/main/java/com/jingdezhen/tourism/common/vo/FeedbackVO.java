package com.jingdezhen.tourism.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 反馈视图对象
 */
@Data
public class FeedbackVO {
    private Long id;
    private Long userId;
    private String type;
    private String typeText;
    private String content;
    private String contact;
    private String images;
    private Integer status;
    private String statusText;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}

