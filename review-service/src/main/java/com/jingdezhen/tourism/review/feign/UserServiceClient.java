package com.jingdezhen.tourism.review.feign;

import com.jingdezhen.tourism.common.entity.User;
import com.jingdezhen.tourism.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service", path = "/user")
public interface UserServiceClient {

    @GetMapping("/entity/{id}")
    Result<User> getUserById(@PathVariable Long id);
}

