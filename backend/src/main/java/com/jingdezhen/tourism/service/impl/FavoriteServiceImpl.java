package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.entity.Favorite;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.FavoriteMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.service.FavoriteService;
import com.jingdezhen.tourism.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏Service实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long productId, Long userId) {
        // 检查产品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        // 使用自定义方法查询（包括已删除的记录）
        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        if (existingFavorite != null) {
            if (existingFavorite.getDeleted() == 0) {
                // 已收藏且未删除
                throw new BusinessException("已经收藏过了");
            } else {
                // 已收藏但被删除了，恢复收藏
                existingFavorite.setDeleted(0);
                favoriteMapper.updateById(existingFavorite);
                return;
            }
        }

        // 不存在任何记录，添加新收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long productId, Long userId) {
        // 使用自定义方法查询（包括已删除的记录）
        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        if (existingFavorite == null) {
            // 记录不存在，直接返回（目标已达成）
            return;
        }
        
        if (existingFavorite.getDeleted() == 1) {
            // 已经被删除了，直接返回
            return;
        }
        
        // 执行逻辑删除
        favoriteMapper.deleteById(existingFavorite.getId());
    }

    @Override
    public boolean isFavorite(Long productId, Long userId) {
        // 使用自定义方法查询（包括已删除的记录）
        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        // 存在且未被逻辑删除才算已收藏
        return existingFavorite != null && existingFavorite.getDeleted() == 0;
    }

    @Override
    public Page<FavoriteVO> getUserFavorites(Long userId, Long current, Long size) {
        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, wrapper);

        // 查询产品信息
        List<Long> productIds = favoritePage.getRecords().stream()
                .map(Favorite::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        // 转换为VO
        Map<Long, Product> finalProductMap = productMap;
        List<FavoriteVO> voList = favoritePage.getRecords().stream().map(favorite -> {
            FavoriteVO vo = new FavoriteVO();
            BeanUtils.copyProperties(favorite, vo);

            Product product = finalProductMap.get(favorite.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductCoverImage(product.getCoverImage());
                vo.setProductPrice(product.getPrice());
                vo.setProductStatus(product.getStatus());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<FavoriteVO> voPage = new Page<>(favoritePage.getCurrent(), favoritePage.getSize(), favoritePage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long productId, Long userId) {
        // 检查产品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        // 查询记录（包括已删除的）
        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        if (existingFavorite != null) {
            // 记录存在，切换 deleted 状态
            if (existingFavorite.getDeleted() == 0) {
                // 当前已收藏(deleted=0)，改为取消收藏(deleted=1)
                favoriteMapper.updateDeletedStatus(existingFavorite.getId(), 1);
                return false;
            } else {
                // 当前已取消(deleted=1)，改为收藏(deleted=0)
                favoriteMapper.updateDeletedStatus(existingFavorite.getId(), 0);
                return true;
            }
        } else {
            // 记录不存在，创建新记录(deleted=0表示收藏)
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favorite.setDeleted(0);
            favoriteMapper.insert(favorite);
            return true;
        }
    }

    @Override
    public Long getFavoriteCount(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        return favoriteMapper.selectCount(wrapper);
    }
}

