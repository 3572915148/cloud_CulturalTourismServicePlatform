package com.jingdezhen.tourism.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.review", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.jingdezhen.tourism.review.feign")
@MapperScan("com.jingdezhen.tourism.review.mapper")
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
        System.out.println("评价服务启动成功！");
    }
}

