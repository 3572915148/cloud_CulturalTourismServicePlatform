package com.jingdezhen.tourism.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI推荐请求DTO
 */
@Data
public class AiRecommendationRequestDTO {

    @NotBlank(message = "查询内容不能为空")
    @Size(max = 500, message = "查询内容不能超过500个字符")
    private String query;

    /**
     * 用户偏好（可选）
     */
    private String preferences;

    /**
     * 预算范围（可选）
     */
    private String budget;

    /**
     * 人数（可选）
     */
    private Integer peopleCount;
}
