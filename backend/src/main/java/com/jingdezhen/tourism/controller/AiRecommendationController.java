package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.AiRecommendationRequestDTO;
import com.jingdezhen.tourism.dto.AiRecommendationResponseDTO;
import com.jingdezhen.tourism.agent.core.ConversationContext;
import com.jingdezhen.tourism.service.AgentService;
import com.jingdezhen.tourism.service.AiRecommendationService;
import com.jingdezhen.tourism.vo.AiRecommendationVO;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.jingdezhen.tourism.utils.TokenUtil;

import java.util.HashMap;
import java.util.Map;

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
    private final AgentService agentService;
    private final TokenUtil tokenUtil;

    /**
     * 获取AI推荐（非流式）
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
     * 获取AI推荐（流式SSE）
     */
    @PostMapping(value = "/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getRecommendationStream(
            @Valid @RequestBody AiRecommendationRequestDTO request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("用户 {} 请求AI流式推荐: {}", userId, request.getQuery());
        
        // 创建SSE发射器，设置超时时间为3分钟（缩短超时时间）
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        
        try {
            // 在主线程中完成数据查询（避免异步线程访问已关闭的数据库）
            aiRecommendationService.getRecommendationStream(userId, request, emitter);
        } catch (Exception e) {
            log.error("创建流式推荐任务失败", e);
            emitter.completeWithError(e);
        }
        
        return emitter;
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
     * 根据推荐记录ID恢复会话（缓存回填策略）
     * 先从Redis查找，如果不存在则从数据库恢复并保存到Redis
     */
    @PostMapping("/restore-session")
    public Result<Map<String, Object>> restoreSession(
            @RequestParam @NotNull Long recommendationId,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("用户 {} 请求恢复会话: recommendationId={}", userId, recommendationId);
        
        ConversationContext context = agentService.restoreSessionByRecommendationId(recommendationId, userId);
        
        if (context == null) {
            return Result.error("恢复会话失败：推荐记录不存在或不属于当前用户");
        }
        
        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", context.getSessionId());
        result.put("userId", context.getUserId());
        result.put("messageCount", context.getHistory() != null ? context.getHistory().size() : 0);
        result.put("createTime", context.getCreateTime());
        result.put("lastActiveTime", context.getLastActiveTime());
        
        log.info("✅ 会话恢复成功: recommendationId={}, sessionId={}", recommendationId, context.getSessionId());
        
        return Result.success(result);
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
