package com.jingdezhen.tourism.product.feign;

import com.jingdezhen.tourism.common.entity.Review;
import com.jingdezhen.tourism.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 评论服务Feign客户端
 */
@FeignClient(name = "review-service", path = "/review")
public interface ReviewServiceClient {

    /**
     * 获取产品的所有评论（用于计算评分）
     */
    @GetMapping("/product/all")
    Result<List<Review>> getProductAllReviews(@RequestParam Long productId);
}

