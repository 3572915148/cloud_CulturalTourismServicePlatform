package com.jingdezhen.tourism.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.ReviewCreateDTO;
import com.jingdezhen.tourism.common.entity.Review;
import com.jingdezhen.tourism.common.vo.ReviewVO;

/**
 * 评价Service
 */
public interface ReviewService {

    /**
     * 创建评价
     */
    ReviewVO createReview(ReviewCreateDTO dto, Long userId);

    /**
     * 获取产品评价列表
     */
    Page<ReviewVO> getProductReviews(Long productId, Long current, Long size);

    /**
     * 获取用户评价列表
     */
    Page<ReviewVO> getUserReviews(Long userId, Long current, Long size);

    /**
     * 删除评价
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 获取商户下产品的评价列表
     */
    Page<ReviewVO> getMerchantReviews(Long merchantId, Long current, Long size);

    /**
     * 商户回复评价
     */
    void replyReview(Long reviewId, Long merchantId, String replyContent);

    /**
     * 获取产品的所有评论（用于计算评分）
     */
    java.util.List<Review> getProductAllReviews(Long productId);
}

