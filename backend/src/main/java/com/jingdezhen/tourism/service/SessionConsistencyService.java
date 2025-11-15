package com.jingdezhen.tourism.service;

import com.alibaba.fastjson2.JSON;
import com.jingdezhen.tourism.agent.core.ConversationContext;
import com.jingdezhen.tourism.config.RedisConfig;
import com.jingdezhen.tourism.entity.AiRecommendation;
import com.jingdezhen.tourism.mapper.AiRecommendationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 会话一致性服务
 * 负责维护Redis会话和数据库推荐历史之间的数据一致性
 * 
 * @author AI Assistant
 */
@Slf4j
@Service
public class SessionConsistencyService {
    
    @Autowired(required = false)
    private RedisSessionManager sessionManager;
    
    @Autowired(required = false)
    private AiRecommendationMapper aiRecommendationMapper;
    
    
    /**
     * 保存推荐记录ID到会话上下文中
     * 用于建立Redis会话和数据库记录之间的关联
     */
    private static final String RECOMMENDATION_ID_KEY = "lastRecommendationId";
    
    /**
     * 保存推荐记录ID列表到会话上下文中
     * 用于记录一个会话中的所有推荐记录
     */
    private static final String RECOMMENDATION_IDS_KEY = "recommendationIds";
    
    // 使用RedisConfig中的常量，不再重复定义
    
    /**
     * 随机过期时间范围（秒）
     * 最小：30分钟，最大：2小时
     */
    private static final long MIN_EXPIRE_SECONDS = 30 * 60; // 30分钟
    private static final long MAX_EXPIRE_SECONDS = 2 * 60 * 60; // 2小时
    
    /**
     * 保存推荐历史到数据库，并建立与Redis会话的关联
     * 
     * @param context 会话上下文
     * @param userQuery 用户查询
     * @param aiResponse AI回复
     * @param productIds 推荐的产品ID列表
     * @return 保存的推荐记录ID，如果保存失败返回null
     */
    public Long saveRecommendationWithConsistency(ConversationContext context, 
                                                  String userQuery, 
                                                  String aiResponse, 
                                                  List<Long> productIds) {
        if (aiRecommendationMapper == null) {
            log.debug("AiRecommendationMapper未注入，跳过保存推荐历史");
            return null;
        }
        
        try {
            // 创建推荐记录
            AiRecommendation recommendation = new AiRecommendation();
            recommendation.setUserId(context.getUserId());
            recommendation.setQuery(userQuery);
            recommendation.setContext("会话ID: " + context.getSessionId());
            recommendation.setResponse(aiResponse);
            recommendation.setRecommendedProducts(JSON.toJSONString(productIds));
            recommendation.setFeedback(null);
            
            // 保存到数据库
            aiRecommendationMapper.insert(recommendation);
            Long recommendationId = recommendation.getId();
            
            // 将推荐记录ID保存到会话上下文中，建立关联
            context.setVariable(RECOMMENDATION_ID_KEY, recommendationId);
            
            // 建立推荐记录ID到会话ID的映射（用于快速查找）
            if (sessionManager != null) {
                try {
                    // 保存映射关系：推荐记录ID -> 会话ID
                    String mappingKey = RedisConfig.KeyPrefix.RECOMMENDATION_SESSION_MAPPING + recommendationId;
                    // 映射关系使用较长的过期时间（7天），因为历史记录需要长期保存
                    StringRedisTemplate redisTemplate = sessionManager.getRedisTemplate();
                    if (redisTemplate != null) {
                        redisTemplate.opsForValue().set(
                            mappingKey, 
                            context.getSessionId(), 
                            RedisConfig.ExpireTime.RECOMMENDATION_MAPPING, 
                            java.util.concurrent.TimeUnit.SECONDS
                        );
                    }
                    log.info("✅ 已建立推荐记录到会话的映射: recommendationId={} -> sessionId={}", 
                        recommendationId, context.getSessionId());
                } catch (Exception e) {
                    log.warn("⚠️ 建立推荐记录映射失败（不影响主流程）: recommendationId={}", recommendationId, e);
                }
                
                // 更新Redis会话（包含推荐记录ID）
                sessionManager.saveSession(context.getSessionId(), context);
                log.info("✅ 推荐历史已保存并建立关联: recommendationId={}, sessionId={}", 
                    recommendationId, context.getSessionId());
            }
            
            return recommendationId;
            
        } catch (Exception e) {
            log.error("❌ 保存推荐历史失败: userId={}, sessionId={}", 
                context.getUserId(), context.getSessionId(), e);
            return null;
        }
    }
    
