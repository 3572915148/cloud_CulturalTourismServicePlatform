package com.jingdezhen.tourism.feedback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.FeedbackCreateDTO;
import com.jingdezhen.tourism.common.vo.FeedbackVO;

public interface FeedbackService {
    FeedbackVO createFeedback(FeedbackCreateDTO dto, Long userId);
    Page<FeedbackVO> getUserFeedbacks(Long userId, Long current, Long size);
    FeedbackVO getFeedbackDetail(Long feedbackId, Long userId);
    void deleteFeedback(Long feedbackId, Long userId);
}

