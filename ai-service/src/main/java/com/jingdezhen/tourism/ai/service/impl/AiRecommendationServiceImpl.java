package com.jingdezhen.tourism.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingdezhen.tourism.common.dto.AiRecommendationRequestDTO;
import com.jingdezhen.tourism.common.dto.AiRecommendationResponseDTO;
import com.jingdezhen.tourism.common.entity.AiRecommendation;
import com.jingdezhen.tourism.common.entity.Product;
import com.jingdezhen.tourism.common.exception.BusinessException;
import com.jingdezhen.tourism.common.vo.AiRecommendationVO;
import com.jingdezhen.tourism.common.vo.Result;
import com.jingdezhen.tourism.ai.feign.ProductServiceClient;
import com.jingdezhen.tourism.ai.mapper.AiRecommendationMapper;
import com.jingdezhen.tourism.ai.service.AiRecommendationService;
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
 * AI推荐服务实现类（简化版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final AiRecommendationMapper aiRecommendationMapper;
    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api-key:sk-your-deepseek-api-key}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${ai.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Override
    @Transactional
    public AiRecommendationResponseDTO getRecommendation(Long userId, AiRecommendationRequestDTO request) {
        try {
            // 1. 查询匹配的产品（通过Feign调用product-service）
            List<Product> matchedProducts = findMatchingProducts(request);

            // 2. 构建上下文信息
            String context = buildContext(matchedProducts, request);

            // 3. 构建提示词
            String prompt = buildPrompt(request.getQuery(), context, request);

            // 4. 调用AI模型
            String aiResponse = callDeepSeekAPI(prompt);

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                log.warn("DeepSeek API返回空响应");
                aiResponse = "抱歉，AI服务暂时无法生成推荐内容。请稍后重试。";
            }

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
    public void getRecommendationStream(Long userId, AiRecommendationRequestDTO request, SseEmitter emitter) {
        // 简化实现：直接调用非流式接口，然后流式返回
        try {
            AiRecommendationResponseDTO response = getRecommendation(userId, request);
            String content = response.getResponse();
            
            // 模拟流式输出
            String[] sentences = content.split("[。！？\n]");
            for (String sentence : sentences) {
                if (sentence.trim().isEmpty()) continue;
                emitter.send(SseEmitter.event().data(sentence.trim() + "。"));
                Thread.sleep(100); // 模拟延迟
            }
            
            emitter.complete();
        } catch (Exception e) {
            log.error("流式推荐失败", e);
            emitter.completeWithError(e);
        }
    }

    @Override
    public Page<AiRecommendationVO> getRecommendationHistory(Long userId, Long current, Long size) {
        Page<AiRecommendation> page = new Page<>(current, size);
        LambdaQueryWrapper<AiRecommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiRecommendation::getUserId, userId)
               .orderByDesc(AiRecommendation::getCreateTime);

        Page<AiRecommendation> result = aiRecommendationMapper.selectPage(page, wrapper);

        Page<AiRecommendationVO> voPage = new Page<>(current, size, result.getTotal());
        List<AiRecommendationVO> voList = result.getRecords().stream().map(record -> {
            AiRecommendationVO vo = new AiRecommendationVO();
            BeanUtils.copyProperties(record, vo);
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
        // 通过Feign调用product-service
        Result<Page<Product>> result = productServiceClient.getProductList(1L, 100L, null, null, request.getQuery());
        if (result.getCode() == 200 && result.getData() != null) {
            return result.getData().getRecords();
        }
        return Collections.emptyList();
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
            context.append("---\n");
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
            
            请推荐3-5个最合适的产品，并说明推荐理由。
            """, query, context);
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String requestBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {"role": "user", "content": "%s"}
                ],
                "temperature": 0.7,
                "max_tokens": 2000
            }
            """, model, prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + deepseekApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 解析响应
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }

        log.error("DeepSeek API调用失败: status={}, body={}", response.statusCode(), response.body());
        return null;
    }

    /**
     * 解析推荐的产品ID
     */
    private List<Long> parseRecommendedProducts(String aiResponse, List<Product> products) {
        List<Long> productIds = new ArrayList<>();
        
        // 简单解析：查找产品ID
        for (Product product : products) {
            if (aiResponse.contains(String.valueOf(product.getId())) || 
                aiResponse.contains(product.getTitle())) {
                productIds.add(product.getId());
            }
        }
        
        // 如果没找到，返回前3个产品
        if (productIds.isEmpty() && !products.isEmpty()) {
            productIds = products.stream()
                    .limit(3)
                    .map(Product::getId)
                    .collect(Collectors.toList());
        }
        
        return productIds;
    }

    /**
     * 构建推荐产品列表
     */
    private List<AiRecommendationResponseDTO.RecommendedProductDTO> buildRecommendedProducts(
            List<Long> productIds, String aiResponse) {
        return productIds.stream().map(productId -> {
            AiRecommendationResponseDTO.RecommendedProductDTO dto = 
                new AiRecommendationResponseDTO.RecommendedProductDTO();
            dto.setId(productId);
            // 这里可以进一步查询产品详情
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 提取推荐理由
     */
    private String extractReason(String aiResponse) {
        // 简单提取：返回前200个字符
        if (aiResponse.length() > 200) {
            return aiResponse.substring(0, 200) + "...";
        }
        return aiResponse;
    }
}

