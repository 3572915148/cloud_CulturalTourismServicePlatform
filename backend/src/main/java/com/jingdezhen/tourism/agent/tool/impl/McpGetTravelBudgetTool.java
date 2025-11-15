package com.jingdezhen.tourism.agent.tool.impl;

import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.mcp.tool.impl.GetTravelBudgetTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP旅行预算计算工具的Agent包装器
 * 让AI Agent能够调用MCP Server的预算计算功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpGetTravelBudgetTool implements AgentTool {
    
    private final GetTravelBudgetTool getTravelBudgetTool;
    
    @Override
    public String getName() {
        return "mcp_get_travel_budget";
    }
    
    @Override
    public String getDescription() {
        return "根据旅行天数、人数、住宿标准、用餐标准等参数，计算详细的旅行预算清单。包括交通、住宿、餐饮、门票、购物等各项开支，以及省钱建议和支出占比分析。当用户询问费用、预算、多少钱时使用此工具。";
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
                "days": {
                  "type": "integer",
                  "description": "旅游天数（必填），1-30天"
                },
                "people": {
                  "type": "integer",
                  "description": "旅游人数，默认1"
                },
                "accommodationLevel": {
                  "type": "string",
                  "description": "住宿标准",
                  "enum": ["budget", "standard", "comfort", "luxury"],
                  "default": "standard"
                },
                "mealLevel": {
                  "type": "string",
                  "description": "用餐标准",
                  "enum": ["budget", "standard", "premium"],
                  "default": "standard"
                },
                "includeTransport": {
                  "type": "boolean",
                  "description": "是否包含往返交通费用，默认true"
                },
                "transportType": {
                  "type": "string",
                  "description": "交通方式",
                  "enum": ["train", "plane", "car", "bus"],
                  "default": "train"
                },
                "departureCity": {
                  "type": "string",
                  "description": "出发城市"
                },
                "shoppingBudget": {
                  "type": "number",
                  "description": "购物预算（元/人），默认500"
                },
                "includeShopping": {
                  "type": "boolean",
                  "description": "是否包含购物预算，默认true"
                }
              },
              "required": ["days"]
            }
            """;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("AI调用MCP预算计算工具，用户ID: {}, 参数: {}", userId, parameters);
            
            // 调用MCP工具
            Object result = getTravelBudgetTool.execute(parameters).get();
            
            log.info("MCP预算计算完成");
            return ToolResult.success(result);
            
        } catch (Exception e) {
            log.error("MCP预算计算工具执行失败", e);
            return ToolResult.error("预算计算失败: " + e.getMessage());
        }
    }
}

