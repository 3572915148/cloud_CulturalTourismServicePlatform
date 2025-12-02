package com.jingdezhen.tourism.favorite.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.vo.FavoriteVO;

public interface FavoriteService {
    void addFavorite(Long productId, Long userId);
    void removeFavorite(Long productId, Long userId);
    boolean isFavorite(Long productId, Long userId);
    Page<FavoriteVO> getUserFavorites(Long userId, Long current, Long size);
    boolean toggleFavorite(Long productId, Long userId);
    Long getFavoriteCount(Long userId);
}

