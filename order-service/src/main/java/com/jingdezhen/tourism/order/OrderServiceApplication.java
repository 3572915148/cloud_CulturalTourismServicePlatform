package com.jingdezhen.tourism.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.order", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.jingdezhen.tourism.order.feign")
@MapperScan("com.jingdezhen.tourism.order.mapper")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("订单服务启动成功！");
    }
}

