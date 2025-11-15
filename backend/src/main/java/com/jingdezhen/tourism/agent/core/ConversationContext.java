package com.jingdezhen.tourism.agent.core;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话上下文
 * 保存会话的所有信息，包括对话历史、上下文变量等
 * 
 * @author AI Assistant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    
    /**
     * 会话ID（唯一标识）
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 对话历史记录
     */
    private List<Message> history;
    
    /**
     * 上下文变量（用于存储临时数据）
     */
    private Map<String, Object> variables;
    
    /**
     * 会话创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * 消息对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：user, assistant, tool
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 消息时间
         */
        private LocalDateTime time;
        
        /**
         * 元数据（可选）
         */
        private Map<String, Object> metadata;
        
        /**
         * 创建用户消息
         */
        public static Message user(String content) {
            return new Message("user", content, LocalDateTime.now(), null);
        }
        
        /**
         * 创建助手消息
         */
        public static Message assistant(String content) {
            return new Message("assistant", content, LocalDateTime.now(), null);
        }
        
        /**
         * 创建工具消息
         */
        public static Message tool(String toolName, Object result) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("tool", toolName);
            return new Message("tool", JSON.toJSONString(result), LocalDateTime.now(), metadata);
        }
    }
    
    /**
     * 添加消息到历史
     */
    public void addMessage(Message message) {
        if (this.history == null) {
            this.history = new ArrayList<>();
        }
        this.history.add(message);
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * 设置上下文变量
     */
    public void setVariable(String key, Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(key, value);
    }
    
    /**
     * 获取上下文变量
     */
    public Object getVariable(String key) {
        return this.variables != null ? this.variables.get(key) : null;
    }
    
    /**
     * 获取最近N条消息
     */
    public List<Message> getRecentMessages(int count) {
        if (this.history == null || this.history.isEmpty()) {
            return new ArrayList<>();
        }
        int size = this.history.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(this.history.subList(fromIndex, size));
    }
}

