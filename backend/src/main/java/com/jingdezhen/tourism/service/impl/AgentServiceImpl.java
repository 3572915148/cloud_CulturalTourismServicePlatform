package com.jingdezhen.tourism.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.jingdezhen.tourism.agent.core.ConversationContext;
import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolRegistry;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent服务实现类
 * 
 * @author AI Assistant
 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {
    
    private final ToolRegistry toolRegistry;
    
    /**
     * 构造函数，启动会话清理定时任务
     */
    public AgentServiceImpl(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        startSessionCleanupTask();
    }
    
    /**
     * 会话存储（MVP版本使用内存，生产环境建议使用Redis）
     */
    private final Map<String, ConversationContext> sessions = new ConcurrentHashMap<>();
    
    /**
     * 会话超时时间（30分钟）
     */
    private static final long SESSION_TIMEOUT_MILLIS = 30 * 60 * 1000L;
    
    @Value("${spring.ai.deepseek.api-key:}")
    private String apiKey;
    
    @Value("${spring.ai.deepseek.model:deepseek-chat}")
    private String model;
    
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    @Override
    public void chatStream(String sessionId, Long userId, String message, SseEmitter emitter) {
        try {
            // 参数验证
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sendError(emitter, "会话ID不能为空");
                return;
            }
            
            if (message == null || message.trim().isEmpty()) {
                sendError(emitter, "消息内容不能为空");
                return;
            }
            
        // 获取或创建会话
        ConversationContext context = sessions.computeIfAbsent(sessionId, 
            k -> createNewContext(sessionId, userId));
        
        // 验证用户ID
        if (!context.getUserId().equals(userId)) {
                sendError(emitter, "会话不属于当前用户");
            return;
        }
        
        // 添加用户消息到历史
        context.addMessage(ConversationContext.Message.user(message));
        
        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                processAgentChat(context, emitter);
                } catch (InterruptedException e) {
                    log.error("Agent处理被中断: sessionId={}", sessionId, e);
                    Thread.currentThread().interrupt();
                    sendError(emitter, "处理被中断，请重试");
                } catch (java.net.http.HttpTimeoutException e) {
                    log.error("AI服务超时: sessionId={}", sessionId, e);
                    sendError(emitter, "AI服务响应超时，请重试");
                } catch (java.io.IOException e) {
                    log.error("网络连接错误: sessionId={}", sessionId, e);
                    sendError(emitter, "网络连接错误，请检查网络后重试");
                } catch (Exception e) {
                    log.error("Agent处理失败: sessionId={}", sessionId, e);
                    sendError(emitter, "处理失败：" + getSimpleErrorMessage(e));
                }
            });
            } catch (Exception e) {
            log.error("chatStream初始化失败: sessionId={}", sessionId, e);
            sendError(emitter, "服务初始化失败，请重试");
        }
    }
    
    /**
     * 发送错误消息到前端并完成连接
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                .data(errorMessage));
            emitter.complete();
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", errorMessage, e);
                emitter.completeWithError(e);
            }
    }
    
    /**
     * 获取简化的错误消息
     */
    private String getSimpleErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return e.getClass().getSimpleName();
        }
        
        // 限制错误消息长度
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }
        
        return message;
    }
    
    /**
     * 处理Agent对话
     */
    private void processAgentChat(ConversationContext context, SseEmitter emitter) throws Exception {
        log.info("🤖 开始处理Agent对话: sessionId={}, userId={}", 
            context.getSessionId(), context.getUserId());
        
        // 检查API Key是否配置
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("❌ DeepSeek API Key未配置");
            emitter.send(SseEmitter.event()
                .name("error")
                .data("AI服务未配置，请联系管理员"));
            emitter.complete();
            return;
        }
        
        // 构建请求
        Map<String, Object> request = buildDeepSeekRequest(context);
        
        // 调用DeepSeek API
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(DEEPSEEK_API_URL))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request)))
            .build();
        
        log.info("📡 发送请求到DeepSeek API");
        
        // 处理流式响应
        HttpResponse<java.io.InputStream> response = client.send(httpRequest, 
            HttpResponse.BodyHandlers.ofInputStream());
        
        if (response.statusCode() != 200) {
            String errorBody = new String(response.body().readAllBytes());
            log.error("❌ DeepSeek API错误: status={}, body={}", response.statusCode(), errorBody);
            emitter.send(SseEmitter.event()
                .name("error")
                .data("AI服务错误，请稍后重试"));
            emitter.complete();
            return;
        }
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.body()));
        
        String line;
        StringBuilder assistantMessage = new StringBuilder();
        Map<Integer, ToolCallAccumulator> toolCallsMap = new HashMap<>();
        String finishReason = null;
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6).trim();
                
                if ("[DONE]".equals(data)) {
                    break;
                }
                
                try {
                    JSONObject json = JSON.parseObject(data);
                    JSONArray choices = json.getJSONArray("choices");
                    
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject delta = choice.getJSONObject("delta");
                        
                        // 获取finish_reason
                        if (choice.containsKey("finish_reason") && choice.get("finish_reason") != null) {
                            finishReason = choice.getString("finish_reason");
                        }
                        
                        if (delta == null) {
                            continue;
                        }
                        
                        // 处理文本内容
                        if (delta.containsKey("content")) {
                            String content = delta.getString("content");
                            if (content != null && !content.isEmpty()) {
                                assistantMessage.append(content);
                                
                                // 推送内容
                                emitter.send(SseEmitter.event()
                                    .name("content")
                                    .data(content));
                            }
                        }
                        
                        // 累积工具调用（流式传输中tool_calls是增量的）
                        if (delta.containsKey("tool_calls")) {
                            JSONArray toolCallsArray = delta.getJSONArray("tool_calls");
                            for (int i = 0; i < toolCallsArray.size(); i++) {
                                JSONObject toolCall = toolCallsArray.getJSONObject(i);
                                Integer index = toolCall.getInteger("index");
                                
                                if (index == null) {
                                    continue;
                                }
                                
                                ToolCallAccumulator accumulator = toolCallsMap.computeIfAbsent(
                                    index, k -> new ToolCallAccumulator());
                                
                                // 累积tool call的各个部分
                                JSONObject function = toolCall.getJSONObject("function");
                                if (function != null) {
                                    if (function.containsKey("name")) {
                                        accumulator.functionName = function.getString("name");
                                    }
                                    if (function.containsKey("arguments")) {
                                        accumulator.arguments.append(function.getString("arguments"));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析SSE数据失败: {}", data, e);
                }
            }
        }
        
        // 如果有工具调用，执行工具并继续对话
        if (!toolCallsMap.isEmpty() || "tool_calls".equals(finishReason)) {
            log.info("🔧 检测到工具调用请求，共{}个工具", toolCallsMap.size());
            
            // 如果有工具调用，第一次的回复可能是不完整的（如"我来帮您搜索..."），
            // 不保存到历史，等工具执行完成后再统一生成完整回复
            // 但如果有内容，先保存起来，后续可能会用到
            String preliminaryMessage = assistantMessage.length() > 0 ? assistantMessage.toString() : null;
            
            List<ToolResult> toolResults = executeTools(toolCallsMap, context, emitter);
            
            // 工具执行后，继续对话让AI根据工具结果生成回复
            // 将初步消息也传递给后续处理，让AI可以基于此继续回复
            continueConversationWithToolResults(context, toolResults, emitter, preliminaryMessage);
        } else {
            // 没有工具调用，保存AI的完整回复
        if (assistantMessage.length() > 0) {
            context.addMessage(ConversationContext.Message.assistant(
                assistantMessage.toString()));
        }
        
            log.info("✅ Agent对话处理完成");
        
        // 发送完成事件
        emitter.send(SseEmitter.event()
            .name("complete")
            .data(""));
        emitter.complete();
        }
    }
    
    /**
     * 工具调用累积器（用于处理流式响应中的增量tool_calls）
     */
    private static class ToolCallAccumulator {
        String functionName;
        StringBuilder arguments = new StringBuilder();
    }
    
    /**
     * 执行工具调用
     */
    private List<ToolResult> executeTools(Map<Integer, ToolCallAccumulator> toolCallsMap, 
                                          ConversationContext context, 
                                          SseEmitter emitter) throws Exception {
        
        List<ToolResult> results = new ArrayList<>();
        
        for (Map.Entry<Integer, ToolCallAccumulator> entry : toolCallsMap.entrySet()) {
            ToolCallAccumulator accumulator = entry.getValue();
            
            String toolName = accumulator.functionName;
            String argsStr = accumulator.arguments.toString();
            
            if (toolName == null || argsStr == null || argsStr.isEmpty()) {
                log.warn("⚠️ 工具调用信息不完整: name={}, args={}", toolName, argsStr);
                continue;
            }
            
            log.info("🔧 AI请求调用工具: {}, 参数: {}", toolName, argsStr);
            
            try {
                Map<String, Object> args = JSON.parseObject(argsStr, 
                    new TypeReference<Map<String, Object>>() {});
                
                // 通知前端正在调用工具
                Map<String, Object> toolCallInfo = new HashMap<>();
                toolCallInfo.put("tool", toolName);
                toolCallInfo.put("parameters", args);
                
                emitter.send(SseEmitter.event()
                    .name("tool_call")
                    .data(JSON.toJSONString(toolCallInfo)));
                
                // 执行工具
                AgentTool tool = toolRegistry.getTool(toolName);
                if (tool != null) {
                    ToolResult result = tool.execute(args, context.getUserId());
                    results.add(result);
                    
                    // 通知前端工具执行结果
                    // 注意：只发送产品相关的工具结果，分类工具结果不发送（避免前端误判为产品）
                    // 分类信息只用于AI内部决策，不需要发送给前端
                    if (!"get_product_categories".equals(toolName)) {
                        emitter.send(SseEmitter.event()
                            .name("tool_result")
                            .data(JSON.toJSONString(result)));
                    } else {
                        log.info("跳过发送分类工具结果到前端（分类不是产品数据）");
                    }
                    
                    log.info("✅ 工具执行成功: {}", toolName);
                } else {
                    log.error("⚠️ 工具不存在: {}", toolName);
                    ToolResult errorResult = ToolResult.error("工具不存在：" + toolName, "TOOL_NOT_FOUND");
                    results.add(errorResult);
                    
                    emitter.send(SseEmitter.event()
                        .name("tool_result")
                        .data(JSON.toJSONString(errorResult)));
                }
            } catch (Exception e) {
                log.error("❌ 工具执行失败: {}", toolName, e);
                ToolResult errorResult = ToolResult.error("工具执行失败：" + e.getMessage(), "TOOL_EXECUTION_ERROR");
                results.add(errorResult);
                
                emitter.send(SseEmitter.event()
                    .name("tool_result")
                    .data(JSON.toJSONString(errorResult)));
            }
        }
        
        return results;
    }
    
    /**
     * 工具执行后，继续对话让AI根据工具结果生成回复
     */
    private void continueConversationWithToolResults(ConversationContext context, 
                                                     List<ToolResult> toolResults, 
                                                     SseEmitter emitter,
                                                     String preliminaryMessage) throws Exception {
        
        // 将工具结果添加到对话历史中（只包含JSON数据，不包含提示词）
        // 提示词已经在系统提示词中说明，不需要在这里重复
        StringBuilder toolResultsText = new StringBuilder();
        for (ToolResult result : toolResults) {
            toolResultsText.append(formatToolResultForAI(result));
            toolResultsText.append("\n");
        }
        
        // 如果有初步消息（如"我来帮您搜索..."），将其与工具结果合并
        // 这样AI可以基于初步消息继续回复，而不是重新开始
        if (preliminaryMessage != null && !preliminaryMessage.trim().isEmpty()) {
            // 将初步消息和工具结果合并，让AI基于此继续回复
            String combinedMessage = preliminaryMessage + toolResultsText.toString();
            context.addMessage(ConversationContext.Message.assistant(combinedMessage));
        } else {
            // 添加工具结果作为assistant消息（DeepSeek API不支持tool角色，所以使用assistant）
            context.addMessage(ConversationContext.Message.assistant(toolResultsText.toString()));
        }
        
        // 再次调用AI，让它根据工具结果生成自然语言回复
        Map<String, Object> request = buildDeepSeekRequest(context);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(DEEPSEEK_API_URL))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request)))
            .build();
        
        log.info("📡 发送工具结果，请求AI生成回复");
        
        HttpResponse<java.io.InputStream> response = client.send(httpRequest, 
            HttpResponse.BodyHandlers.ofInputStream());
        
        if (response.statusCode() != 200) {
            String errorBody = new String(response.body().readAllBytes());
            log.error("❌ DeepSeek API错误: status={}, body={}", response.statusCode(), errorBody);
            emitter.send(SseEmitter.event()
                .name("error")
                .data("AI服务错误，请稍后重试"));
            emitter.complete();
            return;
        }
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.body()));
        
        String line;
        StringBuilder finalResponse = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6).trim();
                
                if ("[DONE]".equals(data)) {
                    break;
                }
                
                try {
                    JSONObject json = JSON.parseObject(data);
                    JSONArray choices = json.getJSONArray("choices");
                    
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject delta = choice.getJSONObject("delta");
                        
                        if (delta != null && delta.containsKey("content")) {
                            String content = delta.getString("content");
                            if (content != null && !content.isEmpty()) {
                                finalResponse.append(content);
                                
                                // 推送内容
                                emitter.send(SseEmitter.event()
                                    .name("content")
                                    .data(content));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析SSE数据失败: {}", data, e);
                }
            }
        }
        
        // 保存AI的最终回复
        if (finalResponse.length() > 0) {
            context.addMessage(ConversationContext.Message.assistant(finalResponse.toString()));
        }
        
        log.info("✅ Agent对话处理完成（含工具调用）");
        
        // 发送完成事件
        emitter.send(SseEmitter.event()
            .name("complete")
            .data(""));
        emitter.complete();
    }
    
    /**
     * 格式化工具结果给AI看（结构化格式）
     * 注意：这里的内容会被AI看到，但AI不应该把这些内容原样输出给用户
     * 只提供简洁的产品信息，让AI能够理解并使用，然后用自然语言描述给用户
     */
    private String formatToolResultForAI(ToolResult result) {
        // 如果执行失败
        if (!result.isSuccess()) {
            return "搜索失败：" + (result.getMessage() != null ? result.getMessage() : "未知错误");
        }
        
        // 如果返回了数据
        if (result.getData() != null) {
            // 处理List类型（产品列表等）
            if (result.getData() instanceof List) {
                List<?> dataList = (List<?>) result.getData();
                if (!dataList.isEmpty()) {
                    StringBuilder formatted = new StringBuilder();
                    // 简洁的产品列表，不包含任何提示性文本
                    for (int i = 0; i < Math.min(dataList.size(), 10); i++) {
                        Object item = dataList.get(i);
                        if (item instanceof Map) {
                            Map<?, ?> product = (Map<?, ?>) item;
                            Object title = product.get("title");
                            Object price = product.get("price");
                            Object category = product.get("category");
                            Object region = product.get("region");
                            Object description = product.get("description");
                            
                            if (title != null) {
                                formatted.append(title);
                                if (price != null) {
                                    formatted.append("，").append(price).append("元");
                                }
                                if (category != null) {
                                    formatted.append("，").append(category);
                                }
                                if (region != null) {
                                    formatted.append("，").append(region);
                                }
                                if (description != null && description.toString().length() > 0) {
                                    String desc = description.toString();
                                    if (desc.length() > 50) {
                                        desc = desc.substring(0, 50) + "...";
                                    }
                                    formatted.append("，").append(desc);
                                }
                                formatted.append("\n");
                            }
                        }
                    }
                    
                    return formatted.toString();
                } else {
                    return "未找到产品";
                }
            } 
            // 处理Map类型（MCP工具返回的结构化数据：预算、行程、住宿等）
            else if (result.getData() instanceof Map) {
                // 将Map转换为格式化的JSON字符串，让AI能够理解
                try {
                    String jsonData = JSON.toJSONString(result.getData(), 
                        com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat);
                    return "工具返回的数据：\n" + jsonData;
                } catch (Exception e) {
                    log.error("格式化Map数据失败", e);
                    return "工具返回了结构化数据：" + result.getData().toString();
                }
            }
            // 其他类型
            else {
                return "工具返回：" + result.getData().toString();
            }
        }
        
        return "";
    }
    
    /**
     * 构建DeepSeek API请求
     */
    private Map<String, Object> buildDeepSeekRequest(ConversationContext context) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统提示词
        messages.add(Map.of(
            "role", "system",
            "content", buildSystemPrompt()
        ));
        
        // 历史消息（保留最近10轮）
        List<ConversationContext.Message> recentMessages = context.getRecentMessages(20);
        for (ConversationContext.Message msg : recentMessages) {
            messages.add(Map.of(
                "role", msg.getRole(),
                "content", msg.getContent()
            ));
        }
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", messages);
        request.put("tools", toolRegistry.getToolDefinitions());
        request.put("tool_choice", "auto");
        request.put("stream", true);
        request.put("temperature", 0.7);
        request.put("max_tokens", 2000);
        
        return request;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是景德镇文旅AI智能助手，一个专业的Agent智能体。\n\n");
        prompt.append("## 你的能力\n");
        prompt.append("1. 理解用户需求，提供专业、友好的建议\n");
        prompt.append("2. 调用工具完成各种任务（搜索、查询、预订等）\n");
        prompt.append("3. 记住对话历史，提供连贯的服务\n");
        prompt.append("4. 主动提供有价值的建议\n\n");
        
        prompt.append("## 可用工具\n");
        prompt.append(toolRegistry.getToolsDescription());
        prompt.append("\n");
        
        prompt.append("## 工作原则\n");
        prompt.append("1. 认真理解用户需求\n");
        prompt.append("2. 选择合适的工具帮助用户\n");
        prompt.append("3. 用友好、专业的语气回复\n");
        prompt.append("4. 遇到问题时，礼貌地说明并提供替代方案\n\n");
        
        prompt.append("## ⚠️ 关键：工具结果处理流程（必须严格遵守）\n");
        prompt.append("当工具执行完成后，你会在对话历史中看到工具返回的JSON格式数据。你必须按照以下步骤处理：\n\n");
        prompt.append("### 第一步：识别工具结果\n");
        prompt.append("工具结果是一个JSON对象，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"success\": true/false,  // 是否成功\n");
        prompt.append("  \"message\": \"消息描述\",  // 工具返回的消息\n");
        prompt.append("  \"data\": [...] 或 {...},  // 实际数据（可能是数组或对象）\n");
        prompt.append("  \"errorCode\": \"错误码\"  // 失败时的错误码\n");
        prompt.append("}\n\n");
        
        prompt.append("### 第二步：解析JSON数据（必须执行）\n");
        prompt.append("1. **检查success字段**：如果success为true，说明工具执行成功\n");
        prompt.append("2. **检查data字段**：这是最重要的字段，包含从数据库查询的真实数据\n");
        prompt.append("   - 如果data是数组（[]），检查数组是否为空\n");
        prompt.append("   - 如果data是数组且不为空，数组中每个元素都是一个产品对象\n");
        prompt.append("   - 产品对象包含：id、title、price、region、category、coverImage、description等字段\n");
        prompt.append("3. **检查message字段**：了解工具执行的情况\n\n");
        
        prompt.append("### 第三步：使用数据生成回复（关键步骤）\n");
        prompt.append("**情况1：工具返回了产品数据（data是数组且不为空）**\n");
        prompt.append("- 你必须使用这些真实的产品数据向用户推荐\n");
        prompt.append("- 从每个产品对象中提取：title（产品名称）、price（价格）、region（区域）、category（分类）等字段\n");
        prompt.append("- 用自然语言介绍每个产品，例如：\"我为您找到了[title]，价格为[price]元，位于[region]\"\n");
        prompt.append("- 绝对不要忽略工具返回的数据，即使数据看起来不完整也要使用\n");
        prompt.append("- 绝对不要编造产品信息，只能使用工具返回的真实数据\n\n");
        
        prompt.append("**情况2：工具返回空数据（data是空数组[]）**\n");
        prompt.append("- 如实告诉用户：\"抱歉，没有找到符合条件的产品\"\n");
        prompt.append("- 建议用户调整搜索条件（如放宽价格范围、选择其他区域、使用更宽泛的关键词等）\n");
        prompt.append("- 绝对不要编造产品信息来填充空结果\n\n");
        
        prompt.append("**情况3：工具执行失败（success为false）**\n");
        prompt.append("- 查看message和errorCode了解失败原因\n");
        prompt.append("- 友好地向用户说明情况，并提供替代方案\n\n");
        
        prompt.append("### 第四步：数据字段说明\n");
        prompt.append("**重要**：工具返回的数据来自product表（产品表），不是product_category表（分类表）！\n");
        prompt.append("产品对象的标准字段（来自product表，真实的产品数据）：\n");
        prompt.append("- id: 产品ID（数字，来自product表的id字段）\n");
        prompt.append("- title: 产品标题/名称（字符串，产品表特有字段，分类表没有）\n");
        prompt.append("- price: 产品价格（数字，产品表特有字段，分类表没有）\n");
        prompt.append("- originalPrice: 原价（数字，可能为null）\n");
        prompt.append("- region: 所在区域（字符串，如\"昌江区\"、\"珠山区\"）\n");
        prompt.append("- category: 分类名称（字符串，通过categoryId关联查询分类表获取，仅用于显示）\n");
        prompt.append("- coverImage: 封面图片URL（字符串，可能为null，产品表字段）\n");
        prompt.append("- description: 产品描述（字符串，可能被截断，产品表字段）\n");
        prompt.append("- rating: 评分（数字，产品表字段）\n");
        prompt.append("- sales: 销量（数字，产品表字段）\n");
        prompt.append("- tags: 标签（字符串，可能为null，产品表字段）\n");
        prompt.append("- _source: 数据来源标识（固定为\"product_table\"，表示来自产品表）\n\n");
        prompt.append("**区分产品数据和分类数据**：\n");
        prompt.append("- 产品数据（product表）：有title、price、description、coverImage等字段\n");
        prompt.append("- 分类数据（product_category表）：只有id、name、icon等字段，没有title、price等\n");
        prompt.append("- search_products工具返回的是产品数据，不是分类数据！\n\n");
        
        prompt.append("### 第五步：回复格式要求（非常重要，必须严格遵守）\n");
        prompt.append("1. **必须解析工具结果**：看到工具执行结果时，必须解析它\n");
        prompt.append("2. **必须检查data字段**：这是数据的关键，不能忽略\n");
        prompt.append("3. **使用真实数据**：如果data中有产品，必须使用这些产品数据\n");
        prompt.append("4. **绝对不要输出工具结果原文**：工具结果中的产品列表信息是给你看的，不要原样输出给用户\n");
        prompt.append("5. **绝对不要输出JSON**：前端会自动处理产品数据并显示产品卡片（带图片、可点击），你只需要用自然语言描述\n");
        prompt.append("6. **简洁描述**：用1-2句话介绍产品的特色和亮点即可，不要重复列出所有产品信息\n");
        prompt.append("7. **友好语气**：用自然、友好的语言，让用户感受到你的专业和热情\n");
        prompt.append("8. **示例回复**：\"我为您找到了几个特色的陶瓷体验活动，包括陶艺体验课程、古窑参观等，每个活动都有独特的文化内涵。\"\n");
        prompt.append("9. **禁止行为**：\n");
        prompt.append("   - 不要输出工具结果中的产品列表原文\n");
        prompt.append("   - 不要输出\"工具已找到X个产品\"这样的文本\n");
        prompt.append("   - 不要输出JSON格式\n");
        prompt.append("   - 不要输出任何技术性信息\n");
        prompt.append("   - 前端会自动显示产品卡片，你只需要用自然语言描述即可\n\n");
        
        prompt.append("### 示例说明\n");
        prompt.append("**示例1：工具返回了产品数据**\n");
        prompt.append("工具返回：找到2个产品（景德镇古窑民俗博览区、御窑厂国家考古遗址公园）\n\n");
        prompt.append("你的回复应该是（简洁、自然、友好）：\n");
        prompt.append("我为您找到了几个景德镇特色的景点，包括古窑民俗博览区和御窑厂国家考古遗址公园。这些景点都是了解景德镇陶瓷文化的绝佳去处，您可以体验传统的制瓷工艺，感受千年瓷都的魅力。\n\n");
        prompt.append("**注意**：\n");
        prompt.append("- 不要输出JSON格式\n");
        prompt.append("- 不要列出所有产品详情（前端会自动显示产品卡片）\n");
        prompt.append("- 用自然语言概括产品的特色和亮点\n");
        prompt.append("- 保持回复简洁友好（2-3句话即可）\n\n");
        
        prompt.append("**示例2：工具返回空数据**\n");
        prompt.append("工具返回JSON：\n");
        prompt.append("{\"success\":true,\"message\":\"未找到符合条件的产品，建议放宽搜索条件\",\"data\":[]}\n\n");
        prompt.append("你的回复应该是：\n");
        prompt.append("抱歉，没有找到符合条件的产品。建议您尝试：\n");
        prompt.append("- 使用更宽泛的关键词搜索\n");
        prompt.append("- 放宽价格范围\n");
        prompt.append("- 选择其他区域\n");
        prompt.append("（注意：不要编造产品信息）\n\n");
        
        prompt.append("## ⚠️ 重要提醒\n");
        prompt.append("1. **工具返回的数据来自product表（产品表）**，是真实的产品数据，必须使用这些数据\n");
        prompt.append("2. **如果data字段是数组且不为空，说明找到了产品**，数组中每个元素都是product表中的真实产品记录\n");
        prompt.append("3. **产品数据包含title、price等字段**，这些字段证明数据来自product表，不是分类表\n");
        prompt.append("4. **绝对禁止忽略工具返回的数据**，即使数据看起来不完整也要使用\n");
        prompt.append("5. **绝对禁止编造产品信息**，只能使用工具返回的真实产品数据\n");
        prompt.append("6. **如果找不到产品，如实告诉用户**，不要编造数据\n");
        prompt.append("7. **记住：search_products工具查询的是product表，返回的是产品，不是分类！**\n");
        prompt.append("8. **前端会自动显示产品卡片**：前端会通过tool_result事件自动处理产品数据，显示带图片、价格、可点击的产品卡片，你只需要用自然语言描述即可\n");
        prompt.append("9. **不要输出JSON或工具执行结果**：用户不需要看到JSON数据，前端会自动处理并显示产品卡片\n\n");
        
        prompt.append("## 产品搜索流程\n");
        prompt.append("当用户要求推荐或搜索产品时：\n");
        prompt.append("1. **必须调用search_products工具**：无论用户使用什么表达方式（如\"家庭游玩的景点推荐\"、\"价格实惠的酒店\"等），都必须调用search_products工具\n");
        prompt.append("2. **智能参数提取**：\n");
        prompt.append("   - query参数：提取用户查询中的核心关键词（如\"景点\"、\"酒店\"、\"美食\"等）\n");
        prompt.append("   - 工具会自动识别分类：如果查询中包含\"景点\"、\"酒店\"、\"美食\"等关键词，工具会自动匹配到对应的分类\n");
        prompt.append("   - 例如：\"家庭游玩的景点推荐\" -> query=\"景点\"，工具会自动识别为\"景点门票\"分类\n");
        prompt.append("   - 例如：\"价格实惠的酒店\" -> query=\"酒店\"，工具会自动识别为\"酒店住宿\"分类\n");
        prompt.append("   - 例如：\"陶瓷体验活动\" -> query=\"陶瓷体验\"，工具会自动识别为\"文化体验\"分类\n");
        prompt.append("3. **等待工具返回结果**：工具会从product表（产品表）查询真实的产品数据\n");
        prompt.append("4. **解析工具返回的JSON**：检查data字段，这是从数据库查询的真实产品数据\n");
        prompt.append("5. **使用真实数据回复**：\n");
        prompt.append("   - 如果data中有产品数据，必须使用这些真实的产品数据向用户推荐\n");
        prompt.append("   - 如果data为空数组，如实告诉用户并建议调整搜索条件\n");
        prompt.append("   - 绝对不要编造产品信息\n\n");
        
        prompt.append("## 快捷输入文本处理\n");
        prompt.append("用户可能使用快捷输入文本，如：\n");
        prompt.append("- \"家庭游玩的景点推荐\" -> 调用search_products(query=\"景点\")，工具会自动识别为\"景点门票\"分类\n");
        prompt.append("- \"价格实惠的酒店\" -> 调用search_products(query=\"酒店\")，工具会自动识别为\"酒店住宿\"分类\n");
        prompt.append("- \"陶瓷体验活动\" -> 调用search_products(query=\"陶瓷体验\")，工具会自动识别为\"文化体验\"分类\n");
        prompt.append("- \"当地特色美食\" -> 调用search_products(query=\"美食\")，工具会自动识别为\"特色餐饮\"分类\n");
        prompt.append("- \"适合拍照的景点\" -> 调用search_products(query=\"景点\")，工具会自动识别为\"景点门票\"分类\n");
        prompt.append("**重要**：无论用户使用什么表达方式，都必须调用search_products工具，工具会智能识别分类并返回数据库中的真实产品数据！\n\n");
        
        prompt.append("现在，请开始为用户服务！记住：\n");
        prompt.append("1. **必须调用search_products工具**，不能跳过工具直接回复\n");
        prompt.append("2. **必须使用工具返回的真实数据**，不能编造产品信息\n");
        prompt.append("3. **如果工具返回空数据，如实告诉用户**，不要编造数据");
        
        return prompt.toString();
    }
    
    /**
     * 创建新的对话上下文
     */
    private ConversationContext createNewContext(String sessionId, Long userId) {
        ConversationContext context = new ConversationContext();
        context.setSessionId(sessionId);
        context.setUserId(userId);
        context.setHistory(new ArrayList<>());
        context.setVariables(new HashMap<>());
        context.setCreateTime(LocalDateTime.now());
        context.setLastActiveTime(LocalDateTime.now());
        
        log.info("✨ 创建新会话: sessionId={}, userId={}", sessionId, userId);
        
        return context;
    }
    
    @Override
    public ConversationContext getSession(String sessionId, Long userId) {
        ConversationContext context = sessions.get(sessionId);
        if (context != null && context.getUserId().equals(userId)) {
            // 检查会话是否过期
            if (isSessionExpired(context)) {
                sessions.remove(sessionId);
                log.info("🗑️ 会话已过期并清除: sessionId={}", sessionId);
                return null;
            }
            return context;
        }
        return null;
    }
    
    @Override
    public void clearSession(String sessionId, Long userId) {
        ConversationContext context = sessions.get(sessionId);
        if (context != null && context.getUserId().equals(userId)) {
            sessions.remove(sessionId);
            log.info("🗑️ 清除会话: sessionId={}", sessionId);
        }
    }
    
    @Override
    public List<Map<String, Object>> getAvailableTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        for (AgentTool tool : toolRegistry.getAllTools()) {
            Map<String, Object> toolInfo = new HashMap<>();
            toolInfo.put("name", tool.getName());
            toolInfo.put("description", tool.getDescription());
            toolInfo.put("category", tool.getCategory());
            tools.add(toolInfo);
        }
        
        return tools;
    }
    
    /**
     * 启动会话清理定时任务
     */
    private void startSessionCleanupTask() {
        // 每10分钟清理一次过期会话
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Thread.sleep(10 * 60 * 1000L); // 10分钟
                    cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    log.error("会话清理任务被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        log.info("✅ 会话清理定时任务已启动");
    }
    
    /**
     * 清理过期会话
     */
    private void cleanupExpiredSessions() {
        int removedCount = 0;
        List<String> expiredSessionIds = new ArrayList<>();
        
        for (Map.Entry<String, ConversationContext> entry : sessions.entrySet()) {
            if (isSessionExpired(entry.getValue())) {
                expiredSessionIds.add(entry.getKey());
            }
        }
        
        for (String sessionId : expiredSessionIds) {
            sessions.remove(sessionId);
            removedCount++;
        }
        
        if (removedCount > 0) {
            log.info("🗑️ 清理了{}个过期会话，当前活跃会话数：{}", removedCount, sessions.size());
        }
    }
    
    /**
     * 检查会话是否过期
     */
    private boolean isSessionExpired(ConversationContext context) {
        if (context.getLastActiveTime() == null) {
            return true;
        }
        
        long inactiveTime = java.time.Duration.between(
            context.getLastActiveTime(), 
            LocalDateTime.now()
        ).toMillis();
        
        return inactiveTime > SESSION_TIMEOUT_MILLIS;
    }
}