    /**
     * 异步保存推荐历史（不阻塞主流程）
     * 
     * @param context 会话上下文
     * @param userQuery 用户查询
     * @param aiResponse AI回复
     * @param productIds 推荐的产品ID列表
     * @return CompletableFuture，包含保存的推荐记录ID
     */
    public CompletableFuture<Long> saveRecommendationAsync(ConversationContext context, 
                                                           String userQuery, 
                                                           String aiResponse, 
                                                           List<Long> productIds) {
        return CompletableFuture.supplyAsync(() -> {
            return saveRecommendationWithConsistency(context, userQuery, aiResponse, productIds);
        });
    }
    
    /**
     * 根据推荐记录ID恢复会话（缓存回填策略）
     * 1. 先从Redis查找会话（通过推荐记录ID映射找到会话ID，再获取会话）
     * 2. 如果Redis中没有，从MySQL数据库查找推荐记录
     * 3. 如果从数据库查找到了，恢复会话并保存到Redis，使用随机过期时间
     * 
     * @param recommendationId 推荐记录ID
     * @param userId 用户ID
     * @return 恢复的会话上下文，如果恢复失败返回null
     */
    public ConversationContext restoreSessionByRecommendationId(Long recommendationId, Long userId) {
        if (sessionManager == null || aiRecommendationMapper == null) {
            log.warn("SessionManager或AiRecommendationMapper未注入，无法恢复会话");
            return null;
        }
        
        try {
            // 第一步：尝试从Redis查找会话
            StringRedisTemplate redisTemplate = sessionManager != null ? sessionManager.getRedisTemplate() : null;
            if (redisTemplate == null) {
                log.info("ℹ️ Redis未配置，将从数据库恢复: recommendationId={}", recommendationId);
            } else {
                String mappingKey = RedisConfig.KeyPrefix.RECOMMENDATION_SESSION_MAPPING + recommendationId;
                String sessionId = redisTemplate.opsForValue().get(mappingKey);
            
                if (sessionId != null && !sessionId.isEmpty()) {
                    // 找到了映射关系，尝试从Redis获取会话
                    ConversationContext context = sessionManager.getSession(sessionId);
                    if (context != null && context.getUserId().equals(userId)) {
                        log.info("✅ 从Redis找到会话: recommendationId={}, sessionId={}", recommendationId, sessionId);
                        return context;
                    } else {
                        log.info("ℹ️ Redis中的会话已过期或不存在，将从数据库恢复: recommendationId={}, sessionId={}", 
                            recommendationId, sessionId);
                    }
                } else {
                    log.info("ℹ️ Redis中没有找到推荐记录的映射关系，将从数据库恢复: recommendationId={}", recommendationId);
                }
            }
            
            // 第二步：从MySQL数据库查找推荐记录
            AiRecommendation recommendation = aiRecommendationMapper.selectById(recommendationId);
            if (recommendation == null || !recommendation.getUserId().equals(userId)) {
                log.warn("推荐记录不存在或不属于当前用户: recommendationId={}, userId={}", 
                    recommendationId, userId);
                return null;
            }
            
            // 第三步：从数据库恢复会话上下文
            ConversationContext context = new ConversationContext();
            // 生成新的会话ID（格式：restored_{recommendationId}_{timestamp}）
            String newSessionId = "restored_" + recommendationId + "_" + System.currentTimeMillis();
            context.setSessionId(newSessionId);
            context.setUserId(userId);
            context.setHistory(new ArrayList<>());
            context.setVariables(new java.util.HashMap<>());
            context.setCreateTime(recommendation.getCreateTime());
            context.setLastActiveTime(LocalDateTime.now());
            
            // 恢复对话历史
            context.addMessage(ConversationContext.Message.user(recommendation.getQuery()));
            context.addMessage(ConversationContext.Message.assistant(recommendation.getResponse()));
            
            // 保存推荐记录ID到上下文
            context.setVariable(RECOMMENDATION_ID_KEY, recommendationId);
            List<Long> recommendationIds = new ArrayList<>();
            recommendationIds.add(recommendationId);
            context.setVariable(RECOMMENDATION_IDS_KEY, recommendationIds);
            
            // 第四步：保存到Redis，使用随机过期时间（防止缓存雪崩）
            long randomExpireSeconds = generateRandomExpireTime();
            try {
                if (sessionManager != null) {
                    StringRedisTemplate redisTemplateForSave = sessionManager.getRedisTemplate();
                    if (redisTemplateForSave != null) {
                        // 保存会话到Redis
                        redisTemplateForSave.opsForValue().set(
                            sessionManager.getSessionKey(newSessionId),
                            com.alibaba.fastjson2.JSON.toJSONString(context),
                            randomExpireSeconds,
                            java.util.concurrent.TimeUnit.SECONDS
                        );
                        
                        // 建立推荐记录ID到会话ID的映射（使用较长的过期时间）
                        String mappingKey = RedisConfig.KeyPrefix.RECOMMENDATION_SESSION_MAPPING + recommendationId;
                        redisTemplateForSave.opsForValue().set(
                            mappingKey,
                            newSessionId,
                            RedisConfig.ExpireTime.RECOMMENDATION_MAPPING,
                            java.util.concurrent.TimeUnit.SECONDS
                        );
                    }
                }
                
                log.info("✅ 从数据库恢复会话并保存到Redis: recommendationId={}, sessionId={}, 过期时间={}秒 ({}分钟)", 
                    recommendationId, newSessionId, randomExpireSeconds, randomExpireSeconds / 60);
                
            } catch (Exception e) {
                log.error("❌ 保存恢复的会话到Redis失败: recommendationId={}, sessionId={}", 
                    recommendationId, newSessionId, e);
                // 即使Redis保存失败，也返回恢复的上下文（至少可以在内存中使用）
            }
            
            return context;
            
        } catch (Exception e) {
            log.error("❌ 根据推荐记录ID恢复会话失败: recommendationId={}, userId={}", 
                recommendationId, userId, e);
            return null;
        }
    }
    
