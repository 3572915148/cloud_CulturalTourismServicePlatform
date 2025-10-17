package com.jingdezhen.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.vo.FavoriteVO;

/**
 * 收藏Service
 */
public interface FavoriteService {

    /**
     * 添加收藏
     *
     * @param productId 产品ID
     * @param userId    用户ID
     */
    void addFavorite(Long productId, Long userId);

    /**
     * 取消收藏
     *
     * @param productId 产品ID
     * @param userId    用户ID
     */
    void removeFavorite(Long productId, Long userId);

    /**
     * 检查是否已收藏
     *
     * @param productId 产品ID
     * @param userId    用户ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long productId, Long userId);

    /**
     * 获取用户收藏列表
     *
     * @param userId  用户ID
     * @param current 当前页
     * @param size    每页大小
     * @return 收藏列表
     */
    Page<FavoriteVO> getUserFavorites(Long userId, Long current, Long size);

    /**
     * 切换收藏状态（智能收藏/取消收藏）
     *
     * @param productId 产品ID
     * @param userId    用户ID
     * @return 切换后的收藏状态（true=已收藏，false=未收藏）
     */
    boolean toggleFavorite(Long productId, Long userId);

    /**
     * 获取收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    Long getFavoriteCount(Long userId);
}

