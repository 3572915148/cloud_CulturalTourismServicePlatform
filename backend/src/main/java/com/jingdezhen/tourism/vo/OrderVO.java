package com.jingdezhen.tourism.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象
 * @author shirenan
 */
@Data
public class OrderVO {

    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

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
    private String productImage;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态：0-待支付，1-已支付，2-已完成，3-已取消，4-退款中，5-已退款
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusText;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 使用日期
     */
    private String useDate;

    /**
     * 使用人数
     */
    private Integer useCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否可以评价
     */
    private Boolean canReview;
}

