package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.CeramicContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 陶瓷文化内容Mapper
 */
@Mapper
public interface CeramicContentMapper extends BaseMapper<CeramicContent> {

    /**
     * 增加浏览量
     * @param id 内容ID
     */
    @Update("UPDATE ceramic_content SET views = views + 1 WHERE id = #{id} AND deleted = 0")
    void incrementViews(Long id);
}

