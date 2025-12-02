package com.jingdezhen.tourism.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏视图对象
 */
@Data
public class FavoriteVO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productTitle;
    private String productCoverImage;
    private BigDecimal productPrice;
    private Integer productStatus;
    private LocalDateTime createTime;
}

