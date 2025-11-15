package com.jingdezhen.tourism.controller;

import com.jingdezhen.tourism.agent.core.ConversationContext;
import com.jingdezhen.tourism.service.AgentService;
import com.jingdezhen.tourism.utils.TokenUtil;
import com.jingdezhen.tourism.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Agent控制器
 * 提供Agent相关的API接口
 * 
 * @author AI Assistant
 */
@Slf4j
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
@Validated
public class AgentController {
    
    private final AgentService agentService;
    private final TokenUtil tokenUtil;
    
    /**
     * Agent流式对话
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @Valid @RequestBody AgentChatRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        log.info("🤖 用户{}开始Agent对话: sessionId={}, message={}", 
            userId, request.getSessionId(), request.getMessage());
        
        // 创建SSE发射器，设置超时时间为3分钟
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        
        // 设置回调
        emitter.onCompletion(() -> {
            log.info("✅ SSE连接正常完成: sessionId={}", request.getSessionId());
        });
        
        emitter.onTimeout(() -> {
            log.warn("⏰ SSE连接超时: sessionId={}", request.getSessionId());
            emitter.complete();
        });
        
        emitter.onError((ex) -> {
            log.error("❌ SSE连接错误: sessionId={}", request.getSessionId(), ex);
        });
        
        // 开始处理
        agentService.chatStream(
            request.getSessionId(), 
            userId, 
            request.getMessage(), 
            emitter);
        
        return emitter;
    }
    
    /**
     * 获取会话历史
     */
    @GetMapping("/session/{sessionId}")
    public Result<ConversationContext> getSession(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        ConversationContext context = agentService.getSession(sessionId, userId);
        
        if (context == null) {
            return Result.error("会话不存在或已过期");
        }
        
        return Result.success(context);
    }
    
    /**
     * 清除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        agentService.clearSession(sessionId, userId);
        
        log.info("🗑️ 用户{}清除会话: sessionId={}", userId, sessionId);
        
        return Result.success();
    }
    
    /**
     * 获取可用工具列表
     */
    @GetMapping("/tools")
    public Result<List<Map<String, Object>>> getTools() {
        List<Map<String, Object>> tools = agentService.getAvailableTools();
        return Result.success(tools);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            return tokenUtil.getUserIdFromAuth(authHeader);
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            throw new RuntimeException("用户未登录或登录已过期");
        }
    }
    
    /**
     * Agent聊天请求DTO
     */
    @Data
    public static class AgentChatRequest {
        @NotBlank(message = "会话ID不能为空")
        private String sessionId;
        
        @NotBlank(message = "消息内容不能为空")
        private String message;
    }
}

