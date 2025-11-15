package com.jingdezhen.tourism.agent.tool.impl;

import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.mcp.tool.impl.SearchAttractionsTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP景点搜索工具的Agent包装器
 * 让AI Agent能够调用MCP Server的景点搜索功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpSearchAttractionsTool implements AgentTool {
    
    private final SearchAttractionsTool searchAttractionsTool;
    
    @Override
    public String getName() {
        return "mcp_search_attractions";
    }
    
    @Override
    public String getDescription() {
        return "搜索景德镇的景点信息。支持按关键词、区域、价格范围、评分等条件搜索景点，返回景点列表包括名称、价格、评分、地址、图片等详细信息。当用户询问景点、旅游景区、参观地点时使用此工具。";
    }
    
    @Override
    public String getCategory() {
        return "mcp_tools";
    }
    
    @Override
    public String getParametersSchema() {
        return """
            {
              "type": "object",
              "properties": {
                "keyword": {
                  "type": "string",
                  "description": "搜索关键词，例如：陶瓷、古窑、博物馆等"
                },
                "region": {
                  "type": "string",
                  "description": "所在区域，例如：昌江区、珠山区、浮梁县等"
                },
                "minPrice": {
                  "type": "number",
                  "description": "最低价格（元）"
                },
                "maxPrice": {
                  "type": "number",
                  "description": "最高价格（元）"
                },
                "minRating": {
                  "type": "number",
                  "description": "最低评分（1-5分）"
                },
                "sortBy": {
                  "type": "string",
                  "description": "排序方式",
                  "enum": ["price_asc", "price_desc", "rating_desc", "default"]
                },
                "page": {
                  "type": "integer",
                  "description": "页码，默认1"
                },
                "pageSize": {
                  "type": "integer",
                  "description": "每页数量，默认10"
                }
              }
            }
            """;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("AI调用MCP景点搜索工具，用户ID: {}, 参数: {}", userId, parameters);
            
            // 调用MCP工具
            Object result = searchAttractionsTool.execute(parameters).get();
            
            log.info("MCP景点搜索完成");
            return ToolResult.success(result);
            
        } catch (Exception e) {
            log.error("MCP景点搜索工具执行失败", e);
            return ToolResult.error("景点搜索失败: " + e.getMessage());
        }
    }
}

