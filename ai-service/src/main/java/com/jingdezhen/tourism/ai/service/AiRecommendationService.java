package com.jingdezhen.tourism.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.AiRecommendationRequestDTO;
import com.jingdezhen.tourism.common.dto.AiRecommendationResponseDTO;
import com.jingdezhen.tourism.common.vo.AiRecommendationVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI推荐服务接口
 */
public interface AiRecommendationService {

    /**
     * 获取AI推荐（非流式）
     */
    AiRecommendationResponseDTO getRecommendation(Long userId, AiRecommendationRequestDTO request);

    /**
     * 获取AI推荐（流式SSE）
     */
    void getRecommendationStream(Long userId, AiRecommendationRequestDTO request, SseEmitter emitter);

    /**
     * 获取用户的推荐历史
     */
    Page<AiRecommendationVO> getRecommendationHistory(Long userId, Long current, Long size);

    /**
     * 提交用户反馈
     */
    void submitFeedback(Long userId, Long recommendationId, Integer feedback);

    /**
     * 获取推荐统计
     */
    Object getRecommendationStats(Long userId);
}

