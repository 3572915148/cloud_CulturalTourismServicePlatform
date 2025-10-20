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

import java.io.IOException;
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
            wrapper.and(w -> w
                .like(Product::getTitle, request.getQuery())
                .or()
                .like(Product::getDescription, request.getQuery())
                .or()
                .like(Product::getTags, request.getQuery())
                .or()
                .like(Product::getRegion, request.getQuery())
            );
        }
        
        // 限制返回数量，避免上下文过长
        wrapper.last("LIMIT 20");
        wrapper.orderByDesc(Product::getRecommend, Product::getRating);
        
        return productMapper.selectList(wrapper);
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
            
            请按照以下格式回复：
            1. 首先给出你的推荐理由和建议
            2. 然后推荐3-5个最合适的产品，格式如下：
            推荐产品：[产品ID1, 产品ID2, 产品ID3, ...]
            
            要求：
            - 推荐要精准，符合用户需求
            - 考虑价格、位置、评分等因素
            - 给出推荐理由
            - 回复要友好、专业
            """, query, context);
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) throws IOException, InterruptedException {
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
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
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
                        // 验证产品ID是否在可用产品列表中
                        if (availableProducts.stream().anyMatch(product -> product.getId().equals(id))) {
                            recommendedIds.add(id);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析产品ID失败: {}", idStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析推荐产品ID失败", e);
        }
        
        // 如果没有解析到推荐产品，返回前3个匹配的产品
        if (recommendedIds.isEmpty() && !availableProducts.isEmpty()) {
            recommendedIds = availableProducts.stream()
                .limit(3)
                .map(Product::getId)
                .collect(Collectors.toList());
        }
        
        return recommendedIds;
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
}
