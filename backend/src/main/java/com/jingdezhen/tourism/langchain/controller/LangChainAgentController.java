package com.jingdezhen.tourism.langchain.controller;

import com.jingdezhen.tourism.langchain.service.LangChainAgentService;
import com.jingdezhen.tourism.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * LangChain4j Agent 控制器
 * 提供新的 API 端点，与原有 Agent 接口并行运行
 * 
 * @author AI Assistant
 */
@Slf4j
@RestController
@RequestMapping("/langchain/agent")
@RequiredArgsConstructor
public class LangChainAgentController {
    
    private final LangChainAgentService langChainAgentService;
    private final JwtUtil jwtUtil;
    
    /**
     * 流式对话接口
     * 
     * @param request 对话请求
     * @param token JWT Token
     * @return SSE流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        String sessionId = request.get("sessionId");
        String message = request.get("message");
        
        log.info("📨 [LangChain4j] 收到对话请求: sessionId={}, message={}", sessionId, message);
        
        // 解析用户ID
        Long userId = parseUserId(token);
        
        // 创建 SSE 发射器
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        setupEmitterCallbacks(emitter, sessionId);
        
        // 调用服务处理对话
        langChainAgentService.chatStream(sessionId, userId, message, emitter);
        
        return emitter;
    }
    
    /**
     * 清除会话
     * 
     * @param sessionId 会话ID
     * @param token JWT Token
     * @return 响应
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> clearSession(
            @PathVariable String sessionId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        log.info("🗑️ [LangChain4j] 清除会话: sessionId={}", sessionId);
        
        Long userId = parseUserId(token);
        
        try {
            langChainAgentService.clearSession(sessionId, userId);
            return Map.of(
                    "success", true,
                    "message", "会话已清除"
            );
        } catch (Exception e) {
            log.error("清除会话失败", e);
            return Map.of(
                    "success", false,
                    "message", "清除失败：" + e.getMessage()
            );
        }
    }
    
    /**
     * 获取欢迎消息
     * 
     * @return 欢迎消息
     */
    @GetMapping("/welcome")
    public Map<String, Object> getWelcome() {
        String welcomeMessage = langChainAgentService.getWelcomeMessage();
        return Map.of(
                "success", true,
                "message", welcomeMessage
        );
    }
    
    /**
     * 健康检查
     * 
     * @return 响应
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "LangChain4j Agent Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("features", Map.of(
                "promptManagement", true,
                "configBasedPrompts", true,
                "streamingSupport", true
        ));
        return response;
    }
    
    /**
     * 设置SSE发射器的回调
     */
    private void setupEmitterCallbacks(SseEmitter emitter, String sessionId) {
        emitter.onTimeout(() -> {
            log.warn("⏰ SSE 连接超时: sessionId={}", sessionId);
            emitter.complete();
        });
        
        emitter.onError(error -> {
            log.error("❌ SSE 连接错误: sessionId={}", sessionId, error);
            emitter.completeWithError(error);
        });
        
        emitter.onCompletion(() -> {
            log.info("✅ SSE 连接完成: sessionId={}", sessionId);
        });
    }
    
    /**
     * 解析用户ID
     */
    private Long parseUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                return jwtUtil.getUserIdFromToken(jwt);
            } catch (Exception e) {
                log.warn("解析token失败，使用默认用户ID", e);
            }
        }
        // 默认用户ID（用于测试）
        return 1L;
    }
}

