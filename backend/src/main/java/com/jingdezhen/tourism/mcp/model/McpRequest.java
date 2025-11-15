package com.jingdezhen.tourism.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * MCP协议请求模型
 * 遵循JSON-RPC 2.0规范
 */
@Data
public class McpRequest {
    
    /**
     * JSON-RPC版本，固定为"2.0"
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    /**
     * 请求ID，用于关联请求和响应
     */
    private String id;
    
    /**
     * 方法名称
     * 例如：tools/list, tools/call
     */
    private String method;
    
    /**
     * 方法参数
     */
    private Map<String, Object> params;
    
}

