package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.CeramicContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 陶瓷文化内容Mapper
 * SQL语句定义在 CeramicContentMapper.xml 中
 */
@Mapper
public interface CeramicContentMapper extends BaseMapper<CeramicContent> {

    /**
     * 增加浏览量
     * 
     * @param id 内容ID
     */
    void incrementViews(Long id);
}

