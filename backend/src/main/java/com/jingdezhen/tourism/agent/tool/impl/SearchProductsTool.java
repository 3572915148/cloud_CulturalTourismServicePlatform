package com.jingdezhen.tourism.agent.tool.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.entity.ProductCategory;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.mapper.ProductCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索产品工具
 * 支持按关键词、分类、区域、价格范围搜索
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchProductsTool implements AgentTool {
    
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    
    @Override
    public String getName() {
        return "search_products";
    }
    
    @Override
    public String getDescription() {
        return "搜索旅游产品。支持按关键词、分类ID或分类名称、区域、价格范围搜索产品。可以使用get_product_categories工具先获取可用的分类列表。";
    }
    
    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "query": {
                    "type": "string",
                    "description": "搜索关键词，可以是产品名称、特色、地点等。如果只按分类搜索，可以不提供此参数"
                },
                "categoryId": {
                    "type": "integer",
                    "description": "产品分类ID。可以使用get_product_categories工具获取可用的分类ID"
                },
                "categoryName": {
                    "type": "string",
                    "description": "产品分类名称（可选，如果提供categoryId则忽略此参数）。例如：景点门票、酒店住宿、特色餐饮等。支持模糊匹配"
                },
                "region": {
                    "type": "string",
                    "description": "区域名称，如：昌江区、珠山区、浮梁县等"
                },
                "minPrice": {
                    "type": "number",
                    "description": "最低价格"
                },
                "maxPrice": {
                    "type": "number",
                    "description": "最高价格"
                },
                "maxResults": {
                    "type": "integer",
                    "description": "最多返回结果数量，默认10，最大30",
                    "default": 10
                }
            },
            "required": []
        }
        """;
    }
    
    @Override
    public String getCategory() {
        return "product";
    }
    
    @Override
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("🔍 执行搜索产品工具: userId={}, params={}", userId, parameters);
            
            // 提取参数
            String query = (String) parameters.get("query");
            Long categoryId = extractLong(parameters, "categoryId");
            String categoryName = (String) parameters.get("categoryName");
            String region = (String) parameters.get("region");
            Double minPrice = extractDouble(parameters, "minPrice");
            Double maxPrice = extractDouble(parameters, "maxPrice");
            Integer maxResults = extractInteger(parameters, "maxResults", 10);
            
            // 限制最大结果数
            if (maxResults > 30) {
                maxResults = 30;
            }
            
            // 如果提供了分类名称但没有分类ID，尝试通过名称查找分类ID
            if (categoryId == null && categoryName != null && !categoryName.trim().isEmpty()) {
                String searchCategoryName = categoryName.trim();
                LambdaQueryWrapper<ProductCategory> categoryWrapper = new LambdaQueryWrapper<>();
                
                // 先尝试精确匹配（MyBatis-Plus会自动处理逻辑删除）
                categoryWrapper.eq(ProductCategory::getName, searchCategoryName);
                ProductCategory category = categoryMapper.selectOne(categoryWrapper);
                
                // 如果精确匹配失败，尝试模糊匹配
                if (category == null) {
                    categoryWrapper = new LambdaQueryWrapper<>();
                    categoryWrapper.like(ProductCategory::getName, searchCategoryName);
                    List<ProductCategory> categories = categoryMapper.selectList(categoryWrapper);
                    if (categories != null && !categories.isEmpty()) {
                        category = categories.get(0); // 取第一个匹配的分类
                        log.info("通过模糊匹配分类名称'{}'找到分类: {}", searchCategoryName, category.getName());
                    }
                }
                
                if (category != null) {
                    categoryId = category.getId();
                    log.info("通过分类名称'{}'找到分类ID: {}", searchCategoryName, categoryId);
                } else {
                    log.warn("未找到分类名称'{}'对应的分类", searchCategoryName);
                }
            }
            
            // 如果既没有分类ID也没有分类名称，但有关键词，尝试从关键词中识别分类
            // 例如："景点" -> "景点门票"，"酒店" -> "酒店住宿"
            // 优化：支持更智能的分类识别，包括快捷输入文本
            if (categoryId == null && categoryName == null && query != null && !query.trim().isEmpty()) {
                String queryLower = query.toLowerCase();
                // 分类关键词映射（优先级从高到低）
                Map<String, String> categoryKeywords = new HashMap<>();
                // 景点相关
                categoryKeywords.put("景点", "景点门票");
                categoryKeywords.put("门票", "景点门票");
                categoryKeywords.put("景区", "景点门票");
                categoryKeywords.put("旅游", "景点门票");
                categoryKeywords.put("游玩", "景点门票");
                categoryKeywords.put("拍照", "景点门票"); // 适合拍照的景点
                // 酒店相关
                categoryKeywords.put("酒店", "酒店住宿");
                categoryKeywords.put("住宿", "酒店住宿");
                categoryKeywords.put("宾馆", "酒店住宿");
                categoryKeywords.put("旅馆", "酒店住宿");
                // 餐饮相关
                categoryKeywords.put("餐饮", "特色餐饮");
                categoryKeywords.put("美食", "特色餐饮");
                categoryKeywords.put("餐厅", "特色餐饮");
                categoryKeywords.put("小吃", "特色餐饮");
                categoryKeywords.put("特色", "特色餐饮");
                // 体验相关
                categoryKeywords.put("体验", "文化体验");
                categoryKeywords.put("文化", "文化体验");
                categoryKeywords.put("活动", "文化体验");
                categoryKeywords.put("陶瓷体验", "文化体验");
                // 购物相关
                categoryKeywords.put("陶瓷", "陶瓷购物");
                categoryKeywords.put("购物", "陶瓷购物");
                categoryKeywords.put("工艺品", "陶瓷购物");
                
                // 查找匹配的分类关键词（按优先级匹配）
                String matchedCategoryName = null;
                // 优先匹配更具体的词汇（如"陶瓷体验"优先于"体验"）
                List<String> sortedKeys = new ArrayList<>(categoryKeywords.keySet());
                sortedKeys.sort((a, b) -> Integer.compare(b.length(), a.length())); // 按长度降序排序
                
                for (String key : sortedKeys) {
                    if (queryLower.contains(key)) {
                        matchedCategoryName = categoryKeywords.get(key);
                        log.info("从查询'{}'中识别出关键词'{}'，对应分类'{}'", query, key, matchedCategoryName);
                        break;
                    }
                }
                
                // 如果找到匹配的分类，查询分类ID
                if (matchedCategoryName != null) {
                    LambdaQueryWrapper<ProductCategory> categoryWrapper = new LambdaQueryWrapper<>();
                    // 先尝试精确匹配（MyBatis-Plus会自动处理逻辑删除）
                    categoryWrapper.eq(ProductCategory::getName, matchedCategoryName);
                    ProductCategory category = categoryMapper.selectOne(categoryWrapper);
                    
                    // 如果精确匹配失败，尝试模糊匹配
                    if (category == null) {
                        categoryWrapper = new LambdaQueryWrapper<>();
                        categoryWrapper.like(ProductCategory::getName, matchedCategoryName);
                        List<ProductCategory> categories = categoryMapper.selectList(categoryWrapper);
                        if (categories != null && !categories.isEmpty()) {
                            category = categories.get(0);
                        }
                    }
                    
                    if (category != null) {
                        categoryId = category.getId();
                        log.info("✅ 从查询'{}'中识别出分类'{}'，分类ID: {}", query, category.getName(), categoryId);
                    } else {
                        log.warn("⚠️ 识别出分类名称'{}'，但数据库中未找到该分类", matchedCategoryName);
                    }
                }
            }
            
            // 构建查询条件
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getStatus, 1); // 只查询上架的产品
            
            // 检查是否有任何搜索条件
            boolean hasSearchCondition = false;
            
            // 关键词搜索（标题或描述包含）
            // 改进：提取关键词，支持更灵活的搜索
            if (query != null && !query.trim().isEmpty()) {
                hasSearchCondition = true;
                String searchQuery = query.trim();
                
                // 提取关键词：移除常见的修饰词，保留核心关键词
                // 例如："价格实惠的酒店" -> "酒店"，"适合家庭游玩的景点" -> "景点"
                // 优化：更智能的关键词提取，支持快捷输入文本
                String[] commonModifiers = {
                    "价格", "实惠", "便宜", "经济", "适合", "家庭", "游玩", "推荐", 
                    "好的", "不错的", "一些", "一个", "找", "想", "要", "给我", 
                    "帮我", "请", "的", "了", "吗", "呢", "拍照", "当地", "特色"
                };
                
                // 如果已经通过分类识别找到了分类，关键词搜索可以更宽松
                // 如果没有分类，需要更精确的关键词匹配
                boolean hasCategoryFilter = categoryId != null;
                
                String[] keywords = searchQuery.split("的|和|或|、|，|,| ");
                
                // 构建搜索条件：使用OR连接多个关键词，每个关键词在title、description、tags中搜索
                wrapper.and(w -> {
                    boolean hasValidKeyword = false;
                    List<String> validKeywords = new ArrayList<>();
                    
                    for (String keyword : keywords) {
                        String tempKeyword = keyword.trim();
                        // 移除修饰词
                        for (String modifier : commonModifiers) {
                            tempKeyword = tempKeyword.replace(modifier, "").trim();
                        }
                        
                        // 保留有效关键词（长度>=1，因为有些产品名称可能很短）
                        if (!tempKeyword.isEmpty() && tempKeyword.length() >= 1) {
                            validKeywords.add(tempKeyword);
                        }
                    }
                    
                    // 如果有有效关键词，使用它们进行搜索
                    if (!validKeywords.isEmpty()) {
                        hasValidKeyword = true;
                        for (String validKeyword : validKeywords) {
                            final String finalKeyword = validKeyword;
                            w.or(subW -> subW.like(Product::getTitle, finalKeyword)
                                             .or()
                                             .like(Product::getDescription, finalKeyword)
                                             .or()
                                             .like(Product::getTags, finalKeyword));
                        }
                        log.info("使用提取的关键词进行搜索: {}", validKeywords);
                    }
                    
                    // 如果所有关键词都被过滤掉了，或者没有有效关键词
                    // 如果已经有分类筛选，可以不添加关键词条件（只按分类搜索）
                    // 如果没有分类筛选，使用原始查询进行模糊匹配
                    if (!hasValidKeyword) {
                        if (hasCategoryFilter) {
                            // 如果有分类筛选，即使没有关键词也可以（只按分类返回产品）
                            log.info("已通过分类筛选，关键词被过滤后为空，仅使用分类条件搜索");
                        } else {
                            // 如果没有分类筛选，尝试使用原始查询进行模糊匹配
                            if (searchQuery.length() >= 2) {
                                log.info("关键词被过滤后为空，使用原始查询进行模糊匹配: {}", searchQuery);
                                w.like(Product::getTitle, searchQuery)
                                 .or()
                                 .like(Product::getDescription, searchQuery)
                                 .or()
                                 .like(Product::getTags, searchQuery);
                            } else {
                                // 如果查询太短，不添加关键词条件，只使用其他筛选条件
                                log.warn("查询关键词太短，跳过关键词搜索: {}", searchQuery);
                            }
                        }
                    }
                });
            } else {
                // 如果没有提供query参数，只使用其他筛选条件（分类、区域、价格等）
                log.info("未提供query参数，仅使用其他筛选条件");
            }
            
            // 分类筛选
            if (categoryId != null) {
                hasSearchCondition = true;
                wrapper.eq(Product::getCategoryId, categoryId);
                log.info("添加分类筛选条件: categoryId={}", categoryId);
            }
            
            // 区域筛选
            if (region != null && !region.trim().isEmpty()) {
                hasSearchCondition = true;
                wrapper.eq(Product::getRegion, region);
                log.info("添加区域筛选条件: region={}", region);
            }
            
            // 价格范围筛选
            if (minPrice != null) {
                hasSearchCondition = true;
                wrapper.ge(Product::getPrice, minPrice);
                log.info("添加最低价格筛选条件: minPrice={}", minPrice);
            }
            if (maxPrice != null) {
                hasSearchCondition = true;
                wrapper.le(Product::getPrice, maxPrice);
                log.info("添加最高价格筛选条件: maxPrice={}", maxPrice);
            }
            
            // 如果没有任何搜索条件，返回所有上架的产品（限制数量）
            if (!hasSearchCondition) {
                log.warn("⚠️ 没有任何搜索条件，返回所有上架产品（限制{}个）", maxResults);
            }
            
            // 排序：推荐优先，然后按评分降序
            wrapper.orderByDesc(Product::getRecommend)
                   .orderByDesc(Product::getRating)
                   .orderByDesc(Product::getSales)
                   .last("LIMIT " + maxResults);
            
            // 查询产品（从product表查询，不是从product_category表）
            log.info("📊 执行产品查询（从product表），条件: query={}, categoryId={}, region={}, minPrice={}, maxPrice={}", 
                query, categoryId, region, minPrice, maxPrice);
            log.info("📊 SQL查询条件: status=1(上架), categoryId={}, region={}", categoryId, region);
            
            List<Product> products = productMapper.selectList(wrapper);
            
            log.info("📊 数据库查询结果: 从product表找到{}个产品", products.size());
            
            // 验证查询到的确实是产品数据
            if (!products.isEmpty()) {
                Product firstProduct = products.get(0);
                log.info("📊 第一个产品示例: id={}, title={}, price={}, categoryId={}, status={}", 
                    firstProduct.getId(), firstProduct.getTitle(), firstProduct.getPrice(), 
                    firstProduct.getCategoryId(), firstProduct.getStatus());
            }
            
            if (products.isEmpty()) {
                // 构建详细的错误信息
                StringBuilder errorMsg = new StringBuilder("未找到符合条件的产品");
                List<String> suggestions = new ArrayList<>();
                
                if (query != null && !query.trim().isEmpty()) {
                    suggestions.add("尝试使用更宽泛的关键词");
                }
                if (categoryId != null) {
                    suggestions.add("尝试其他分类");
                }
                if (region != null && !region.trim().isEmpty()) {
                    suggestions.add("尝试其他区域");
                }
                if (minPrice != null || maxPrice != null) {
                    suggestions.add("放宽价格范围");
                }
                
                if (suggestions.isEmpty()) {
                    errorMsg.append("。数据库中可能没有上架的产品，请联系管理员。");
                } else {
                    errorMsg.append("。建议：").append(String.join("、", suggestions));
                }
                
                log.warn("❌ 未找到符合条件的产品: {}", errorMsg.toString());
                return ToolResult.builder()
                    .success(true)
                    .data(Collections.emptyList())
                    .message(errorMsg.toString())
                    .build();
            }
            
            // 转换为简化格式（确保返回的是产品表中的真实产品数据）
            List<Map<String, Object>> result = products.stream()
                .filter(product -> {
                    // 验证：确保是真正的产品数据（有id、title、price等字段）
                    boolean isValid = product.getId() != null 
                        && product.getTitle() != null 
                        && !product.getTitle().trim().isEmpty()
                        && product.getPrice() != null;
                    
                    if (!isValid) {
                        log.warn("⚠️ 发现无效产品数据: id={}, title={}, price={}", 
                            product.getId(), product.getTitle(), product.getPrice());
                    }
                    
                    return isValid;
                })
                .map(this::convertToSimpleFormat)
                .collect(Collectors.toList());
            
            log.info("✅ 成功转换{}个产品数据（来自product表）", result.size());
            
            // 再次验证返回的数据结构
            if (!result.isEmpty()) {
                Map<String, Object> firstResult = result.get(0);
                log.info("📊 返回数据示例: id={}, title={}, price={}, category={}", 
                    firstResult.get("id"), firstResult.get("title"), 
                    firstResult.get("price"), firstResult.get("category"));
                
                // 确保返回的数据包含产品必需字段
                if (!firstResult.containsKey("id") || !firstResult.containsKey("title") || !firstResult.containsKey("price")) {
                    log.error("❌ 返回数据格式错误：缺少必需字段！");
                }
            }
            
            return ToolResult.builder()
                .success(true)
                .data(result)
                .message(String.format("找到%d个符合条件的产品（来自产品表）", result.size()))
                .build();
                
        } catch (Exception e) {
            log.error("❌ 搜索产品失败: userId={}, params={}", userId, parameters, e);
            return ToolResult.builder()
                .success(false)
                .message("搜索失败：" + e.getMessage())
                .errorCode("SEARCH_ERROR")
                .build();
        }
    }
    
    /**
     * 转换为简化格式
     * 注意：这里转换的是Product实体（来自product表），不是ProductCategory（分类表）
     */
    private Map<String, Object> convertToSimpleFormat(Product product) {
        // 验证：确保这是产品数据，不是分类数据
        if (product == null) {
            log.error("❌ 产品数据为null");
            return new HashMap<>();
        }
        
        // 产品表字段：id, title, price, description, coverImage等
        // 分类表字段：id, name, icon等（没有title、price等字段）
        // 通过检查是否有title和price字段来确认这是产品数据
        
        String description = product.getDescription();
        if (description != null && description.length() > 150) {
            description = description.substring(0, 150) + "...";
        }
        
        Map<String, Object> map = new HashMap<>();
        // 产品表的核心字段
        map.put("id", product.getId());  // 产品ID（来自product表）
        map.put("title", product.getTitle());  // 产品标题（产品表特有，分类表没有）
        map.put("price", product.getPrice());  // 产品价格（产品表特有，分类表没有）
        map.put("originalPrice", product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice());
        map.put("region", product.getRegion());  // 区域（产品表字段）
        map.put("category", getCategoryName(product.getCategoryId()));  // 分类名称（通过categoryId关联查询分类表获取名称）
        map.put("rating", product.getRating());  // 评分（产品表字段）
        map.put("sales", product.getSales());  // 销量（产品表字段）
        map.put("stock", product.getStock());  // 库存（产品表字段）
        map.put("tags", product.getTags() != null ? product.getTags() : "");
        map.put("description", description);  // 描述（产品表字段）
        map.put("coverImage", product.getCoverImage());  // 封面图（产品表字段）
        
        // 添加数据来源标识（用于调试）
        map.put("_source", "product_table");  // 标识数据来自产品表
        
        return map;
    }
    
    /**
     * 根据分类ID获取分类名称（从数据库查询）
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "其他";
        
        try {
            ProductCategory category = categoryMapper.selectById(categoryId);
            if (category != null && (category.getDeleted() == null || category.getDeleted() == 0)) {
                return category.getName();
            }
        } catch (Exception e) {
            log.warn("查询分类名称失败: categoryId={}", categoryId, e);
        }
        
        return "其他";
    }
    
    // 辅助方法：提取Long类型参数
    private Long extractLong(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
    
    // 辅助方法：提取Double类型参数
    private Double extractDouble(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }
    
    // 辅助方法：提取Integer类型参数（带默认值）
    private Integer extractInteger(Map<String, Object> params, String key, Integer defaultValue) {
        Object value = params.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        return defaultValue;
    }
}

