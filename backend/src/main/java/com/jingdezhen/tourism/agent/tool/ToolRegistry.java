package com.jingdezhen.tourism.agent.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Agent工具注册器
 * 自动注册所有实现了AgentTool接口的Bean
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
public class ToolRegistry {
    
    /**
     * 工具存储Map，key为工具名称，value为工具实例
     */
    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，自动注册所有工具
     * Spring会自动注入所有实现了AgentTool接口的Bean
     * 
     * @param toolList Spring自动注入的工具列表
     */
    public ToolRegistry(List<AgentTool> toolList) {
        log.info("=".repeat(60));
        log.info("🚀 开始注册Agent工具...");
        log.info("=".repeat(60));
        
        toolList.forEach(tool -> {
            tools.put(tool.getName(), tool);
            log.info("✅ 注册工具: {} - {}", tool.getName(), tool.getDescription());
        });
        
        log.info("=".repeat(60));
        log.info("📦 工具注册完成，共注册 {} 个工具", tools.size());
        log.info("=".repeat(60));
    }
    
    /**
     * 根据名称获取工具
     * 
     * @param name 工具名称
     * @return 工具实例，不存在返回null
     */
    public AgentTool getTool(String name) {
        return tools.get(name);
    }
    
    /**
     * 获取所有工具
     * 
     * @return 工具列表
     */
    public List<AgentTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * 检查工具是否存在
     * 
     * @param name 工具名称
     * @return 是否存在
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
    
    /**
     * 获取工具数量
     * 
     * @return 工具数量
     */
    public int getToolCount() {
        return tools.size();
    }
    
    /**
     * 按分类获取工具
     * 
     * @param category 分类名称
     * @return 该分类下的工具列表
     */
    public List<AgentTool> getToolsByCategory(String category) {
        return tools.values().stream()
                .filter(tool -> category.equals(tool.getCategory()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取工具定义（用于DeepSeek Function Calling）
     * 转换为DeepSeek API需要的格式
     * 
     * @return 工具定义列表
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return tools.values().stream()
                .map(tool -> {
                    Map<String, Object> definition = new HashMap<>();
                    definition.put("type", "function");
                    
                    Map<String, Object> function = new HashMap<>();
                    function.put("name", tool.getName());
                    function.put("description", tool.getDescription());
                    function.put("parameters", parseSchema(tool.getParametersSchema()));
                    
                    definition.put("function", function);
                    return definition;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取工具描述文本（用于System Prompt）
     * 
     * @return 工具描述文本
     */
    public String getToolsDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("可用工具列表：\n");
        
        tools.values().forEach(tool -> {
            sb.append(String.format("- %s: %s\n", tool.getName(), tool.getDescription()));
        });
        
        return sb.toString();
    }
    
    /**
     * 解析JSON Schema字符串为Map
     * 
     * @param schema JSON Schema字符串
     * @return Map对象
     */
    private Map<String, Object> parseSchema(String schema) {
        try {
            return JSON.parseObject(schema, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析工具参数Schema失败: {}", schema, e);
            // 返回一个空的Schema
            Map<String, Object> emptySchema = new HashMap<>();
            emptySchema.put("type", "object");
            emptySchema.put("properties", new HashMap<>());
            return emptySchema;
        }
    }
}

