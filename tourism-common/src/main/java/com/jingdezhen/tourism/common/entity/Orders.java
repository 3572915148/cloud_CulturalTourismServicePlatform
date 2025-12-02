package com.jingdezhen.tourism.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long merchantId;

    private Long productId;

    private String productTitle;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;

    private LocalDate bookingDate;

    private String bookingTime;

    private String contactName;

    private String contactPhone;

    private String remark;

    /**
     * 订单状态：0-待支付，1-已支付，2-已完成，3-已取消，4-已退款
     */
    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime completeTime;

    private LocalDateTime cancelTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

