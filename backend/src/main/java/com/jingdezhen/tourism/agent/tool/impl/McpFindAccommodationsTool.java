package com.jingdezhen.tourism.agent.tool.impl;

import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.mcp.tool.impl.FindAccommodationsTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP住宿推荐工具的Agent包装器
 * 让AI Agent能够调用MCP Server的住宿搜索功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpFindAccommodationsTool implements AgentTool {
    
    private final FindAccommodationsTool findAccommodationsTool;
    
    @Override
    public String getName() {
        return "mcp_find_accommodations";
    }
    
    @Override
    public String getDescription() {
        return "根据位置、价格、评分、设施等条件搜索并推荐合适的酒店住宿。支持入住退房日期计算、酒店类型筛选、设施要求等。当用户询问酒店、住宿、民宿时使用此工具。";
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
                "region": {
                  "type": "string",
                  "description": "所在区域，例如：昌江区、珠山区"
                },
                "checkInDate": {
                  "type": "string",
                  "description": "入住日期，格式：YYYY-MM-DD"
                },
                "checkOutDate": {
                  "type": "string",
                  "description": "退房日期，格式：YYYY-MM-DD"
                },
                "minPrice": {
                  "type": "number",
                  "description": "最低价格（元/晚）"
                },
                "maxPrice": {
                  "type": "number",
                  "description": "最高价格（元/晚）"
                },
                "minRating": {
                  "type": "number",
                  "description": "最低评分（1-5分）"
                },
                "facilities": {
                  "type": "string",
                  "description": "设施要求，多个用逗号分隔，例如：免费WiFi,停车场,早餐"
                },
                "hotelType": {
                  "type": "string",
                  "description": "酒店类型",
                  "enum": ["经济型", "舒适型", "高档型", "豪华型", "民宿", "客栈"]
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
            log.info("AI调用MCP住宿搜索工具，用户ID: {}, 参数: {}", userId, parameters);
            
            // 调用MCP工具
            Object result = findAccommodationsTool.execute(parameters).get();
            
            log.info("MCP住宿搜索完成");
            return ToolResult.success(result);
            
        } catch (Exception e) {
            log.error("MCP住宿搜索工具执行失败", e);
            return ToolResult.error("住宿搜索失败: " + e.getMessage());
        }
    }
}

