package com.jingdezhen.tourism.feedback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.feedback", "com.jingdezhen.tourism.common"})
@EnableDiscoveryClient
@MapperScan("com.jingdezhen.tourism.feedback.mapper")
public class FeedbackServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeedbackServiceApplication.class, args);
        System.out.println("反馈服务启动成功！");
    }
}

