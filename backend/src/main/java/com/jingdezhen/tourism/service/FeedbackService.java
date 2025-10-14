package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.FeedbackCreateDTO;
import com.jingdezhen.tourism.vo.FeedbackVO;

/**
 * 反馈Service
 */
public interface FeedbackService {

    /**
     * 创建反馈
     *
     * @param dto 反馈信息
     * @param userId 用户ID
     * @return 反馈VO
     */
    FeedbackVO createFeedback(FeedbackCreateDTO dto, Long userId);

    /**
     * 获取用户反馈列表
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 反馈分页列表
     */
    Page<FeedbackVO> getUserFeedbacks(Long userId, Long current, Long size);

    /**
     * 获取反馈详情
     *
     * @param feedbackId 反馈ID
     * @param userId 用户ID
     * @return 反馈VO
     */
    FeedbackVO getFeedbackDetail(Long feedbackId, Long userId);

    /**
     * 删除反馈
     *
     * @param feedbackId 反馈ID
     * @param userId 用户ID
     */
    void deleteFeedback(Long feedbackId, Long userId);
}

