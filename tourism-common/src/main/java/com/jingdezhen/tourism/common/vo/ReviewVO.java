package com.jingdezhen.tourism.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价视图对象
 */
@Data
public class ReviewVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long productId;
    private String productTitle;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
    private Boolean anonymous;
    private String merchantReply;
    private LocalDateTime merchantReplyTime;
    private Integer likeCount;
    private LocalDateTime createTime;
}

