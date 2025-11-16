package com.jingdezhen.tourism.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 为不同的业务场景配置专用的线程池，提升系统性能
 * 
 * @author AI Assistant
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    /**
     * 库存同步线程池
     * 用于定时任务批量同步库存到数据库
     * 特点：批量操作，可以并行处理多个商品
     */
    @Bean("stockSyncExecutor")
    public ThreadPoolTaskExecutor stockSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 核心线程数
        executor.setMaxPoolSize(10);  // 最大线程数
        executor.setQueueCapacity(100);  // 队列容量
        executor.setThreadNamePrefix("stock-sync-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("✅ 库存同步线程池初始化完成: corePoolSize=5, maxPoolSize=10, queueCapacity=100");
        return executor;
    }
    
    /**
     * 库存异步同步线程池
     * 用于库存变更后的异步同步到数据库
     * 特点：高频、低延迟，需要快速响应
     */
    @Bean("stockAsyncExecutor")
    public ThreadPoolTaskExecutor stockAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("stock-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("✅ 库存异步同步线程池初始化完成: corePoolSize=3, maxPoolSize=5, queueCapacity=200");
        return executor;
    }
    
    /**
     * AI Agent工具执行线程池
     * 用于并行执行多个工具调用
     * 特点：工具调用可能涉及数据库查询、外部API调用等
     */
    @Bean("agentToolExecutor")
    public ThreadPoolTaskExecutor agentToolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("agent-tool-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("✅ AI Agent工具执行线程池初始化完成: corePoolSize=5, maxPoolSize=10, queueCapacity=50");
        return executor;
    }
    
    /**
     * AI流式处理线程池
     * 用于AI推荐流式响应处理
     * 特点：长连接，需要保持响应
     */
    @Bean("aiStreamExecutor")
    public ThreadPoolTaskExecutor aiStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-stream-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        log.info("✅ AI流式处理线程池初始化完成: corePoolSize=10, maxPoolSize=20, queueCapacity=100");
        return executor;
    }
    
    /**
     * 数据一致性检查线程池
     * 用于批量检查Redis和数据库的数据一致性
     */
    @Bean("consistencyCheckExecutor")
    public ThreadPoolTaskExecutor consistencyCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("consistency-check-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("✅ 数据一致性检查线程池初始化完成: corePoolSize=3, maxPoolSize=5, queueCapacity=50");
        return executor;
    }
    
    /**
     * 产品查询线程池
     * 用于并行查询多个产品详情
     */
    @Bean("productQueryExecutor")
    public ThreadPoolTaskExecutor productQueryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("product-query-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("✅ 产品查询线程池初始化完成: corePoolSize=5, maxPoolSize=10, queueCapacity=100");
        return executor;
    }
    
    /**
     * 文件处理线程池
     * 用于文件上传后的异步处理（如生成缩略图、文件校验等）
     */
    @Bean("fileProcessExecutor")
    public ThreadPoolTaskExecutor fileProcessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("file-process-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("✅ 文件处理线程池初始化完成: corePoolSize=3, maxPoolSize=5, queueCapacity=50");
        return executor;
    }
    
    /**
     * 通用异步任务线程池
     * 用于其他不需要专用线程池的异步任务
     */
    @Bean("asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("✅ 通用异步任务线程池初始化完成: corePoolSize=5, maxPoolSize=10, queueCapacity=100");
        return executor;
    }
}

