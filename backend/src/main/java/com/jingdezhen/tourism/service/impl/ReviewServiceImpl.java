package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.ReviewCreateDTO;
import com.jingdezhen.tourism.entity.Orders;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.entity.Review;
import com.jingdezhen.tourism.entity.User;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.OrdersMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.mapper.ReviewMapper;
import com.jingdezhen.tourism.mapper.UserMapper;
import com.jingdezhen.tourism.service.ReviewService;
import com.jingdezhen.tourism.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价Service实现
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewVO createReview(ReviewCreateDTO dto, Long userId) {
        // 检查订单是否存在且属于当前用户
        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("只有已完成的订单才能评价");
        }

        // 允许同一订单多次评价，移除了"已评价"检查

        // 创建评价
        Review review = new Review();
        BeanUtils.copyProperties(dto, review);
        review.setUserId(userId);
        review.setMerchantId(order.getMerchantId());
        review.setStatus(1); // 默认显示

        reviewMapper.insert(review);

        // 更新产品评分
        updateProductRating(dto.getProductId());

        // 返回评价信息
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }

        // 查询产品信息
        Product product = productMapper.selectById(dto.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
        }

        return vo;
    }

    @Override
    public Page<ReviewVO> getProductReviews(Long productId, Long current, Long size) {
        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);
        wrapper.orderByDesc(Review::getCreateTime);

        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        // 查询用户信息
        List<Long> userIds = reviewPage.getRecords().stream()
                .map(Review::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        // 转换为VO
        Map<Long, User> finalUserMap = userMap;
        List<ReviewVO> voList = reviewPage.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(review, vo);

            User user = finalUserMap.get(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public Page<ReviewVO> getUserReviews(Long userId, Long current, Long size) {
        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId);
        wrapper.orderByDesc(Review::getCreateTime);

        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        // 查询产品信息
        List<Long> productIds = reviewPage.getRecords().stream()
                .map(Review::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        // 转换为VO
        Map<Long, Product> finalProductMap = productMap;
        List<ReviewVO> voList = reviewPage.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(review, vo);

            Product product = finalProductMap.get(review.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评价");
        }

        reviewMapper.deleteById(reviewId);

        // 更新产品评分
        updateProductRating(review.getProductId());
    }

    @Override
    public Page<ReviewVO> getMerchantReviews(Long merchantId, Long current, Long size) {
        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getMerchantId, merchantId);
        wrapper.orderByDesc(Review::getCreateTime);

        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        // 关联用户与产品信息
        List<Long> userIds = reviewPage.getRecords().stream().map(Review::getUserId).distinct().collect(Collectors.toList());
        List<Long> productIds = reviewPage.getRecords().stream().map(Review::getProductId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        Map<Long, User> finalUserMap = userMap;
        Map<Long, Product> finalProductMap = productMap;
        List<ReviewVO> voList = reviewPage.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(review, vo);
            User user = finalUserMap.get(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
            Product product = finalProductMap.get(review.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void replyReview(Long reviewId, Long merchantId, String replyContent) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!merchantId.equals(review.getMerchantId())) {
            throw new BusinessException("无权回复他人产品的评价");
        }
        Review update = new Review();
        update.setId(reviewId);
        update.setReplyContent(replyContent);
        update.setReplyTime(java.time.LocalDateTime.now());
        reviewMapper.updateById(update);
    }

    /**
     * 更新产品评分
     */
    private void updateProductRating(Long productId) {
        // 查询该产品的所有评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            // 没有评价，设置为默认5.0
            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setRating(new BigDecimal("5.0"));
                productMapper.updateById(product);
            }
        } else {
            // 计算平均评分
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(5.0);

            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setRating(new BigDecimal(avgRating).setScale(1, RoundingMode.HALF_UP));
                productMapper.updateById(product);
            }
        }
    }
}

