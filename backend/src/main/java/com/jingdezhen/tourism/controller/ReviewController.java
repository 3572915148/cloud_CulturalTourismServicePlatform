package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.ReviewCreateDTO;
import com.jingdezhen.tourism.service.ReviewService;
import com.jingdezhen.tourism.utils.JwtUtil;
import com.jingdezhen.tourism.vo.ReviewVO;
import com.jingdezhen.tourism.vo.Result;
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
    private final JwtUtil jwtUtil;

    /**
     * 创建评价
     */
    @PostMapping("/create")
    public Result<ReviewVO> createReview(
            @Validated @RequestBody ReviewCreateDTO dto,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
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
            @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Page<ReviewVO> page = reviewService.getUserReviews(userId, current, size);
        return Result.success(page);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        reviewService.deleteReview(reviewId, userId);
        return Result.success("删除成功");
    }
}

