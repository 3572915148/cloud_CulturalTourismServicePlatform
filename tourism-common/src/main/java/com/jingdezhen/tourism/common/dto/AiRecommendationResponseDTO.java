package com.jingdezhen.tourism.common.dto;

import lombok.Data;
import java.util.List;

/**
 * AI推荐响应DTO
 */
@Data
public class AiRecommendationResponseDTO {

    private Long recommendationId;
    private String response;
    private List<RecommendedProductDTO> recommendedProducts;
    private String reason;

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
        private String reason;
    }
}

