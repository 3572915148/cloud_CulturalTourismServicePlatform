package com.jingdezhen.tourism.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果封装
 * 
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    
    /**
     * 是否执行成功
     */
    private boolean success;
    
    /**
     * 返回的数据
     */
    private Object data;
    
    /**
     * 消息（成功或失败的描述）
     */
    private String message;
    
    /**
     * 错误码（失败时使用）
     */
    private String errorCode;
    
    /**
     * 创建成功结果
     * 
     * @param data 返回数据
     * @return ToolResult
     */
    public static ToolResult success(Object data) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * 创建成功结果（带消息）
     * 
     * @param data 返回数据
     * @param message 成功消息
     * @return ToolResult
     */
    public static ToolResult success(Object data, String message) {
        return ToolResult.builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return ToolResult
     */
    public static ToolResult error(String message) {
        return ToolResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 创建失败结果（带错误码）
     * 
     * @param message 失败消息
     * @param errorCode 错误码
     * @return ToolResult
     */
    public static ToolResult error(String message, String errorCode) {
        return ToolResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}

