package com.jingdezhen.tourism.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.jingdezhen.tourism.file", "com.jingdezhen.tourism.common"}, 
                       exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class FileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
        System.out.println("文件服务启动成功！");
    }
}

