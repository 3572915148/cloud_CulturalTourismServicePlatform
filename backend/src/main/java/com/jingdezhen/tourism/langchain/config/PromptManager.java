package com.jingdezhen.tourism.langchain.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词管理器
 * 从 prompts.yml 配置文件加载和管理所有提示词
 * 支持动态更新、多场景切换、模板变量替换
 * 
 * 使用示例：
 * String prompt = promptManager.getSystemPrompt("default");
 * String response = promptManager.getResponse("success.product_found", Map.of("count", 5));
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@Configuration
@ConfigurationProperties(prefix = "prompts")
public class PromptManager {
    
    @Getter
    private Map<String, Object> system = new HashMap<>();
    
    @Getter
    private Map<String, Object> user = new HashMap<>();
    
    @Getter
    private Map<String, Object> tools = new HashMap<>();
    
    @Getter
    private Map<String, Object> responses = new HashMap<>();
    
    /**
     * 初始化后的回调
     */
    @PostConstruct
    public void init() {
        log.info("📋 提示词管理器初始化完成");
        log.info("   - 系统提示词: {} 个", system.size());
        log.info("   - 用户提示词: {} 个", user.size());
        log.info("   - 工具提示词: {} 个", tools.size());
        log.info("   - 响应模板: {} 个", responses.size());
    }
    
    // ==================== 系统提示词 ====================
    
    /**
     * 获取系统提示词
     * 
     * @param type 提示词类型（default, recommendation, order）
     * @return 系统提示词内容
     */
    public String getSystemPrompt(String type) {
        try {
            Object prompt = system.get(type);
            if (prompt != null) {
                return prompt.toString().trim();
            }
            log.warn("未找到系统提示词类型: {}，使用默认提示词", type);
            return getDefaultSystemPrompt();
        } catch (Exception e) {
            log.error("获取系统提示词失败: {}", type, e);
            return getDefaultSystemPrompt();
        }
    }
    
    /**
     * 获取默认系统提示词
     */
    public String getDefaultSystemPrompt() {
        Object defaultPrompt = system.get("default");
        if (defaultPrompt != null) {
            return defaultPrompt.toString().trim();
        }
        return "你是一个专业的AI助手。";
    }
    
    /**
     * 获取推荐场景的系统提示词
     */
    public String getRecommendationPrompt() {
        return getSystemPrompt("recommendation");
    }
    
    /**
     * 获取订单场景的系统提示词
     */
    public String getOrderPrompt() {
        return getSystemPrompt("order");
    }
    
    // ==================== 用户提示词 ====================
    
    /**
     * 获取欢迎语
     */
    public String getWelcomeMessage() {
        try {
            Object welcome = user.get("welcome");
            if (welcome != null) {
                return welcome.toString();
            }
        } catch (Exception e) {
            log.error("获取欢迎语失败", e);
        }
        return "你好！有什么可以帮您的？";
    }
    
    /**
     * 获取快捷问题列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getQuickQuestions() {
        try {
            Object questions = user.get("quick_questions");
            if (questions instanceof List) {
                return (List<String>) questions;
            }
        } catch (Exception e) {
            log.error("获取快捷问题失败", e);
        }
        return List.of();
    }
    
    // ==================== 工具提示词 ====================
    
    /**
     * 获取工具描述
     * 
     * @param toolName 工具名称
     * @return 工具描述信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getToolInfo(String toolName) {
        try {
            Object tool = tools.get(toolName);
            if (tool instanceof Map) {
                return (Map<String, String>) tool;
            }
        } catch (Exception e) {
            log.error("获取工具信息失败: {}", toolName, e);
        }
        return Map.of(
            "description", "工具：" + toolName,
            "usage", "根据需要使用"
        );
    }
    
    /**
     * 获取工具描述
     */
    public String getToolDescription(String toolName) {
        return getToolInfo(toolName).getOrDefault("description", "");
    }
    
    /**
     * 获取工具使用场景
     */
    public String getToolUsage(String toolName) {
        return getToolInfo(toolName).getOrDefault("usage", "");
    }
    
    // ==================== 响应模板 ====================
    
    /**
     * 获取响应模板并替换变量
     * 
     * @param path 模板路径（如 "success.product_found"）
     * @param variables 变量Map（如 {"count": 5, "category": "景点"}）
     * @return 替换后的响应文本
     */
    @SuppressWarnings("unchecked")
    public String getResponse(String path, Map<String, Object> variables) {
        try {
            String template = getResponseTemplate(path);
            if (template == null || template.isEmpty()) {
                return "";
            }
            
            // 替换变量
            return replaceVariables(template, variables);
            
        } catch (Exception e) {
            log.error("获取响应模板失败: {}", path, e);
            return "";
        }
    }
    
    /**
     * 获取响应模板（不替换变量）
     * 
     * @param path 模板路径（如 "success.product_found"）
     * @return 模板文本
     */
    @SuppressWarnings("unchecked")
    public String getResponseTemplate(String path) {
        try {
            String[] parts = path.split("\\.");
            if (parts.length != 2) {
                log.warn("响应路径格式错误: {}", path);
                return "";
            }
            
            String category = parts[0];  // success, error, guidance
            String key = parts[1];        // product_found, etc.
            
            Object categoryMap = responses.get(category);
            if (categoryMap instanceof Map) {
                Object template = ((Map<String, Object>) categoryMap).get(key);
                if (template != null) {
                    return template.toString();
                }
            }
            
            log.warn("未找到响应模板: {}", path);
            return "";
            
        } catch (Exception e) {
            log.error("获取响应模板失败: {}", path, e);
            return "";
        }
    }
    
    /**
     * 获取成功响应
     */
    public String getSuccessResponse(String key, Map<String, Object> variables) {
        return getResponse("success." + key, variables);
    }
    
    /**
     * 获取错误响应
     */
    public String getErrorResponse(String key, Map<String, Object> variables) {
        return getResponse("error." + key, variables);
    }
    
    /**
     * 获取引导响应
     */
    public String getGuidanceResponse(String key, Map<String, Object> variables) {
        return getResponse("guidance." + key, variables);
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 替换模板中的变量
     * 支持 {varName} 格式
     * 
     * @param template 模板文本
     * @param variables 变量Map
     * @return 替换后的文本
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace("{" + key + "}", value);
        }
        
        return result;
    }
    
    /**
     * 重新加载提示词配置
     * 注意：需要配合配置中心使用才能实现热更新
     */
    public void reload() {
        log.info("🔄 重新加载提示词配置...");
        init();
    }
    
    /**
     * 验证提示词配置是否完整
     * 
     * @return 验证结果
     */
    public boolean validate() {
        boolean valid = true;
        
        // 检查必要的系统提示词
        if (!system.containsKey("default")) {
            log.error("❌ 缺少默认系统提示词");
            valid = false;
        }
        
        // 检查欢迎语
        if (!user.containsKey("welcome")) {
            log.warn("⚠️ 缺少欢迎语配置");
        }
        
        // 检查工具配置
        String[] requiredTools = {
            "searchProducts", 
            "getProductDetail", 
            "getProductCategories",
            "createOrder", 
            "smartRecommendation"
        };
        
        for (String tool : requiredTools) {
            if (!tools.containsKey(tool)) {
                log.warn("⚠️ 缺少工具配置: {}", tool);
            }
        }
        
        if (valid) {
            log.info("✅ 提示词配置验证通过");
        }
        
        return valid;
    }
}

