package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingdezhen.tourism.dto.AiRecommendationRequestDTO;
import com.jingdezhen.tourism.dto.AiRecommendationResponseDTO;
import com.jingdezhen.tourism.entity.AiRecommendation;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.AiRecommendationMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.service.AiRecommendationService;
import com.jingdezhen.tourism.vo.AiRecommendationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI推荐服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final AiRecommendationMapper aiRecommendationMapper;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    // DeepSeek API配置
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    @Value("${spring.ai.deepseek.api-key:sk-your-deepseek-api-key}")
    private String deepseekApiKey;
    
    @Value("${spring.ai.deepseek.model:deepseek-chat}")
    private String model;

    @Override
    @Transactional
    public AiRecommendationResponseDTO getRecommendation(Long userId, AiRecommendationRequestDTO request) {
        try {
            // 1. 查询匹配的产品
            List<Product> matchedProducts = findMatchingProducts(request);
            
            // 2. 构建上下文信息
            String context = buildContext(matchedProducts, request);
            
            // 3. 构建提示词
            String prompt = buildPrompt(request.getQuery(), context, request);
            
            // 4. 调用AI模型
            String aiResponse = callDeepSeekAPI(prompt);
            
            // 检查AI响应是否为空
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                log.warn("DeepSeek API返回空响应");
                aiResponse = "抱歉，AI服务暂时无法生成推荐内容。请稍后重试。";
            }
            
            log.info("AI响应内容长度: {}", aiResponse.length());
            log.debug("AI响应内容: {}", aiResponse);
            
            // 5. 解析AI响应，提取推荐的产品ID
            List<Long> recommendedProductIds = parseRecommendedProducts(aiResponse, matchedProducts);
            
            // 6. 构建推荐结果
            List<AiRecommendationResponseDTO.RecommendedProductDTO> recommendedProducts = 
                buildRecommendedProducts(recommendedProductIds, aiResponse);
            
            // 7. 保存推荐记录
            AiRecommendation recommendation = new AiRecommendation();
            recommendation.setUserId(userId);
            recommendation.setQuery(request.getQuery());
            recommendation.setContext(context);
            recommendation.setResponse(aiResponse);
            recommendation.setRecommendedProducts(objectMapper.writeValueAsString(recommendedProductIds));
            aiRecommendationMapper.insert(recommendation);
            
            // 8. 构建响应
            AiRecommendationResponseDTO response = new AiRecommendationResponseDTO();
            response.setRecommendationId(recommendation.getId());
            response.setResponse(aiResponse);
            response.setRecommendedProducts(recommendedProducts);
            response.setReason(extractReason(aiResponse));
            
            return response;
            
        } catch (Exception e) {
            log.error("AI推荐失败", e);
            throw new BusinessException("AI推荐服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public Page<AiRecommendationVO> getRecommendationHistory(Long userId, Long current, Long size) {
        Page<AiRecommendation> page = new Page<>(current, size);
        LambdaQueryWrapper<AiRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiRecommendation::getUserId, userId)
               .orderByDesc(AiRecommendation::getCreateTime);
        
        Page<AiRecommendation> result = aiRecommendationMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<AiRecommendationVO> voPage = new Page<>(current, size, result.getTotal());
        List<AiRecommendationVO> voList = result.getRecords().stream().map(record -> {
            AiRecommendationVO vo = new AiRecommendationVO();
            BeanUtils.copyProperties(record, vo);
            
            // 解析推荐的产品
            if (StringUtils.hasText(record.getRecommendedProducts())) {
                try {
                    List<Long> productIds = objectMapper.readValue(record.getRecommendedProducts(), 
                        new TypeReference<List<Long>>() {});
                    List<AiRecommendationVO.RecommendedProductVO> products = getProductVOs(productIds);
                    vo.setRecommendedProducts(products);
                } catch (Exception e) {
                    log.error("解析推荐产品失败", e);
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void submitFeedback(Long userId, Long recommendationId, Integer feedback) {
        AiRecommendation recommendation = aiRecommendationMapper.selectById(recommendationId);
        if (recommendation == null || !recommendation.getUserId().equals(userId)) {
            throw new BusinessException("推荐记录不存在");
        }
        
        recommendation.setFeedback(feedback);
        aiRecommendationMapper.updateById(recommendation);
    }

    @Override
    public Object getRecommendationStats(Long userId) {
        LambdaQueryWrapper<AiRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiRecommendation::getUserId, userId);
        
        List<AiRecommendation> records = aiRecommendationMapper.selectList(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecommendations", records.size());
        stats.put("helpfulCount", records.stream().mapToInt(r -> r.getFeedback() != null && r.getFeedback() == 1 ? 1 : 0).sum());
        stats.put("notHelpfulCount", records.stream().mapToInt(r -> r.getFeedback() != null && r.getFeedback() == 0 ? 1 : 0).sum());
        
        return stats;
    }

    /**
     * 查找匹配的产品
     */
    private List<Product> findMatchingProducts(AiRecommendationRequestDTO request) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查询上架的产品
        
        // 根据查询内容进行模糊匹配
        if (StringUtils.hasText(request.getQuery())) {
            String query = request.getQuery().trim();
            wrapper.and(w -> w
                .like(Product::getTitle, query)
                .or()
                .like(Product::getDescription, query)
                .or()
                .like(Product::getTags, query)
                .or()
                .like(Product::getRegion, query)
                .or()
                .like(Product::getFeatures, query)
            );
        } else {
            // 如果没有查询条件，返回推荐产品
            wrapper.eq(Product::getRecommend, 1);
        }
        
        // 限制返回数量，避免上下文过长
        wrapper.last("LIMIT 30"); // 增加数量以提供更多选择
        wrapper.orderByDesc(Product::getRecommend, Product::getRating, Product::getSales);
        
        List<Product> products = productMapper.selectList(wrapper);
        log.info("查询到 {} 个匹配的产品", products.size());
        
        // 如果匹配的产品太少，补充一些推荐产品
        if (products.size() < 10 && StringUtils.hasText(request.getQuery())) {
            List<Long> existingIds = products.stream().map(Product::getId).collect(Collectors.toList());
            if (!existingIds.isEmpty()) {
                LambdaQueryWrapper<Product> fallbackWrapper = new LambdaQueryWrapper<>();
                fallbackWrapper.eq(Product::getStatus, 1)
                              .eq(Product::getRecommend, 1)
                              .notIn(Product::getId, existingIds)
                              .last("LIMIT " + (20 - products.size()));
                fallbackWrapper.orderByDesc(Product::getRating, Product::getSales);
                
                List<Product> fallbackProducts = productMapper.selectList(fallbackWrapper);
                products.addAll(fallbackProducts);
                log.info("补充了 {} 个推荐产品，总计 {} 个产品", fallbackProducts.size(), products.size());
            } else {
                // 如果没有现有产品，直接查询推荐产品
                LambdaQueryWrapper<Product> fallbackWrapper = new LambdaQueryWrapper<>();
                fallbackWrapper.eq(Product::getStatus, 1)
                              .eq(Product::getRecommend, 1)
                              .last("LIMIT 20");
                fallbackWrapper.orderByDesc(Product::getRating, Product::getSales);
                
                List<Product> fallbackProducts = productMapper.selectList(fallbackWrapper);
                products.addAll(fallbackProducts);
                log.info("补充了 {} 个推荐产品，总计 {} 个产品", fallbackProducts.size(), products.size());
            }
        }
        
        return products;
    }

    /**
     * 构建上下文信息
     */
    private String buildContext(List<Product> products, AiRecommendationRequestDTO request) {
        StringBuilder context = new StringBuilder();
        context.append("景德镇文旅产品信息：\n");
        
        for (Product product : products) {
            context.append(String.format("产品ID: %d\n", product.getId()));
            context.append(String.format("标题: %s\n", product.getTitle()));
            context.append(String.format("描述: %s\n", product.getDescription()));
            context.append(String.format("价格: %s元\n", product.getPrice()));
            context.append(String.format("地区: %s\n", product.getRegion()));
            context.append(String.format("地址: %s\n", product.getAddress()));
            context.append(String.format("评分: %s\n", product.getRating()));
            context.append(String.format("标签: %s\n", product.getTags()));
            context.append("---\n");
        }
        
        if (StringUtils.hasText(request.getPreferences())) {
            context.append(String.format("用户偏好: %s\n", request.getPreferences()));
        }
        if (StringUtils.hasText(request.getBudget())) {
            context.append(String.format("预算范围: %s\n", request.getBudget()));
        }
        if (request.getPeopleCount() != null) {
            context.append(String.format("人数: %d人\n", request.getPeopleCount()));
        }
        
        return context.toString();
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String query, String context, AiRecommendationRequestDTO request) {
        return String.format("""
            你是一个专业的景德镇文旅推荐助手。请根据用户的需求和提供的产品信息，为用户推荐最合适的产品。
            
            用户查询：%s
            
            可用产品信息：
            %s
            
            重要约束：
            - 你只能推荐上述产品列表中的产品
            - 绝对不能推荐列表之外的产品
            - 推荐的产品ID必须存在于上述列表中
            
            请按照以下格式回复：
            1. 首先给出你的推荐理由和建议
            2. 然后推荐几个最合适的产品，格式如下：
            推荐产品：[产品ID1, 产品ID2, 产品ID3, ...]
            
            要求：
            - 推荐要精准，符合用户需求
            - 考虑价格、位置、评分等因素
            - 给出推荐理由
            - 回复要友好、专业
            - 只能推荐上述列表中的产品，不能推荐其他产品
            """, query, context);
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) throws IOException, InterruptedException {
        log.info("开始调用DeepSeek API...");
        long startTime = System.currentTimeMillis();
        
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(DEEPSEEK_API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + deepseekApiKey)
            .timeout(Duration.ofSeconds(50))  // 设置请求超时为50秒
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        long endTime = System.currentTimeMillis();
        log.info("DeepSeek API调用完成，耗时: {}ms", (endTime - startTime));

        if (response.statusCode() != 200) {
            log.error("DeepSeek API调用失败，状态码: {}, 响应内容: {}", response.statusCode(), response.body());
            throw new RuntimeException("DeepSeek API调用失败: " + response.statusCode());
        }

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        }

        throw new RuntimeException("DeepSeek API响应格式错误");
    }

    /**
     * 解析推荐的产品ID
     */
    private List<Long> parseRecommendedProducts(String aiResponse, List<Product> availableProducts) {
        List<Long> recommendedIds = new ArrayList<>();
        
        // 创建可用产品ID集合，用于快速验证
        Set<Long> availableProductIds = availableProducts.stream()
            .map(Product::getId)
            .collect(Collectors.toSet());
        
        log.info("可用产品ID列表: {}", availableProductIds);
        log.info("AI响应内容: {}", aiResponse);
        
        try {
            // 查找"推荐产品："后面的内容
            String pattern = "推荐产品：\\[([^\\]]+)\\]";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(aiResponse);
            
            if (m.find()) {
                String idsStr = m.group(1);
                String[] idStrings = idsStr.split(",");
                for (String idStr : idStrings) {
                    try {
                        Long id = Long.parseLong(idStr.trim());
                        // 严格验证产品ID是否在可用产品列表中
                        if (availableProductIds.contains(id)) {
                            recommendedIds.add(id);
                            log.info("成功解析并验证产品ID: {}", id);
                        } else {
                            log.warn("AI推荐的产品ID {} 不在可用产品列表中，已忽略", id);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析产品ID失败，无效格式: {}", idStr);
                    }
                }
            } else {
                log.warn("AI响应中未找到推荐产品格式，尝试其他解析方式");
                // 尝试其他可能的格式
                parseAlternativeFormats(aiResponse, availableProductIds, recommendedIds);
            }
        } catch (Exception e) {
            log.error("解析推荐产品ID失败", e);
        }
        
        // 如果没有解析到推荐产品，返回前3个匹配的产品
        if (recommendedIds.isEmpty() && !availableProducts.isEmpty()) {
            log.info("AI解析失败，使用默认推荐策略");
            recommendedIds = availableProducts.stream()
                .limit(3)
                .map(Product::getId)
                .collect(Collectors.toList());
        }
        
        log.info("最终推荐的产品ID列表: {}", recommendedIds);
        return recommendedIds;
    }
    
    /**
     * 尝试其他格式解析
     */
    private void parseAlternativeFormats(String aiResponse, Set<Long> availableProductIds, List<Long> recommendedIds) {
        // 尝试查找数字ID模式
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\b(\\d+)\\b");
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(aiResponse);
        
        while (numberMatcher.find()) {
            try {
                Long id = Long.parseLong(numberMatcher.group(1));
                if (availableProductIds.contains(id) && !recommendedIds.contains(id)) {
                    recommendedIds.add(id);
                    log.info("通过数字模式解析到产品ID: {}", id);
                    if (recommendedIds.size() >= 3) break; // 最多推荐3个
                }
            } catch (NumberFormatException e) {
                // 忽略非数字
            }
        }
    }

    /**
     * 构建推荐产品DTO列表
     */
    private List<AiRecommendationResponseDTO.RecommendedProductDTO> buildRecommendedProducts(
            List<Long> productIds, String aiResponse) {
        
        return productIds.stream().map(id -> {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return null;
            }
            
            AiRecommendationResponseDTO.RecommendedProductDTO dto = 
                new AiRecommendationResponseDTO.RecommendedProductDTO();
            dto.setId(product.getId());
            dto.setTitle(product.getTitle());
            dto.setDescription(product.getDescription());
            dto.setCoverImage(product.getCoverImage());
            dto.setPrice(product.getPrice().toString());
            dto.setRegion(product.getRegion());
            dto.setAddress(product.getAddress());
            dto.setRating(product.getRating() != null ? product.getRating().doubleValue() : 0.0);
            dto.setTags(product.getTags());
            dto.setReason(extractProductReason(aiResponse, product.getTitle()));
            
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 提取推荐理由
     */
    private String extractReason(String aiResponse) {
        // 简单提取，取第一段作为推荐理由
        String[] lines = aiResponse.split("\n");
        StringBuilder reason = new StringBuilder();
        for (String line : lines) {
            if (line.trim().startsWith("推荐产品：")) {
                break;
            }
            if (StringUtils.hasText(line.trim())) {
                reason.append(line.trim()).append(" ");
            }
        }
        return reason.toString().trim();
    }

    /**
     * 提取产品推荐理由
     */
    private String extractProductReason(String aiResponse, String productTitle) {
        // 查找包含产品标题的行作为推荐理由
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            if (line.contains(productTitle)) {
                return line.trim();
            }
        }
        return "根据您的需求推荐";
    }

    /**
     * 获取产品VO列表
     */
    private List<AiRecommendationVO.RecommendedProductVO> getProductVOs(List<Long> productIds) {
        return productIds.stream().map(id -> {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return null;
            }
            
            AiRecommendationVO.RecommendedProductVO vo = new AiRecommendationVO.RecommendedProductVO();
            vo.setId(product.getId());
            vo.setTitle(product.getTitle());
            vo.setDescription(product.getDescription());
            vo.setCoverImage(product.getCoverImage());
            vo.setPrice(product.getPrice().toString());
            vo.setRegion(product.getRegion());
            vo.setAddress(product.getAddress());
            vo.setRating(product.getRating() != null ? product.getRating().doubleValue() : 0.0);
            vo.setTags(product.getTags());
            
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void getRecommendationStream(Long userId, AiRecommendationRequestDTO request, SseEmitter emitter) {
        // 设置完成和超时回调
        emitter.onCompletion(() -> log.info("SSE连接正常关闭"));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        emitter.onError(e -> log.error("SSE连接错误", e));
        
        try {
            log.info("开始处理流式AI推荐请求, 用户ID: {}, 查询: {}", userId, request.getQuery());
            
            // 1. 在主线程中查询匹配的产品（避免异步线程访问数据库）
            List<Product> matchedProducts = findMatchingProducts(request);
            log.info("找到 {} 个匹配的产品", matchedProducts.size());
            
            // 2. 预加载所有产品详情（避免异步线程访问数据库）
            Map<Long, Product> productCache = new HashMap<>();
            for (Product product : matchedProducts) {
                productCache.put(product.getId(), product);
            }
            
            log.info("已预加载 {} 个产品详情", productCache.size());
            
            // 3. 构建上下文信息
            String context = buildContext(matchedProducts, request);
            
            // 4. 构建提示词
            String prompt = buildPrompt(request.getQuery(), context, request);
            
            // 5. 在新线程中处理AI调用（避免阻塞主线程，但不使用@Async避免生命周期问题）
            Thread streamThread = new Thread(() -> {
                try {
                    log.info("流式推送线程开始执行");
                    
                    // 立即发送一条测试消息，确保SSE通道正常
                    try {
                        Map<String, Object> testData = new HashMap<>();
                        testData.put("type", "content");
                        testData.put("content", "");  // 初始化为空，让前端知道连接已建立
                        String jsonData = objectMapper.writeValueAsString(testData);
                        emitter.send(SseEmitter.event()
                                .data(jsonData));
                        log.info("初始化消息发送成功");
                    } catch (Exception e) {
                        log.error("发送初始化消息失败", e);
                    }
                    
                    // 调用DeepSeek流式API
                    StringBuilder fullResponse = new StringBuilder();
                    boolean apiSuccess = false;
                    
                    try {
                        log.info("准备调用DeepSeek流式API");
                        callDeepSeekStreamAPI(prompt, emitter, fullResponse);
                        apiSuccess = true;
                        log.info("DeepSeek API调用成功，响应长度: {}", fullResponse.length());
                        
                        // 检查响应是否为空
                        if (fullResponse.length() == 0) {
                            log.warn("DeepSeek API返回空内容，使用降级方案");
                            throw new IOException("DeepSeek API返回空内容");
                        }
                    } catch (Exception apiError) {
                        log.error("DeepSeek API调用失败: {}", apiError.getMessage());
                        log.error("错误类型: {}", apiError.getClass().getName());
                        log.error("错误堆栈: ", apiError);
                        
                        // 发送降级消息
                        String fallbackMessage = generateFallbackMessage(matchedProducts);
                        fullResponse.setLength(0);  // 清空之前可能的部分响应
                        fullResponse.append(fallbackMessage);
                        
                        log.info("准备发送降级消息，长度: {}", fallbackMessage.length());
                        
                        // 逐步发送降级消息（确保异常被捕获）
                        try {
                            sendContentInChunks(emitter, fallbackMessage);
                            log.info("降级消息发送完成");
                        } catch (IOException e) {
                            log.error("发送降级消息失败", e);
                            // 即使发送失败，也要继续处理，确保complete事件能发送
                        }
                    }
                    
                    String aiResponse = fullResponse.toString();
                    
                    // 解析AI响应，提取推荐的产品ID
                    List<Long> recommendedProductIds = parseRecommendedProducts(aiResponse, matchedProducts);
                    
                    // 如果没有解析到产品ID，使用默认推荐
                    if (recommendedProductIds.isEmpty() && !matchedProducts.isEmpty()) {
                        recommendedProductIds = matchedProducts.stream()
                            .limit(3)
                            .map(Product::getId)
                            .collect(Collectors.toList());
                        log.info("使用默认推荐产品: {}", recommendedProductIds);
                    }
                    
                    // 使用预加载的产品缓存构建推荐结果（避免访问数据库）
                    List<AiRecommendationResponseDTO.RecommendedProductDTO> recommendedProducts = 
                        buildRecommendedProductsFromCache(recommendedProductIds, aiResponse, productCache);
                    
                    // 发送产品推荐信息
                    Map<String, Object> productsData = new HashMap<>();
                    productsData.put("type", "products");
                    productsData.put("products", recommendedProducts);
                    productsData.put("productIds", recommendedProductIds);
                    String productsJson = objectMapper.writeValueAsString(productsData);
                    log.info("准备发送products事件，数据: {}", productsJson.substring(0, Math.min(100, productsJson.length())));
                    emitter.send(SseEmitter.event().data(productsJson));
                    
                    // 保存推荐记录（这里需要在主线程完成）
                    saveRecommendationRecord(userId, request, context, aiResponse, recommendedProductIds);
                    
                    // 发送完成事件
                    Map<String, Object> completeData = new HashMap<>();
                    completeData.put("type", "complete");
                    completeData.put("recommendationId", System.currentTimeMillis());
                    completeData.put("apiSuccess", apiSuccess);
                    String completeJson = objectMapper.writeValueAsString(completeData);
                    log.info("准备发送complete事件，数据: {}", completeJson);
                    emitter.send(SseEmitter.event().data(completeJson));
                    
                    emitter.complete();
                    log.info("流式AI推荐请求处理完成");
                    
                } catch (Exception e) {
                    log.error("流式推送处理失败", e);
                    try {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("type", "error");
                        errorData.put("message", "AI推荐服务暂时不可用");
                        String errorJson = objectMapper.writeValueAsString(errorData);
                        log.error("发送error事件: {}", errorJson);
                        emitter.send(SseEmitter.event().data(errorJson));
                    } catch (Exception ex) {
                        log.error("发送错误消息失败", ex);
                    } finally {
                        emitter.completeWithError(e);
                    }
                }
            });
            
            streamThread.setName("AI-Stream-" + userId);
            streamThread.setDaemon(true); // 设置为守护线程，应用关闭时自动终止
            streamThread.start();
            
        } catch (Exception e) {
            log.error("流式AI推荐失败", e);
            try {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("type", "error");
                errorData.put("message", "AI推荐服务暂时不可用，请稍后重试");
                errorData.put("error", e.getMessage());
                String errorJson = objectMapper.writeValueAsString(errorData);
                log.error("发送error事件: {}", errorJson);
                emitter.send(SseEmitter.event().data(errorJson));
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            } finally {
                emitter.completeWithError(e);
            }
        }
    }
    
    /**
     * 从缓存构建推荐产品列表（避免访问数据库）
     */
    private List<AiRecommendationResponseDTO.RecommendedProductDTO> buildRecommendedProductsFromCache(
            List<Long> productIds, String aiResponse, Map<Long, Product> productCache) {
        
        List<AiRecommendationResponseDTO.RecommendedProductDTO> result = new ArrayList<>();
        
        for (Long productId : productIds) {
            Product product = productCache.get(productId);
            if (product == null) {
                log.warn("产品 {} 不在缓存中", productId);
                continue;
            }
            
            AiRecommendationResponseDTO.RecommendedProductDTO dto = new AiRecommendationResponseDTO.RecommendedProductDTO();
            dto.setId(product.getId());
            dto.setTitle(product.getTitle());
            dto.setDescription(product.getDescription());
            dto.setCoverImage(product.getCoverImage());
            dto.setPrice(product.getPrice().toString());
            dto.setRegion(product.getRegion());
            dto.setAddress(product.getAddress());
            dto.setRating(product.getRating() != null ? product.getRating().doubleValue() : 0.0);
            dto.setTags(product.getTags());
            dto.setReason(extractProductReason(aiResponse, product.getTitle()));
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * 保存推荐记录（同步方法，确保数据库可用）
     */
    private synchronized void saveRecommendationRecord(Long userId, AiRecommendationRequestDTO request, 
            String context, String aiResponse, List<Long> recommendedProductIds) {
        try {
            AiRecommendation recommendation = new AiRecommendation();
            recommendation.setUserId(userId);
            recommendation.setQuery(request.getQuery());
            recommendation.setContext(context);
            recommendation.setResponse(aiResponse);
            recommendation.setRecommendedProducts(objectMapper.writeValueAsString(recommendedProductIds));
            aiRecommendationMapper.insert(recommendation);
            log.info("推荐记录已保存，ID: {}", recommendation.getId());
        } catch (Exception e) {
            log.error("保存推荐记录失败（非关键错误，继续处理）", e);
        }
    }
    
    /**
     * 生成降级消息
     */
    private String generateFallbackMessage(List<Product> matchedProducts) {
        StringBuilder message = new StringBuilder();
        message.append("根据您的需求，我为您推荐以下产品：\n\n");
        
        int count = Math.min(3, matchedProducts.size());
        for (int i = 0; i < count; i++) {
            Product p = matchedProducts.get(i);
            message.append(String.format("%d. %s\n", i + 1, p.getTitle()));
            message.append(String.format("   💰 价格：%s元\n", p.getPrice()));
            message.append(String.format("   📍 地区：%s\n", p.getRegion()));
            if (p.getRating() != null) {
                message.append(String.format("   ⭐ 评分：%.1f\n", p.getRating()));
            }
            if (p.getDescription() != null && p.getDescription().length() > 0) {
                String desc = p.getDescription().length() > 50 
                    ? p.getDescription().substring(0, 50) + "..." 
                    : p.getDescription();
                message.append(String.format("   📝 简介：%s\n", desc));
            }
            message.append("\n");
        }
        
        message.append("推荐产品：[");
        for (int i = 0; i < count; i++) {
            if (i > 0) message.append(", ");
            message.append(matchedProducts.get(i).getId());
        }
        message.append("]\n");
        
        return message.toString();
    }
    
    /**
     * 分块发送内容
     */
    private void sendContentInChunks(SseEmitter emitter, String content) throws IOException {
        int chunkSize = 10; // 每次发送10个字符
        for (int i = 0; i < content.length(); i += chunkSize) {
            String chunk = content.substring(i, Math.min(i + chunkSize, content.length()));
            Map<String, Object> chunkData = new HashMap<>();
            chunkData.put("type", "content");
            chunkData.put("content", chunk);
            String chunkJson = objectMapper.writeValueAsString(chunkData);
            emitter.send(SseEmitter.event().data(chunkJson));
            
            try {
                Thread.sleep(30); // 模拟打字效果
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 调用DeepSeek流式API（带重试机制）
     */
    private void callDeepSeekStreamAPI(String prompt, SseEmitter emitter, StringBuilder fullResponse) throws IOException, InterruptedException {
        int maxRetries = 2;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount <= maxRetries) {
            try {
                log.info("开始调用DeepSeek流式API (尝试 {}/{})", retryCount + 1, maxRetries + 1);
                callDeepSeekStreamAPIInternal(prompt, emitter, fullResponse);
                return; // 成功，直接返回
            } catch (javax.net.ssl.SSLHandshakeException e) {
                lastException = e;
                retryCount++;
                log.warn("SSL握手失败 (尝试 {}/{}): {}", retryCount, maxRetries + 1, e.getMessage());
                
                if (retryCount <= maxRetries) {
                    Thread.sleep(1000 * retryCount); // 指数退避
                }
            } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
                lastException = e;
                retryCount++;
                log.warn("网络连接失败 (尝试 {}/{}): {}", retryCount, maxRetries + 1, e.getMessage());
                
                if (retryCount <= maxRetries) {
                    Thread.sleep(1000 * retryCount);
                }
            }
        }
        
        // 所有重试都失败
        log.error("DeepSeek API调用失败，已重试 {} 次", maxRetries);
        throw new IOException("DeepSeek API不可用: " + (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
    
    /**
     * 调用DeepSeek流式API的内部实现
     */
    private void callDeepSeekStreamAPIInternal(String prompt, SseEmitter emitter, StringBuilder fullResponse) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        
        // 创建HTTP客户端，禁用SSL验证（仅用于开发/测试）
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .version(HttpClient.Version.HTTP_1_1) // 使用HTTP/1.1可能更稳定
            .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        requestBody.put("stream", true);  // 启用流式响应

        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        
        log.debug("DeepSeek API请求: {}", requestBodyJson.substring(0, Math.min(100, requestBodyJson.length())));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(DEEPSEEK_API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + deepseekApiKey)
            .header("Accept", "text/event-stream")
            .timeout(Duration.ofSeconds(50))
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
            .build();

        HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        int statusCode = response.statusCode();
        log.info("DeepSeek API响应状态码: {}", statusCode);
        
        if (statusCode != 200) {
            String errorBody = "";
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(response.body()))) {
                errorBody = errorReader.lines().collect(Collectors.joining("\n"));
            }
            log.error("DeepSeek API调用失败，状态码: {}, 响应: {}", statusCode, errorBody);
            throw new IOException("DeepSeek API调用失败: " + statusCode + " - " + errorBody);
        }

        // 读取流式响应
        int contentLength = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    
                    // 跳过[DONE]标记
                    if ("[DONE]".equals(data.trim())) {
                        log.info("收到[DONE]标记，流式响应结束");
                        break;
                    }
                    
                    if (data.trim().isEmpty()) {
                        continue;
                    }
                    
                    try {
                        // 解析JSON响应
                        Map<String, Object> responseMap = objectMapper.readValue(data, Map.class);
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                        
                        if (choices != null && !choices.isEmpty()) {
                            Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                            if (delta != null && delta.containsKey("content")) {
                                String content = (String) delta.get("content");
                                if (content != null && !content.isEmpty()) {
                                    fullResponse.append(content);
                                    contentLength += content.length();
                                    
                                    // 发送内容块到前端
                                    Map<String, Object> chunkData = new HashMap<>();
                                    chunkData.put("type", "content");
                                    chunkData.put("content", content);
                                    String chunkJson = objectMapper.writeValueAsString(chunkData);
                                    emitter.send(SseEmitter.event().data(chunkJson));
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析流式响应块失败: {}, 数据: {}", e.getMessage(), data.substring(0, Math.min(50, data.length())));
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        log.info("DeepSeek流式API调用成功，耗时: {}ms，总长度: {}", (endTime - startTime), contentLength);
        
        if (contentLength == 0) {
            log.warn("DeepSeek API返回了空内容");
            throw new IOException("DeepSeek API返回空内容");
        }
    }
}
