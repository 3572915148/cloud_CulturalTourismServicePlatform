package com.jingdezhen.tourism.agent.tool;

import java.util.Map;

/**
 * Agent工具接口
 * 所有Agent工具都必须实现此接口
 * 
 * @author AI Assistant
 */
public interface AgentTool {
    
    /**
     * 获取工具名称（唯一标识）
     * 例如：search_products, create_order
     * 
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述（给AI看的）
     * 描述越清晰，AI越容易正确使用这个工具
     * 
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取参数定义（JSON Schema格式）
     * 定义工具需要哪些参数，参数类型等
     * 
     * @return JSON Schema字符串
     */
    String getParametersSchema();
    
    /**
     * 执行工具
     * 
     * @param parameters 参数Map，key为参数名，value为参数值
     * @param userId 当前用户ID
     * @return 执行结果
     */
    ToolResult execute(Map<String, Object> parameters, Long userId);
    
    /**
     * 获取工具分类
     * 用于对工具进行分组管理
     * 
     * @return 工具分类，默认为"general"
     */
    default String getCategory() {
        return "general";
    }
}

