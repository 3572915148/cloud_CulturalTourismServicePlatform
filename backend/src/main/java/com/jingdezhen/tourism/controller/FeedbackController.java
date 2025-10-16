package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.FeedbackCreateDTO;
import com.jingdezhen.tourism.service.FeedbackService;
import com.jingdezhen.tourism.utils.TokenUtil;
import com.jingdezhen.tourism.vo.FeedbackVO;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈Controller
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final TokenUtil tokenUtil;

    /**
     * 创建反馈
     * 需要登录
     */
    @PostMapping("/create")
    public Result<FeedbackVO> createFeedback(
            @Validated @RequestBody FeedbackCreateDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        FeedbackVO feedback = feedbackService.createFeedback(dto, userId);
        return Result.success("反馈提交成功", feedback);
    }

    /**
     * 获取我的反馈列表
     * 需要登录
     */
    @GetMapping("/my")
    public Result<Page<FeedbackVO>> getMyFeedbacks(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        Page<FeedbackVO> page = feedbackService.getUserFeedbacks(userId, current, size);
        return Result.success(page);
    }

    /**
     * 获取反馈详情
     * 需要登录
     */
    @GetMapping("/{feedbackId}")
    public Result<FeedbackVO> getFeedbackDetail(
            @PathVariable Long feedbackId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        FeedbackVO feedback = feedbackService.getFeedbackDetail(feedbackId, userId);
        return Result.success(feedback);
    }

    /**
     * 删除反馈
     * 需要登录
     */
    @DeleteMapping("/{feedbackId}")
    public Result<Void> deleteFeedback(
            @PathVariable Long feedbackId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        feedbackService.deleteFeedback(feedbackId, userId);
        return Result.success("删除成功");
    }
}

