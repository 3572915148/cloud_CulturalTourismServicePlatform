package com.jingdezhen.tourism.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.merchant", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@MapperScan("com.jingdezhen.tourism.merchant.mapper")
public class MerchantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceApplication.class, args);
        System.out.println("商户服务启动成功！");
    }
}

