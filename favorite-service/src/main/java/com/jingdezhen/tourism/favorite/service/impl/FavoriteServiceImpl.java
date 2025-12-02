package com.jingdezhen.tourism.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.entity.Favorite;
import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.common.vo.FavoriteVO;
import com.jingdezhen.tourism.favorite.feign.ProductServiceClient;
import com.jingdezhen.tourism.favorite.mapper.FavoriteMapper;
import com.jingdezhen.tourism.favorite.service.FavoriteService;
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
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long productId, Long userId) {
        // 检查产品是否存在（通过Feign调用product-service）
        Result<Product> productResult = productServiceClient.getProductById(productId);
        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new BusinessException("产品不存在");
        }

        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        if (existingFavorite != null) {
            if (existingFavorite.getDeleted() == 0) {
                throw new BusinessException("已经收藏过了");
            } else {
                existingFavorite.setDeleted(0);
                favoriteMapper.updateById(existingFavorite);
                return;
            }
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long productId, Long userId) {
        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        if (existingFavorite == null || existingFavorite.getDeleted() == 1) {
            return;
        }
        
        favoriteMapper.deleteById(existingFavorite.getId());
    }

    @Override
    public boolean isFavorite(Long productId, Long userId) {
        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        return existingFavorite != null && existingFavorite.getDeleted() == 0;
    }

    @Override
    public Page<FavoriteVO> getUserFavorites(Long userId, Long current, Long size) {
        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favoritePage = favoriteMapper.selectPage(page, wrapper);

        // 查询产品信息（批量调用product-service）
        List<Long> productIds = favoritePage.getRecords().stream()
                .map(Favorite::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        for (Long productId : productIds) {
            Result<Product> productResult = productServiceClient.getProductById(productId);
            if (productResult.getCode() == 200 && productResult.getData() != null) {
                productMap.put(productId, productResult.getData());
            }
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
        Result<Product> productResult = productServiceClient.getProductById(productId);
        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new BusinessException("产品不存在");
        }

        Favorite existingFavorite = favoriteMapper.selectByUserAndProduct(userId, productId);
        
        if (existingFavorite != null) {
            if (existingFavorite.getDeleted() == 0) {
                favoriteMapper.updateDeletedStatus(existingFavorite.getId(), 1);
                return false;
            } else {
                favoriteMapper.updateDeletedStatus(existingFavorite.getId(), 0);
                return true;
            }
        } else {
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

