package com.jingdezhen.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建反馈DTO
 */
@Data
public class FeedbackCreateDTO {

    @NotBlank(message = "反馈类型不能为空")
    private String type;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String content;

    /**
     * 联系方式（可选）
     */
    private String contact;

    /**
     * 截图图片（JSON数组，可选）
     */
    private String images;
}