    /**
     * 生成随机过期时间（防止缓存雪崩）
     * 在最小和最大过期时间之间随机选择
     * 
     * @return 随机过期时间（秒）
     */
    private long generateRandomExpireTime() {
        long range = MAX_EXPIRE_SECONDS - MIN_EXPIRE_SECONDS;
        long randomOffset = (long) (Math.random() * range);
        return MIN_EXPIRE_SECONDS + randomOffset;
    }
    
    /**
     * 从数据库恢复会话（当Redis会话过期时）
     * 根据推荐记录ID恢复部分对话历史
     * 
     * @param recommendationId 推荐记录ID
     * @param userId 用户ID
     * @return 恢复的会话上下文，如果恢复失败返回null
     * @deprecated 使用 restoreSessionByRecommendationId 代替
     */
    @Deprecated
    public ConversationContext restoreSessionFromDatabase(Long recommendationId, Long userId) {
        return restoreSessionByRecommendationId(recommendationId, userId);
    }
    
    /**
     * 检查并修复数据一致性
     * 如果Redis会话存在但数据库中没有对应的推荐记录，尝试修复
     * 
     * @param context 会话上下文
     * @return 是否一致（true表示一致，false表示不一致）
     */
    public boolean checkAndFixConsistency(ConversationContext context) {
        try {
            if (aiRecommendationMapper == null) {
                // 如果没有Mapper，无法检查一致性
                return true;
            }
            
            // 检查会话中是否有推荐记录ID列表
            @SuppressWarnings("unchecked")
            List<Long> recommendationIds = (List<Long>) context.getVariable(RECOMMENDATION_IDS_KEY);
            if (recommendationIds == null || recommendationIds.isEmpty()) {
                // 没有推荐记录ID，说明可能还没有保存过推荐历史，这是正常的
                return true;
            }
            
            // 检查每个推荐记录ID是否在数据库中存在
            List<Long> missingIds = new ArrayList<>();
            for (Long recommendationId : recommendationIds) {
                AiRecommendation recommendation = aiRecommendationMapper.selectById(recommendationId);
                if (recommendation == null) {
                    missingIds.add(recommendationId);
                }
            }
            
            if (!missingIds.isEmpty()) {
                log.warn("⚠️ 数据不一致：Redis会话中有{}个推荐记录ID在数据库中不存在: {}", 
                    missingIds.size(), missingIds);
                
                // 从列表中移除不存在的ID
                recommendationIds.removeAll(missingIds);
                if (recommendationIds.isEmpty()) {
                    context.setVariable(RECOMMENDATION_IDS_KEY, null);
                    context.setVariable(RECOMMENDATION_ID_KEY, null);
                } else {
                    context.setVariable(RECOMMENDATION_IDS_KEY, recommendationIds);
                    // 更新最后一条推荐记录ID
                    context.setVariable(RECOMMENDATION_ID_KEY, recommendationIds.get(recommendationIds.size() - 1));
                }
                
                // 更新Redis会话
                if (sessionManager != null) {
                    try {
                        sessionManager.saveSession(context.getSessionId(), context);
                        log.info("✅ 已修复数据不一致，移除了{}个不存在的推荐记录ID", missingIds.size());
                    } catch (Exception e) {
                        log.error("❌ 修复数据不一致后更新Redis失败: sessionId={}", context.getSessionId(), e);
                    }
                }
                
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ 检查数据一致性失败: sessionId={}", context.getSessionId(), e);
            return false;
        }
    }
    
    /**
     * 补偿机制：当Redis保存失败时，尝试从数据库恢复
     * 
     * @param recommendationId 推荐记录ID
     * @param context 会话上下文
     * @return 是否恢复成功
     */
    public boolean compensateFromDatabase(Long recommendationId, ConversationContext context) {
        try {
            if (aiRecommendationMapper == null) {
                return false;
            }
            
            AiRecommendation recommendation = aiRecommendationMapper.selectById(recommendationId);
            if (recommendation == null) {
                log.warn("补偿机制：推荐记录不存在: recommendationId={}", recommendationId);
                return false;
            }
            
            // 将推荐记录ID添加到会话上下文中
            context.setVariable(RECOMMENDATION_ID_KEY, recommendationId);
            
            @SuppressWarnings("unchecked")
            List<Long> recommendationIds = (List<Long>) context.getVariable(RECOMMENDATION_IDS_KEY);
            if (recommendationIds == null) {
                recommendationIds = new ArrayList<>();
            }
            if (!recommendationIds.contains(recommendationId)) {
                recommendationIds.add(recommendationId);
            }
            context.setVariable(RECOMMENDATION_IDS_KEY, recommendationIds);
            
            // 尝试再次保存到Redis
            if (sessionManager != null) {
                sessionManager.saveSession(context.getSessionId(), context);
                log.info("✅ 补偿机制：已从数据库恢复并更新Redis: recommendationId={}, sessionId={}", 
                    recommendationId, context.getSessionId());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("❌ 补偿机制失败: recommendationId={}, sessionId={}", 
                recommendationId, context.getSessionId(), e);
            return false;
        }
    }
    
    /**
     * 同步保存推荐历史（确保数据一致性）
     * 先保存到数据库，成功后再更新Redis会话
     * 
     * @param context 会话上下文
     * @param userQuery 用户查询
     * @param aiResponse AI回复
     * @param productIds 推荐的产品ID列表
     * @return 保存的推荐记录ID，如果保存失败返回null
     */
    public Long saveRecommendationSync(ConversationContext context, 
                                       String userQuery, 
                                       String aiResponse, 
                                       List<Long> productIds) {
        if (aiRecommendationMapper == null) {
            log.debug("AiRecommendationMapper未注入，跳过保存推荐历史");
            return null;
        }
        
        try {
            // 1. 先保存到数据库
            AiRecommendation recommendation = new AiRecommendation();
            recommendation.setUserId(context.getUserId());
            recommendation.setQuery(userQuery);
            recommendation.setContext("会话ID: " + context.getSessionId());
            recommendation.setResponse(aiResponse);
            recommendation.setRecommendedProducts(JSON.toJSONString(productIds));
            recommendation.setFeedback(null);
            
            aiRecommendationMapper.insert(recommendation);
            Long recommendationId = recommendation.getId();
            
            log.info("✅ 推荐历史已保存到数据库: recommendationId={}, userId={}, query={}, productCount={}", 
                recommendationId, context.getUserId(), userQuery, productIds.size());
            
            // 2. 将推荐记录ID保存到会话上下文中
            context.setVariable(RECOMMENDATION_ID_KEY, recommendationId);
            
            // 3. 更新Redis会话（包含推荐记录ID）
            if (sessionManager != null) {
                sessionManager.saveSession(context.getSessionId(), context);
                log.info("✅ Redis会话已更新，包含推荐记录ID: recommendationId={}, sessionId={}", 
                    recommendationId, context.getSessionId());
            }
            
            return recommendationId;
            
        } catch (Exception e) {
            log.error("❌ 同步保存推荐历史失败: userId={}, sessionId={}", 
                context.getUserId(), context.getSessionId(), e);
            return null;
        }
    }
}

