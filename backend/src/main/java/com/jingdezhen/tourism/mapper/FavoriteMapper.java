package com.jingdezhen.tourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    
    /**
     * 查询收藏记录（包括已删除的）
     * 用于处理重复收藏的情况
     */
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND product_id = #{productId} LIMIT 1")
    Favorite selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 更新deleted状态（直接修改deleted字段，不受逻辑删除限制）
     * @param id 收藏记录ID
     * @param deleted 新的deleted状态：0=收藏，1=取消收藏
     */
    @Update("UPDATE favorite SET deleted = #{deleted}, update_time = NOW() WHERE id = #{id}")
    int updateDeletedStatus(@Param("id") Long id, @Param("deleted") Integer deleted);
}

