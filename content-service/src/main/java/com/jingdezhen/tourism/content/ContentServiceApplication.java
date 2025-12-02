package com.jingdezhen.tourism.content;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.content", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@MapperScan("com.jingdezhen.tourism.content.mapper")
public class ContentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
        System.out.println("内容服务启动成功！");
    }
}

