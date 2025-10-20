package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.AiRecommendationRequestDTO;
import com.jingdezhen.tourism.dto.AiRecommendationResponseDTO;
import com.jingdezhen.tourism.service.AiRecommendationService;
import com.jingdezhen.tourism.vo.AiRecommendationVO;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.jingdezhen.tourism.utils.TokenUtil;

/**
 * AI推荐控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;
    private final TokenUtil tokenUtil;

    /**
     * 获取AI推荐
     */
    @PostMapping("/recommend")
    public Result<AiRecommendationResponseDTO> getRecommendation(
            @Valid @RequestBody AiRecommendationRequestDTO request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("用户 {} 请求AI推荐: {}", userId, request.getQuery());
        
        AiRecommendationResponseDTO response = aiRecommendationService.getRecommendation(userId, request);
        
        return Result.success(response);
    }

    /**
     * 获取推荐历史
     */
    @GetMapping("/history")
    public Result<Page<AiRecommendationVO>> getRecommendationHistory(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        
        Page<AiRecommendationVO> history = aiRecommendationService.getRecommendationHistory(userId, current, size);
        
        return Result.success(history);
    }

    /**
     * 提交反馈
     */
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(
            @RequestParam @NotNull Long recommendationId,
            @RequestParam @NotNull Integer feedback,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        
        aiRecommendationService.submitFeedback(userId, recommendationId, feedback);
        
        return Result.success();
    }

    /**
     * 获取推荐统计
     */
    @GetMapping("/stats")
    public Result<Object> getRecommendationStats(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        Object stats = aiRecommendationService.getRecommendationStats(userId);
        
        return Result.success(stats);
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            return tokenUtil.getUserIdFromAuth(authHeader);
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            throw new RuntimeException("用户未登录或登录已过期");
        }
    }
}
