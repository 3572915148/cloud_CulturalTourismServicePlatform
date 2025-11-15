package com.jingdezhen.tourism.mcp.config;

import com.jingdezhen.tourism.mcp.server.McpServer;
import com.jingdezhen.tourism.mcp.tool.McpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * MCP服务器配置类
 * 负责初始化MCP服务器并注册所有工具
 */
@Slf4j
@Configuration
public class McpServerConfig {
    
    private final McpServer mcpServer;
    private final List<McpTool> mcpTools;
    
    public McpServerConfig(McpServer mcpServer, List<McpTool> mcpTools) {
        this.mcpServer = mcpServer;
        this.mcpTools = mcpTools;
    }
    
    /**
     * 初始化MCP服务器
     */
    @PostConstruct
    public void init() {
        log.info("=== 初始化MCP服务器 ===");
        
        // 注册所有工具
        mcpServer.registerTools(mcpTools);
        
        log.info("MCP服务器初始化完成，已注册{}个工具", mcpTools.size());
        mcpTools.forEach(tool -> 
            log.info("  - {}: {}", tool.getName(), tool.getDescription())
        );
    }
}

