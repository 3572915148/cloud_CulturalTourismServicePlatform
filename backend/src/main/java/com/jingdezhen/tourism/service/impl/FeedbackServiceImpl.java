package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.FeedbackCreateDTO;
import com.jingdezhen.tourism.entity.Feedback;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.FeedbackMapper;
import com.jingdezhen.tourism.service.FeedbackService;
import com.jingdezhen.tourism.vo.FeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 反馈Service实现
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedbackVO createFeedback(FeedbackCreateDTO dto, Long userId) {
        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(dto, feedback);
        feedback.setUserId(userId);
        feedback.setStatus(0); // 待处理

        feedbackMapper.insert(feedback);

        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        vo.setTypeText(getTypeText(feedback.getType()));
        vo.setStatusText(getStatusText(feedback.getStatus()));

        return vo;
    }

    @Override
    public Page<FeedbackVO> getUserFeedbacks(Long userId, Long current, Long size) {
        Page<Feedback> page = new Page<>(current, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId);
        wrapper.orderByDesc(Feedback::getCreateTime);

        Page<Feedback> feedbackPage = feedbackMapper.selectPage(page, wrapper);

        // 转换为VO
        List<FeedbackVO> voList = feedbackPage.getRecords().stream().map(feedback -> {
            FeedbackVO vo = new FeedbackVO();
            BeanUtils.copyProperties(feedback, vo);
            vo.setTypeText(getTypeText(feedback.getType()));
            vo.setStatusText(getStatusText(feedback.getStatus()));
            return vo;
        }).collect(Collectors.toList());

        Page<FeedbackVO> voPage = new Page<>(feedbackPage.getCurrent(), feedbackPage.getSize(), feedbackPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public FeedbackVO getFeedbackDetail(Long feedbackId, Long userId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        if (!feedback.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此反馈");
        }

        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        vo.setTypeText(getTypeText(feedback.getType()));
        vo.setStatusText(getStatusText(feedback.getStatus()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Long feedbackId, Long userId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        if (!feedback.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此反馈");
        }

        feedbackMapper.deleteById(feedbackId);
    }

    /**
     * 获取反馈类型文本
     */
    private String getTypeText(Integer type) {
        if (type == null) {
            return "未知类型";
        }
        switch (type) {
            case 1: return "功能建议";
            case 2: return "问题反馈";
            case 3: return "投诉";
            default: return "未知类型";
        }
    }

    /**
     * 获取处理状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "已处理";
            case 3: return "已关闭";
            default: return "未知状态";
        }
    }
}

