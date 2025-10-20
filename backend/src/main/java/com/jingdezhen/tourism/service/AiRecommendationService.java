package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.AiRecommendationRequestDTO;
import com.jingdezhen.tourism.dto.AiRecommendationResponseDTO;
import com.jingdezhen.tourism.vo.AiRecommendationVO;

/**
 * AI推荐服务接口
 */
public interface AiRecommendationService {

    /**
     * 获取AI推荐
     */
    AiRecommendationResponseDTO getRecommendation(Long userId, AiRecommendationRequestDTO request);

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
