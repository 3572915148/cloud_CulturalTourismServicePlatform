package com.jingdezhen.tourism.favorite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.favorite", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.jingdezhen.tourism.favorite.feign")
@MapperScan("com.jingdezhen.tourism.favorite.mapper")
public class FavoriteServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FavoriteServiceApplication.class, args);
        System.out.println("收藏服务启动成功！");
    }
}

