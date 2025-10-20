package com.jingdezhen.tourism.dto;

import lombok.Data;

import java.util.List;

/**
 * AI推荐响应DTO
 */
@Data
public class AiRecommendationResponseDTO {

    /**
     * 推荐ID
     */
    private Long recommendationId;

    /**
     * AI回复内容
     */
    private String response;

    /**
     * 推荐的产品列表
     */
    private List<RecommendedProductDTO> recommendedProducts;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 推荐的产品DTO
     */
    @Data
    public static class RecommendedProductDTO {
        private Long id;
        private String title;
        private String description;
        private String coverImage;
        private String price;
        private String region;
        private String address;
        private Double rating;
        private String tags;
        private String reason; // 推荐理由
    }
}
