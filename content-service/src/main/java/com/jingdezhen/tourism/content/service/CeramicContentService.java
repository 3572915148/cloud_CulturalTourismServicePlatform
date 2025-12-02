package com.jingdezhen.tourism.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdezhen.tourism.common.entity.CeramicContent;
import com.jingdezhen.tourism.common.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 陶瓷文化内容服务接口
 */
public interface CeramicContentService extends IService<CeramicContent> {

    /**
     * 分页查询陶瓷文化内容列表
     */
    PageResult<CeramicContent> getContentList(Integer current, Integer size, String category);

    /**
     * 根据ID查询陶瓷文化内容详情
     */
    CeramicContent getContentById(Long id);

    /**
     * 增加浏览量
     */
    void incrementViews(Long id);

    /**
     * 获取所有分类及统计
     */
    List<Map<String, Object>> getCategoryStatistics();
}

