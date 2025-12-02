package com.jingdezhen.tourism.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类（景点、酒店、餐厅等）
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long categoryId;

    private String title;

    private String description;

    private String coverImage;

    /**
     * 产品图片（JSON数组）
     */
    private String images;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer sales;

    private String region;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal rating;

    /**
     * 标签（JSON数组）
     */
    private String tags;

    private String features;

    private String notice;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 是否推荐：0-否，1-是
     */
    private Integer recommend;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

