package com.jingdezhen.tourism.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP工具定义模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolDefinition {
    
    /**
     * 工具名称
     */
    private String name;
    
    /**
     * 工具描述
     */
    private String description;
    
    /**
     * 输入参数模式（JSON Schema格式）
     */
    private InputSchema inputSchema;
    
    /**
     * 参数模式定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputSchema {
        /**
         * 类型，通常为"object"
         */
        private String type;
        
        /**
         * 属性定义
         */
        private Map<String, PropertySchema> properties;
        
        /**
         * 必填字段列表
         */
        private List<String> required;
    }
    
    /**
     * 属性模式定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PropertySchema {
        /**
         * 属性类型
         */
        private String type;
        
        /**
         * 属性描述
         */
        private String description;
        
        /**
         * 枚举值（如果有）
         */
        private List<String> enumValues;
        
        /**
         * 默认值
         */
        private Object defaultValue;
        
        /**
         * 最小值（数字类型）
         */
        private Number minimum;
        
        /**
         * 最大值（数字类型）
         */
        private Number maximum;
    }
}

