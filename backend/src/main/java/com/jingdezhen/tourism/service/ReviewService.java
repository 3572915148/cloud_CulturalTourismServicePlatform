package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.ReviewCreateDTO;
import com.jingdezhen.tourism.vo.ReviewVO;

/**
 * 评价Service
 */
public interface ReviewService {

    /**
     * 创建评价
     *
     * @param dto 评价信息
     * @param userId 用户ID
     * @return 评价VO
     */
    ReviewVO createReview(ReviewCreateDTO dto, Long userId);

    /**
     * 获取产品评价列表
     *
     * @param productId 产品ID
     * @param current 当前页
     * @param size 每页大小
     * @return 评价分页列表
     */
    Page<ReviewVO> getProductReviews(Long productId, Long current, Long size);

    /**
     * 获取用户评价列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 评价分页列表
     */
    Page<ReviewVO> getUserReviews(Long userId, Long current, Long size);

    /**
     * 删除评价
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 获取商户下产品的评价列表
     *
     * @param merchantId 商户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 评价分页列表
     */
    Page<ReviewVO> getMerchantReviews(Long merchantId, Long current, Long size);

    /**
     * 商户回复评价
     *
     * @param reviewId 评价ID
     * @param merchantId 商户ID（鉴权）
     * @param replyContent 回复内容
     */
    void replyReview(Long reviewId, Long merchantId, String replyContent);
}

