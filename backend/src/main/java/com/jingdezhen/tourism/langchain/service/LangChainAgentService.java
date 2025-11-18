package com.jingdezhen.tourism.langchain.service;

import com.jingdezhen.tourism.langchain.config.PromptManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * LangChain4j Agent 服务实现
 * 使用 LangChain4j 框架，代码量大幅减少
 * 支持动态提示词加载和多场景切换
 * 
 * @author AI Assistant
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LangChainAgentService {
    
    private final TourismAssistant tourismAssistant;
    private final PromptManager promptManager;
    
    /**
     * 流式对话
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param message 用户消息
     * @param emitter SSE发射器
     */
    public void chatStream(String sessionId, Long userId, String message, SseEmitter emitter) {
        try {
            log.info("🤖 [LangChain4j] 开始对话: sessionId={}, userId={}, message={}", 
                    sessionId, userId, message);
            
            // 参数验证
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sendError(emitter, "会话ID不能为空");
                return;
            }
            
            if (message == null || message.trim().isEmpty()) {
                sendError(emitter, "消息内容不能为空");
                return;
            }
            
            // 构建会话ID（用户ID + 会话ID）
            String memoryId = userId + ":" + sessionId;
            
            // 异步处理对话
            CompletableFuture.runAsync(() -> {
                try {
                    // 调用 LangChain4j AI 服务
                    // LangChain4j 会自动：
                    // 1. 管理会话历史
                    // 2. 调用 AI 模型
                    // 3. 执行工具（如需要）
                    // 4. 返回流式响应
                    tourismAssistant.chat(memoryId, message)
                            .onNext(token -> {
                                // 收到 AI 返回的每个 token
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("content")
                                            .data(token));
                                } catch (IOException e) {
                                    log.error("发送token失败", e);
                                    throw new RuntimeException(e);
                                }
                            })
                            .onComplete(response -> {
                                // AI 回复完成
                                log.info("✅ [LangChain4j] 对话完成: sessionId={}", sessionId);
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("complete")
                                            .data(""));
                                    emitter.complete();
                                } catch (IOException e) {
                                    log.error("发送完成事件失败", e);
                                    emitter.completeWithError(e);
                                }
                            })
                            .onError(error -> {
                                // 发生错误
                                log.error("❌ [LangChain4j] 对话失败: sessionId={}", sessionId, error);
                                sendError(emitter, "AI 服务错误：" + error.getMessage());
                            })
                            .start();
                    
                } catch (Exception e) {
                    log.error("❌ [LangChain4j] 处理对话异常: sessionId={}", sessionId, e);
                    sendError(emitter, "处理失败：" + getSimpleErrorMessage(e));
                }
            });
            
        } catch (Exception e) {
            log.error("❌ [LangChain4j] 初始化对话失败: sessionId={}", sessionId, e);
            sendError(emitter, "服务初始化失败，请重试");
        }
    }
    
    /**
     * 清除会话
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    public void clearSession(String sessionId, Long userId) {
        try {
            String memoryId = userId + ":" + sessionId;
            // TODO: 实现清除会话逻辑
            // LangChain4j 的 ChatMemoryStore 可以删除会话
            log.info("🗑️ [LangChain4j] 清除会话: memoryId={}", memoryId);
        } catch (Exception e) {
            log.error("❌ 清除会话失败", e);
        }
    }
    
    /**
     * 获取欢迎消息
     */
    public String getWelcomeMessage() {
        return promptManager.getWelcomeMessage();
    }
    
    /**
     * 发送错误消息
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMessage));
            emitter.complete();
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", errorMessage, e);
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 获取简化的错误消息
     */
    private String getSimpleErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return e.getClass().getSimpleName();
        }
        
        // 限制错误消息长度
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        
        return message;
    }
}

