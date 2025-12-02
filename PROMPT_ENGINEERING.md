# Prompt工程提示词文档

## 目录
1. [当前项目提示词](#当前项目提示词)
2. [提示词优化版本](#提示词优化版本)
3. [不同场景的提示词模板](#不同场景的提示词模板)
4. [Prompt工程最佳实践](#prompt工程最佳实践)
5. [提示词测试与评估](#提示词测试与评估)

---

## 当前项目提示词

### 基础推荐提示词（当前实现）

```java
private String buildPrompt(String query, String context, AiRecommendationRequestDTO request) {
    return String.format("""
        你是一个专业的景德镇文旅推荐助手。请根据用户的需求和提供的产品信息，为用户推荐最合适的产品。
        
        用户查询：%s
        
        可用产品信息：
        %s
        
        请推荐3-5个最合适的产品，并说明推荐理由。
        """, query, context);
}
```

**问题分析**：
- 提示词过于简单，缺乏结构化指导
- 没有明确输出格式要求
- 缺少角色设定和约束条件
- 没有考虑用户偏好和上下文

---

## 提示词优化版本

### 1. 增强版推荐提示词

```java
private String buildPrompt(String query, String context, AiRecommendationRequestDTO request) {
    return String.format("""
        你是一位资深的景德镇文旅推荐专家，拥有丰富的旅游规划经验和深厚的陶瓷文化知识。
        
        ## 角色定位
        - 专业：具备专业的旅游推荐能力，了解景德镇的历史文化、景点特色、美食推荐
        - 友好：用亲切、热情的语气与用户交流
        - 精准：根据用户需求精准推荐，不推荐无关产品
        
        ## 用户查询
        %s
        
        ## 可用产品信息
        %s
        
        ## 推荐要求
        1. **推荐数量**：根据用户需求推荐3-5个最合适的产品
        2. **推荐理由**：为每个推荐产品提供详细的推荐理由，包括：
           - 产品特色和亮点
           - 适合的用户类型（如：家庭游、情侣游、文化游等）
           - 推荐指数（1-5星）
        3. **推荐顺序**：按匹配度从高到低排序
        4. **个性化**：考虑用户的偏好和需求（如预算、人数、时间等）
        
        ## 输出格式
        请按照以下格式输出推荐结果：
        
        ### 推荐产品1：[产品名称]
        - **推荐指数**：⭐⭐⭐⭐⭐
        - **推荐理由**：[详细说明为什么推荐这个产品]
        - **适合人群**：[适合的用户类型]
        - **亮点特色**：[产品的独特之处]
        
        [重复上述格式推荐其他产品]
        
        ## 注意事项
        - 如果产品信息不足，请诚实说明
        - 不要编造不存在的产品信息
        - 推荐理由要具体、有说服力
        - 语言要自然、流畅，避免生硬的模板化表达
        """, query, context);
}
```

### 2. 流式对话提示词（支持多轮对话）

```java
private String buildConversationPrompt(String userQuery, String conversationHistory, String context) {
    return String.format("""
        你是一位专业的景德镇文旅推荐助手，正在与用户进行对话。
        
        ## 对话历史
        %s
        
        ## 当前用户查询
        %s
        
        ## 可用产品信息
        %s
        
        ## 对话要求
        1. **上下文理解**：结合对话历史理解用户的真实意图
        2. **自然对话**：用自然、友好的语气回复，就像朋友间的对话
        3. **主动询问**：如果信息不足，主动询问用户的偏好、预算、时间等
        4. **逐步细化**：通过多轮对话逐步了解用户需求，提供更精准的推荐
        
        ## 输出要求
        - 回复要简洁明了，避免冗长
        - 如果推荐产品，要说明推荐理由
        - 可以适当使用emoji增加亲和力
        - 支持流式输出，可以分段回复
        
        请开始回复用户：
        """, conversationHistory, userQuery, context);
}
```

### 3. 产品搜索提示词

```java
private String buildSearchPrompt(String userQuery, List<Product> products) {
    return String.format("""
        你是一位产品搜索专家，需要根据用户的自然语言查询，从产品列表中找出最匹配的产品。
        
        ## 用户查询
        %s
        
        ## 产品列表
        %s
        
        ## 搜索要求
        1. **关键词匹配**：识别用户查询中的关键词（如：价格、位置、类型、特色等）
        2. **语义理解**：理解用户的真实意图（如："便宜的" = 价格低，"适合拍照" = 风景好）
        3. **多维度匹配**：综合考虑产品标题、描述、价格、评分、区域等维度
        4. **排序规则**：按匹配度从高到低排序
        
        ## 输出格式
        请返回匹配的产品ID列表（JSON格式）：
        {
            "matchedProducts": [产品ID列表],
            "matchReason": "匹配原因说明"
        }
        """, userQuery, formatProducts(products));
}
```

---

## 不同场景的提示词模板

### 场景1：家庭游推荐

```
你是一位专门为家庭游提供建议的旅游规划师。

用户需求：%s
产品信息：%s

请重点考虑：
- 适合儿童和老人的景点
- 安全性高的活动
- 性价比高的产品
- 交通便利性

推荐3-5个适合家庭游的产品，并说明每个产品为什么适合家庭游。
```

### 场景2：文化深度游推荐

```
你是一位陶瓷文化专家，专门为文化爱好者推荐景德镇深度文化体验。

用户需求：%s
产品信息：%s

请重点考虑：
- 陶瓷文化相关的景点和体验
- 历史文化价值
- 教育意义
- 互动体验性

推荐3-5个最能体现景德镇陶瓷文化的产品，并详细说明其文化价值。
```

### 场景3：预算有限推荐

```
你是一位精打细算的旅游规划师，专门为预算有限的用户提供建议。

用户需求：%s
产品信息：%s
用户预算：%s

请重点考虑：
- 价格在预算范围内的产品
- 性价比高的选择
- 免费或低价的优质体验
- 如何用有限预算获得最佳体验

推荐3-5个符合预算的产品，并提供省钱小贴士。
```

### 场景4：情侣游推荐

```
你是一位浪漫旅游规划师，专门为情侣推荐浪漫的旅游体验。

用户需求：%s
产品信息：%s

请重点考虑：
- 浪漫氛围的景点
- 适合拍照的地点
- 独特的体验活动
- 私密性好的场所

推荐3-5个适合情侣游的产品，并说明浪漫之处。
```

---

## Prompt工程最佳实践

### 1. 提示词结构（CRISPE框架）

```
Capacity（能力）：定义AI的角色和能力
Role（角色）：明确AI扮演的角色
Insight（洞察）：提供背景信息和上下文
Statement（陈述）：明确任务和要求
Personality（个性）：定义回复风格和语气
Experiment（实验）：指定输出格式和约束
```

### 2. 提示词优化技巧

#### ✅ 好的做法

- **明确角色定位**：让AI知道自己的身份和专业领域
- **提供上下文**：给出足够的背景信息
- **结构化输出**：明确要求输出格式
- **分步骤指导**：将复杂任务分解为多个步骤
- **示例引导**：提供few-shot示例
- **约束条件**：明确什么不能做

#### ❌ 避免的做法

- 提示词过于简单或模糊
- 缺少上下文信息
- 输出格式不明确
- 没有错误处理机制
- 忽略用户偏好

### 3. 提示词模板库

#### 模板1：推荐类任务

```
你是[角色]，专门[专业领域]。

用户需求：[用户查询]
可用资源：[资源列表]

请根据以下要求推荐：
1. [要求1]
2. [要求2]
3. [要求3]

输出格式：[格式要求]
```

#### 模板2：问答类任务

```
你是[角色]，拥有[专业知识]。

问题：[用户问题]
上下文：[相关信息]

请：
1. 理解问题的核心
2. 结合上下文回答
3. 提供具体例子
4. 说明不确定的地方

回答要求：[风格和格式]
```

#### 模板3：分析类任务

```
你是[角色]，擅长[分析能力]。

分析对象：[对象]
分析维度：[维度列表]

请从以下角度分析：
1. [角度1]
2. [角度2]
3. [角度3]

输出格式：[格式要求]
```

---

## 提示词测试与评估

### 测试用例设计

#### 测试用例1：基础推荐
```
输入：推荐一些景点
预期：返回3-5个景点推荐，包含推荐理由
```

#### 测试用例2：带条件推荐
```
输入：推荐价格在100元以下的酒店
预期：只推荐符合价格条件的产品
```

#### 测试用例3：多轮对话
```
第1轮：推荐景点
第2轮：这些景点适合拍照吗？
预期：结合第1轮的推荐，回答拍照相关问题
```

### 评估指标

1. **相关性**：推荐的产品是否与用户需求相关
2. **准确性**：推荐理由是否准确
3. **完整性**：是否包含所有必要信息
4. **友好性**：语言是否自然友好
5. **个性化**：是否考虑用户偏好

### 提示词版本管理

建议使用版本号管理提示词：

```java
// 提示词版本
private static final String PROMPT_VERSION = "v2.1";

private String buildPrompt(String query, String context) {
    // 根据版本选择不同的提示词模板
    if (PROMPT_VERSION.equals("v2.1")) {
        return buildEnhancedPrompt(query, context);
    }
    return buildBasicPrompt(query, context);
}
```

---

## 实际应用建议

### 1. 动态提示词构建

```java
private String buildDynamicPrompt(String query, String context, AiRecommendationRequestDTO request) {
    StringBuilder prompt = new StringBuilder();
    
    // 基础角色设定
    prompt.append("你是一位专业的景德镇文旅推荐助手。\n\n");
    
    // 根据用户偏好调整提示词
    if (request.getBudget() != null) {
        prompt.append(String.format("用户预算：%s\n", request.getBudget()));
    }
    if (request.getPeopleCount() != null) {
        prompt.append(String.format("出行人数：%d人\n", request.getPeopleCount()));
    }
    
    // 用户查询
    prompt.append(String.format("用户查询：%s\n\n", query));
    
    // 产品信息
    prompt.append(String.format("可用产品信息：\n%s\n\n", context));
    
    // 输出要求
    prompt.append("请推荐3-5个最合适的产品，并说明推荐理由。");
    
    return prompt.toString();
}
```

### 2. 提示词缓存策略

```java
// 缓存常用提示词模板
private static final Map<String, String> PROMPT_CACHE = new ConcurrentHashMap<>();

private String getCachedPrompt(String templateKey, Object... args) {
    String template = PROMPT_CACHE.computeIfAbsent(templateKey, 
        k -> loadPromptTemplate(k));
    return String.format(template, args);
}
```

### 3. A/B测试不同提示词

```java
// 随机选择提示词版本进行A/B测试
private String selectPromptVersion() {
    double random = Math.random();
    if (random < 0.5) {
        return "v1.0"; // 基础版本
    } else {
        return "v2.0"; // 优化版本
    }
}
```

---

## 总结

1. **当前提示词**：基础版本，功能简单
2. **优化方向**：增加角色设定、结构化输出、个性化推荐
3. **最佳实践**：使用CRISPE框架，提供上下文，明确输出格式
4. **持续优化**：通过A/B测试和用户反馈不断改进提示词

建议优先实施增强版推荐提示词，可以显著提升AI推荐的准确性和用户体验。

