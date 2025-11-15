package com.jingdezhen.tourism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 景德镇文旅服务平台启动类
 */
@SpringBootApplication
@EnableScheduling
public class TourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourismApplication.class, args);
        System.out.println("========================================");
        System.out.println("景德镇文旅服务平台启动成功！");
        System.out.println("========================================");
    }
}

