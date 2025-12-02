package com.jingdezhen.tourism.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingdezhen.tourism.common.entity.Favorite;
import org.apache.ibatis.annotations.Param;

public interface FavoriteMapper extends BaseMapper<Favorite> {
    Favorite selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
    int updateDeletedStatus(@Param("id") Long id, @Param("deleted") Integer deleted);
}

