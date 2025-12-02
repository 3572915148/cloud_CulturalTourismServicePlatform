package com.jingdezhen.tourism.common.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI推荐记录VO
 */
@Data
public class AiRecommendationVO {
    private Long id;
    private String query;
    private String response;
    private List<RecommendedProductVO> recommendedProducts;
    private Integer feedback;
    private LocalDateTime createTime;

    @Data
    public static class RecommendedProductVO {
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

