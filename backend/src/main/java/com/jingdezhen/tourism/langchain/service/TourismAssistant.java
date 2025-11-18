package com.jingdezhen.tourism.langchain.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 景德镇文旅 AI 智能助手接口
 * LangChain4j 会自动实现这个接口
 * 
 * 注意：系统提示词已迁移到 prompts.yml 配置文件
 * 虽然接口上仍使用 @SystemMessage 注解（LangChain4j要求），
 * 但提示词内容统一在 prompts.yml 中管理，便于维护和优化
 * 
 * @author AI Assistant
 */
public interface TourismAssistant {
    
    /**
     * 流式对话（通用场景）
     * 系统提示词：参考 prompts.yml 中的 prompts.system.default
     * 
     * @param memoryId 会话ID（用于管理会话记忆）
     * @param userMessage 用户消息
     * @return 流式响应
     */
    @SystemMessage(fromResource = "system-prompt.txt")
    TokenStream chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
