package com.jingdezhen.tourism.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.common.entity.CeramicContent;

/**
 * 陶瓷文化内容Mapper
 */
public interface CeramicContentMapper extends BaseMapper<CeramicContent> {

    /**
     * 增加浏览量
     */
    void incrementViews(Long id);
}

