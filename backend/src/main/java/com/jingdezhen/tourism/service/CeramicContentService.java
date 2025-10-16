package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingdezhen.tourism.entity.CeramicContent;
import com.jingdezhen.tourism.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 陶瓷文化内容服务接口
 */
public interface CeramicContentService extends IService<CeramicContent> {

    /**
     * 分页查询陶瓷文化内容列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param category 分类（可选）
     * @return 分页结果
     */
    PageResult<CeramicContent> getContentList(Integer current, Integer size, String category);

    /**
     * 根据ID查询陶瓷文化内容详情
     *
     * @param id 内容ID
     * @return 内容详情
     */
    CeramicContent getContentById(Long id);

    /**
     * 增加浏览量
     *
     * @param id 内容ID
     */
    void incrementViews(Long id);

    /**
     * 获取所有分类及统计
     *
     * @return 分类列表及每个分类的内容数量
     */
    List<Map<String, Object>> getCategoryStatistics();
}

