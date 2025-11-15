package com.jingdezhen.tourism.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP协议响应模型
 * 遵循JSON-RPC 2.0规范
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {
    
    /**
     * JSON-RPC版本，固定为"2.0"
     */
    @JsonProperty("jsonrpc")
    @Builder.Default
    private String jsonrpc = "2.0";
    
    /**
     * 请求ID，与请求的ID对应
     */
    private String id;
    
    /**
     * 成功时的结果
     */
    private Object result;
    
    /**
     * 错误时的错误信息
     */
    private McpError error;
    
    /**
     * 创建成功响应
     */
    public static McpResponse success(String id, Object result) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }
    
    /**
     * 创建错误响应
     */
    public static McpResponse error(String id, int code, String message, Object data) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(new McpError(code, message, data))
                .build();
    }
    
    /**
     * 创建错误响应（无附加数据）
     */
    public static McpResponse error(String id, int code, String message) {
        return error(id, code, message, null);
    }
    
    /**
     * MCP错误模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class McpError {
        /**
         * 错误代码
         */
        private int code;
        
        /**
         * 错误消息
         */
        private String message;
        
        /**
         * 附加数据
         */
        private Object data;
    }
}

