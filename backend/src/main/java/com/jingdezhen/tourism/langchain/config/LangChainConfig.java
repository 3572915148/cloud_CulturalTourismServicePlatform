package com.jingdezhen.tourism.langchain.config;

import com.jingdezhen.tourism.langchain.service.TourismAssistant;
import com.jingdezhen.tourism.langchain.tools.*;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LangChain4j 配置类
 * 配置 AI 模型、会话记忆、工具等
 * 
 * @author AI Assistant
 */
@Slf4j
@Configuration
public class LangChainConfig {
    
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;
    
    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;
    
    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;
    
    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;
    
    @Value("${langchain4j.open-ai.chat-model.max-tokens:2000}")
    private Integer maxTokens;
    
    @Value("${langchain4j.open-ai.chat-model.timeout:60s}")
    private Duration timeout;
    
    @Value("${langchain4j.open-ai.chat-model.log-requests:false}")
    private Boolean logRequests;
    
    @Value("${langchain4j.open-ai.chat-model.log-responses:false}")
    private Boolean logResponses;
    
    /**
     * 配置流式聊天模型（支持DeepSeek、OpenAI等）
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("🤖 初始化流式聊天模型: baseUrl={}, model={}", baseUrl, modelName);
        
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(timeout)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
    
    /**
     * 配置会话记忆存储
     * 使用内存存储（生产环境建议使用Redis）
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        log.info("💾 初始化会话记忆存储: 使用内存存储");
        return new InMemoryChatMemoryStore();
    }
    
    /**
     * 配置 TourismAssistant AI 服务
     * LangChain4j 会自动实现这个接口
     */
    @Bean
    public TourismAssistant tourismAssistant(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemoryStore chatMemoryStore,
            ProductTools productTools,
            OrderTools orderTools,
            RecommendationTools recommendationTools) {
        
        log.info("🎯 初始化 TourismAssistant AI 服务");
        log.info("📦 注册工具: ProductTools, OrderTools, RecommendationTools");
        
        return AiServices.builder(TourismAssistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)  // 保留最近20条消息
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .tools(productTools, orderTools, recommendationTools)  // 注册所有工具
                .build();
    }
    
    /**
     * 内存会话记忆存储实现
     * 生产环境建议使用 Redis 存储
     */
    public static class InMemoryChatMemoryStore implements ChatMemoryStore {
        private final Map<Object, List<ChatMessage>> store = new ConcurrentHashMap<>();
        
        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            List<ChatMessage> messages = store.get(memoryId);
            // 如果会话不存在，返回可变的空列表而不是 null
            return messages != null ? messages : new java.util.ArrayList<>();
        }
        
        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            store.put(memoryId, messages);
        }
        
        @Override
        public void deleteMessages(Object memoryId) {
            store.remove(memoryId);
        }
    }
}

