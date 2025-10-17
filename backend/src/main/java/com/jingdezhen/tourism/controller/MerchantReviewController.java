package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.service.ReviewService;
import com.jingdezhen.tourism.utils.TokenUtil;
import com.jingdezhen.tourism.vo.Result;
import com.jingdezhen.tourism.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 商户评价管理接口
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantReviewController {

    private final ReviewService reviewService;
    private final TokenUtil tokenUtil;

    /**
     * 获取我店铺的评价列表
     */
    @GetMapping("/reviews")
    public Result<Page<ReviewVO>> listReviews(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(request.getHeader("Authorization"));
        Page<ReviewVO> page = reviewService.getMerchantReviews(merchantId, current, size);
        return Result.success(page);
    }

    /**
     * 回复某条评价
     */
    @PostMapping("/review/{id}/reply")
    public Result<Void> reply(
            @PathVariable("id") Long reviewId,
            @RequestParam("content") String content,
            HttpServletRequest request
    ) {
        Long merchantId = tokenUtil.getUserIdFromAuth(request.getHeader("Authorization"));
        reviewService.replyReview(reviewId, merchantId, content);
        return Result.success("回复成功");
    }
}


