package com.jingdezhen.tourism.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏视图对象
 */
@Data
public class FavoriteVO {

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品标题
     */
    private String productTitle;

    /**
     * 产品封面图
     */
    private String productCoverImage;

    /**
     * 产品价格
     */
    private BigDecimal productPrice;

    /**
     * 产品状态
     */
    private Integer productStatus;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
}

