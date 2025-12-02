package com.jingdezhen.tourism.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productTitle;
    private String productImage;
    private Long merchantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private String contactName;
    private String contactPhone;
    private String useDate;
    private Integer useCount;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean canReview;
}

