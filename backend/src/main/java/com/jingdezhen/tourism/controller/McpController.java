package com.jingdezhen.tourism.controller;

import com.jingdezhen.tourism.mcp.model.McpRequest;
import com.jingdezhen.tourism.mcp.model.McpResponse;
import com.jingdezhen.tourism.mcp.server.McpServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MCP服务器HTTP接口
 * 提供RESTful API供客户端调用
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {
    
    private final McpServer mcpServer;
    
    /**
     * 处理MCP请求
     * 
     * @param request MCP请求
     * @return MCP响应
     */
    @PostMapping(
        value = "/message",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<McpResponse>> handleMessage(
            @RequestBody McpRequest request) {
        
        log.info("收到MCP请求: method={}, id={}", request.getMethod(), request.getId());
        
        return mcpServer.handleRequest(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> {
                    log.error("处理MCP请求失败", e);
                    McpResponse errorResponse = McpResponse.error(
                            request.getId(),
                            -32603,
                            "内部错误: " + e.getMessage()
                    );
                    return ResponseEntity.status(500).body(errorResponse);
                });
    }
    
    /**
     * 获取服务器信息
     * 
     * @return 服务器信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServerInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Jingdezhen Tourism MCP Server");
        info.put("version", "1.0.0");
        info.put("protocolVersion", "2024-11-05");
        info.put("description", "景德镇文化旅游服务平台 MCP Server");
        info.put("tools", mcpServer.getTools().keySet());
        info.put("toolCount", mcpServer.getTools().size());
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("toolsRegistered", mcpServer.getTools().size());
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * 列出所有可用工具
     * 
     * @return 工具列表
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> listTools() {
        Map<String, Object> result = new HashMap<>();
        
        mcpServer.getTools().forEach((name, tool) -> {
            Map<String, Object> toolInfo = new HashMap<>();
            toolInfo.put("name", tool.getName());
            toolInfo.put("description", tool.getDescription());
            toolInfo.put("definition", tool.getDefinition());
            result.put(name, toolInfo);
        });
        
        return ResponseEntity.ok(result);
    }
}

