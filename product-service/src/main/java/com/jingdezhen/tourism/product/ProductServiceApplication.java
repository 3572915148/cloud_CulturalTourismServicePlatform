package com.jingdezhen.tourism.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.product", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.jingdezhen.tourism.product.mapper")
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("商品服务启动成功！");
        System.out.println("========================================");
    }
}

