package com.jingdezhen.tourism.mcp.server;

import com.jingdezhen.tourism.mcp.model.McpRequest;
import com.jingdezhen.tourism.mcp.model.McpResponse;
import com.jingdezhen.tourism.mcp.model.ToolDefinition;
import com.jingdezhen.tourism.mcp.tool.McpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP服务器核心
 * 负责工具注册、请求路由和响应处理
 */
@Slf4j
@Component
public class McpServer {
    
    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();
    
    /**
     * 注册MCP工具
     * @param tool 工具实例
     */
    public void registerTool(McpTool tool) {
        String name = tool.getName();
        if (tools.containsKey(name)) {
            log.warn("工具已存在，将被覆盖: {}", name);
        }
        tools.put(name, tool);
        log.info("注册MCP工具: {} - {}", name, tool.getDescription());
    }
    
    /**
     * 注册多个工具
     * @param toolList 工具列表
     */
    public void registerTools(List<McpTool> toolList) {
        toolList.forEach(this::registerTool);
    }
    
    /**
     * 获取所有已注册的工具
     * @return 工具映射表
     */
    public Map<String, McpTool> getTools() {
        return Collections.unmodifiableMap(tools);
    }
    
    /**
     * 处理MCP请求
     * @param request MCP请求
     * @return MCP响应的Future
     */
    public CompletableFuture<McpResponse> handleRequest(McpRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String method = request.getMethod();
                String id = request.getId();
                
                log.info("处理MCP请求: method={}, id={}", method, id);
                
                switch (method) {
                    case "initialize":
                        return handleInitialize(id, request.getParams());
                        
                    case "tools/list":
                        return handleToolsList(id);
                        
                    case "tools/call":
                        return handleToolCall(id, request.getParams());
                        
                    default:
                        return McpResponse.error(id, -32601, 
                                "方法不存在: " + method);
                }
                
            } catch (Exception e) {
                log.error("处理MCP请求失败", e);
                return McpResponse.error(request.getId(), -32603, 
                        "内部错误: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理初始化请求
     */
    private McpResponse handleInitialize(String id, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("serverInfo", Map.of(
                "name", "Jingdezhen Tourism MCP Server",
                "version", "1.0.0"
        ));
        result.put("capabilities", Map.of(
                "tools", Map.of("listChanged", false)
        ));
        
        log.info("MCP服务器初始化成功");
        return McpResponse.success(id, result);
    }
    
    /**
     * 处理工具列表请求
     */
    private McpResponse handleToolsList(String id) {
        List<ToolDefinition> toolDefinitions = new ArrayList<>();
        
        for (McpTool tool : tools.values()) {
            toolDefinitions.add(tool.getDefinition());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("tools", toolDefinitions);
        
        log.info("返回工具列表，共{}个工具", toolDefinitions.size());
        return McpResponse.success(id, result);
    }
    
    /**
     * 处理工具调用请求
     */
    private McpResponse handleToolCall(String id, Map<String, Object> params) {
        try {
            // 获取工具名称和参数
            String toolName = (String) params.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (toolName == null) {
                return McpResponse.error(id, -32602, "缺少工具名称");
            }
            
            // 查找工具
            McpTool tool = tools.get(toolName);
            if (tool == null) {
                return McpResponse.error(id, -32602, 
                        "工具不存在: " + toolName);
            }
            
            // 执行工具
            log.info("调用工具: {} with params: {}", toolName, arguments);
            
            Object toolResult = tool.execute(arguments != null ? arguments : new HashMap<>())
                    .get(); // 同步等待结果
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("content", List.of(
                    Map.of(
                            "type", "text",
                            "text", formatToolResult(toolResult)
                    )
            ));
            
            return McpResponse.success(id, result);
            
        } catch (IllegalArgumentException e) {
            log.error("工具参数验证失败", e);
            return McpResponse.error(id, -32602, 
                    "参数验证失败: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("工具执行失败", e);
            return McpResponse.error(id, -32603, 
                    "工具执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 格式化工具执行结果
     */
    private String formatToolResult(Object result) {
        if (result == null) {
            return "执行成功，无返回数据";
        }
        
        // 如果结果是Map或List，转换为JSON字符串
        if (result instanceof Map || result instanceof List) {
            try {
                return com.alibaba.fastjson2.JSON.toJSONString(result, 
                        com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat);
            } catch (Exception e) {
                log.warn("格式化结果失败，返回toString", e);
                return result.toString();
            }
        }
        
        return result.toString();
    }
}

