package com.jingdezhen.tourism.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.common.dto.ReviewCreateDTO;
import com.jingdezhen.tourism.common.entity.Orders;
import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.entity.Review;
import com.jingdezhen.tourism.common.entity.User;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.common.vo.ReviewVO;
import com.jingdezhen.tourism.review.feign.OrderServiceClient;
import com.jingdezhen.tourism.review.feign.ProductServiceClient;
import com.jingdezhen.tourism.review.feign.UserServiceClient;
import com.jingdezhen.tourism.review.mapper.ReviewMapper;
import com.jingdezhen.tourism.review.service.MessageProducerService;
import com.jingdezhen.tourism.review.service.ReviewService;
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
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final MessageProducerService messageProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewVO createReview(ReviewCreateDTO dto, Long userId) {
        // 检查订单是否存在且属于当前用户（通过Feign调用order-service）
        Result<Orders> orderResult = orderServiceClient.getOrderById(dto.getOrderId());
        if (orderResult.getCode() != 200 || orderResult.getData() == null) {
            throw new BusinessException("订单不存在");
        }
        Orders order = orderResult.getData();
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("只有已完成的订单才能评价");
        }

        // 创建评价
        Review review = new Review();
        BeanUtils.copyProperties(dto, review);
        review.setUserId(userId);
        review.setMerchantId(order.getMerchantId());
        review.setStatus(1); // 默认显示

        reviewMapper.insert(review);

        // 发送评论创建消息，异步更新产品评分
        messageProducerService.sendReviewChangedMessage(
            review.getId(), 
            dto.getProductId(), 
            MessageProducerService.CHANGE_TYPE_CREATE
        );

        // 返回评价信息
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 查询用户信息（通过Feign调用user-service）
        Result<User> userResult = userServiceClient.getUserById(userId);
        if (userResult.getCode() == 200 && userResult.getData() != null) {
            User user = userResult.getData();
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }

        // 查询产品信息（通过Feign调用product-service）
        Result<Product> productResult = productServiceClient.getProductById(dto.getProductId());
        if (productResult.getCode() == 200 && productResult.getData() != null) {
            Product product = productResult.getData();
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

        // 查询用户信息（批量调用user-service）
        List<Long> userIds = reviewPage.getRecords().stream()
                .map(Review::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        for (Long userId : userIds) {
            Result<User> userResult = userServiceClient.getUserById(userId);
            if (userResult.getCode() == 200 && userResult.getData() != null) {
                userMap.put(userId, userResult.getData());
            }
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

        // 查询产品信息（批量调用product-service）
        List<Long> productIds = reviewPage.getRecords().stream()
                .map(Review::getProductId)
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

        // 发送评论删除消息，异步更新产品评分
        messageProducerService.sendReviewChangedMessage(
            reviewId, 
            review.getProductId(), 
            MessageProducerService.CHANGE_TYPE_DELETE
        );
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
        for (Long userId : userIds) {
            Result<User> userResult = userServiceClient.getUserById(userId);
            if (userResult.getCode() == 200 && userResult.getData() != null) {
                userMap.put(userId, userResult.getData());
            }
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Long productId : productIds) {
            Result<Product> productResult = productServiceClient.getProductById(productId);
            if (productResult.getCode() == 200 && productResult.getData() != null) {
                productMap.put(productId, productResult.getData());
            }
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

    @Override
    public java.util.List<Review> getProductAllReviews(Long productId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);
        return reviewMapper.selectList(wrapper);
    }

    /**
     * 更新产品评分（通过Feign调用product-service）
     * 注意：此方法已废弃，现在通过消息队列异步更新
     */
    @Deprecated
    private void updateProductRating(Long productId) {
        // 查询该产品的所有评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            // 没有评价，设置为默认5.0
            Result<Product> productResult = productServiceClient.getProductById(productId);
            if (productResult.getCode() == 200 && productResult.getData() != null) {
                Product product = productResult.getData();
                product.setRating(new BigDecimal("5.0"));
                productServiceClient.updateProduct(product);
            }
        } else {
            // 计算平均评分
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(5.0);

            Result<Product> productResult = productServiceClient.getProductById(productId);
            if (productResult.getCode() == 200 && productResult.getData() != null) {
                Product product = productResult.getData();
                product.setRating(new BigDecimal(avgRating).setScale(1, RoundingMode.HALF_UP));
                productServiceClient.updateProduct(product);
            }
        }
    }
}

