package com.jingdezhen.tourism.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 陶瓷文化内容实体类
 */
@Data
@TableName("ceramic_content")
public class CeramicContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类：历史、工艺、名家、作品等
     */
    private String category;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    /**
     * 相关图片（JSON数组）
     */
    private String images;

    private String videoUrl;

    private String author;

    private Integer views;

    private Integer likes;

    private Integer sortOrder;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

