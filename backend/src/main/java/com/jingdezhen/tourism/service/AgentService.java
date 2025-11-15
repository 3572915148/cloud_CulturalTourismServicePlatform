package com.jingdezhen.tourism.service;

import com.jingdezhen.tourism.agent.core.ConversationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Agent服务接口
 * 
 * @author AI Assistant
 */
public interface AgentService {
    
    /**
     * 流式对话
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param message 用户消息
     * @param emitter SSE发射器
     */
    void chatStream(String sessionId, Long userId, String message, SseEmitter emitter);
    
    /**
     * 获取会话上下文
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话上下文
     */
    ConversationContext getSession(String sessionId, Long userId);
    
    /**
     * 清除会话
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void clearSession(String sessionId, Long userId);
    
    /**
     * 获取可用工具列表
     * 
     * @return 工具列表
     */
    List<Map<String, Object>> getAvailableTools();
}

