package com.jingdezhen.tourism.mcp.tool;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MCP工具抽象基类
 * 提供通用的工具实现逻辑
 */
@Slf4j
public abstract class AbstractMcpTool implements McpTool {
    
    @Override
    public CompletableFuture<Object> execute(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证参数
                validateParams(params);
                
                log.info("执行MCP工具: {}, 参数: {}", getName(), params);
                
                // 执行具体的工具逻辑
                Object result = doExecute(params);
                
                log.info("MCP工具执行成功: {}", getName());
                return result;
                
            } catch (Exception e) {
                log.error("MCP工具执行失败: {}, 错误: {}", getName(), e.getMessage(), e);
                throw new RuntimeException("工具执行失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 执行具体的工具逻辑
     * 子类需要实现此方法
     * 
     * @param params 工具参数
     * @return 执行结果
     */
    protected abstract Object doExecute(Map<String, Object> params);
    
    /**
     * 从参数中获取字符串值
     */
    protected String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        Object value = params.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 从参数中获取整数值
     */
    protected Integer getIntParam(Map<String, Object> params, String key, Integer defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 从参数中获取双精度浮点数值
     */
    protected Double getDoubleParam(Map<String, Object> params, String key, Double defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 从参数中获取布尔值
     */
    protected Boolean getBooleanParam(Map<String, Object> params, String key, Boolean defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}

