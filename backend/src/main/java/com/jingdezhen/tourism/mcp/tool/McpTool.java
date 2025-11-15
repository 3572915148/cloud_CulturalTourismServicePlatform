package com.jingdezhen.tourism.mcp.tool;

import com.jingdezhen.tourism.mcp.model.ToolDefinition;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MCP工具接口
 * 所有MCP工具都需要实现此接口
 */
public interface McpTool {
    
    /**
     * 获取工具名称
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取工具定义
     * @return 工具定义
     */
    ToolDefinition getDefinition();
    
    /**
     * 执行工具
     * @param params 工具参数
     * @return 执行结果
     */
    CompletableFuture<Object> execute(Map<String, Object> params);
    
    /**
     * 验证参数
     * @param params 工具参数
     * @throws IllegalArgumentException 参数验证失败时抛出
     */
    default void validateParams(Map<String, Object> params) {
        // 默认不做验证，子类可以重写
    }
}

