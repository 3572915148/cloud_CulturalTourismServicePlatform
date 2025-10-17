package com.jingdezhen.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.service.FavoriteService;
import com.jingdezhen.tourism.utils.TokenUtil;
import com.jingdezhen.tourism.vo.FavoriteVO;
import com.jingdezhen.tourism.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏Controller
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final TokenUtil tokenUtil;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<Void> addFavorite(
            @RequestParam Long productId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        favoriteService.addFavorite(productId, userId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove")
    public Result<Void> removeFavorite(
            @RequestParam Long productId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        favoriteService.removeFavorite(productId, userId);
        return Result.success("取消收藏成功");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(
            @RequestParam Long productId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        boolean isFavorite = favoriteService.isFavorite(productId, userId);
        return Result.success(isFavorite);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my")
    public Result<Page<FavoriteVO>> getMyFavorites(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        Page<FavoriteVO> page = favoriteService.getUserFavorites(userId, current, size);
        return Result.success(page);
    }

    /**
     * 切换收藏状态（智能收藏/取消收藏）
     */
    @PostMapping("/toggle")
    public Result<Boolean> toggleFavorite(
            @RequestParam Long productId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        boolean isNowFavorite = favoriteService.toggleFavorite(productId, userId);
        return Result.success(isNowFavorite);
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public Result<Long> getFavoriteCount(@RequestHeader("Authorization") String authHeader) {
        Long userId = tokenUtil.getUserIdFromAuth(authHeader);
        Long count = favoriteService.getFavoriteCount(userId);
        return Result.success(count);
    }
}

