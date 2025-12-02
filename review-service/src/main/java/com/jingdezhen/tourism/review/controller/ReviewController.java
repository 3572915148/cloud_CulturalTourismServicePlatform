package com.jingdezhen.tourism.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.ReviewCreateDTO;
import com.jingdezhen.tourism.common.entity.Review;
import com.jingdezhen.tourism.common.utils.TokenUtil;
import com.jingdezhen.tourism.common.vo.ReviewVO;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评价Controller
 */
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final TokenUtil tokenUtil;

    /**
     * 创建评价
     */
    @PostMapping("/create")
    public Result<ReviewVO> createReview(
            @Validated @RequestBody ReviewCreateDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        ReviewVO review = reviewService.createReview(dto, userId);
        return Result.success("评价成功", review);
    }

    /**
     * 获取产品评价列表
     */
    @GetMapping("/product/{productId}")
    public Result<Page<ReviewVO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size
    ) {
        Page<ReviewVO> page = reviewService.getProductReviews(productId, current, size);
        return Result.success(page);
    }

    /**
     * 获取我的评价列表
     */
    @GetMapping("/my")
    public Result<Page<ReviewVO>> getMyReviews(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        Page<ReviewVO> page = reviewService.getUserReviews(userId, current, size);
        return Result.success(page);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        reviewService.deleteReview(reviewId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取产品的所有评论（用于计算评分，内部服务调用）
     */
    @GetMapping("/product/all")
    public Result<java.util.List<Review>> getProductAllReviews(@RequestParam Long productId) {
        java.util.List<Review> reviews = reviewService.getProductAllReviews(productId);
        return Result.success(reviews);
    }
}

