package com.jingdezhen.tourism.feedback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.FeedbackCreateDTO;
import com.jingdezhen.tourism.common.utils.TokenUtil;
import com.jingdezhen.tourism.common.vo.FeedbackVO;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.feedback.service.FeedbackService;
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

    @PostMapping("/create")
    public Result<FeedbackVO> createFeedback(
            @Validated @RequestBody FeedbackCreateDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        FeedbackVO feedback = feedbackService.createFeedback(dto, userId);
        return Result.success("反馈提交成功", feedback);
    }

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

    @GetMapping("/{feedbackId}")
    public Result<FeedbackVO> getFeedbackDetail(
            @PathVariable Long feedbackId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        FeedbackVO feedback = feedbackService.getFeedbackDetail(feedbackId, userId);
        return Result.success(feedback);
    }

    @DeleteMapping("/{feedbackId}")
    public Result<Void> deleteFeedback(
            @PathVariable Long feedbackId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        feedbackService.deleteFeedback(feedbackId, userId);
        return Result.success("删除成功");
    }
}

