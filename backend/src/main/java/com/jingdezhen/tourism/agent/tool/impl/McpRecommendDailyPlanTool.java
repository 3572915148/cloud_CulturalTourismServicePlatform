package com.jingdezhen.tourism.agent.tool.impl;

import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.mcp.tool.impl.RecommendDailyPlanTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP每日行程推荐工具的Agent包装器
 * 让AI Agent能够调用MCP Server的行程规划功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpRecommendDailyPlanTool implements AgentTool {
    
    private final RecommendDailyPlanTool recommendDailyPlanTool;
    
    @Override
    public String getName() {
        return "mcp_recommend_daily_plan";
    }
    
    @Override
    public String getDescription() {
        return "根据用户的旅游天数、预算、兴趣偏好等条件，智能生成详细的每日旅游行程计划。包括景点安排、餐饮推荐、住宿建议、时间规划等。当用户询问行程规划、旅游计划、几天几夜游时使用此工具。";
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
                "budget": {
                  "type": "number",
                  "description": "总预算（元），可选"
                },
                "interests": {
                  "type": "string",
                  "description": "兴趣偏好，多个用逗号分隔，例如：陶瓷文化,历史古迹,自然风光"
                },
                "pace": {
                  "type": "string",
                  "description": "旅游节奏",
                  "enum": ["relaxed", "moderate", "intense"],
                  "default": "moderate"
                },
                "startDate": {
                  "type": "string",
                  "description": "开始日期，格式：YYYY-MM-DD"
                }
              },
              "required": ["days"]
            }
            """;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("AI调用MCP行程推荐工具，用户ID: {}, 参数: {}", userId, parameters);
            
            // 调用MCP工具
            Object result = recommendDailyPlanTool.execute(parameters).get();
            
            log.info("MCP行程推荐完成");
            return ToolResult.success(result);
            
        } catch (Exception e) {
            log.error("MCP行程推荐工具执行失败", e);
            return ToolResult.error("行程规划失败: " + e.getMessage());
        }
    }
}

