# LangChain4j 快速测试指南

## 🚀 1. 启动应用

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

等待应用启动，看到以下日志表示成功：

```
🤖 初始化流式聊天模型: baseUrl=https://api.deepseek.com/v1, model=deepseek-chat
💾 初始化会话记忆存储: 使用内存存储
🎯 初始化 TourismAssistant AI 服务
Started TourismApplication in X.XXX seconds
```

---

## 🧪 2. 测试新接口

### 测试 1：健康检查

```bash
curl http://localhost:8080/api/langchain/agent/health
```

**预期响应**：

```json
{
  "status": "UP",
  "service": "LangChain4j Agent Service",
  "timestamp": 1705552800000
}
```

### 测试 2：基础对话

```bash
curl -X POST "http://localhost:8080/api/langchain/agent/chat/stream" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "sessionId=test-001" \
  -d "message=你好"
```

**预期输出**（流式）：

```
event: content
data: 你好

event: content
data: ！

event: content
data: 我是

event: content
data: 景德镇

event: content
data: 文旅

event: content
data: AI

event: content
data: 智能

event: content
data: 助手

event: complete
data: 
```

### 测试 3：产品搜索

```bash
curl -X POST "http://localhost:8080/api/langchain/agent/chat/stream" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "sessionId=test-002" \
  -d "message=推荐几个景德镇的景点"
```

**观察日志**：

```
🤖 [LangChain4j] 开始对话: sessionId=test-002, userId=1
🔍 [LangChain4j] 搜索产品: query=景点
✅ 找到 5 个产品
✅ [LangChain4j] 对话完成: sessionId=test-002
```

### 测试 4：多轮对话

```bash
# 第一轮
curl -X POST "http://localhost:8080/api/langchain/agent/chat/stream" \
  -d "sessionId=test-003" \
  -d "message=推荐景点"

# 第二轮（同一会话）
curl -X POST "http://localhost:8080/api/langchain/agent/chat/stream" \
  -d "sessionId=test-003" \
  -d "message=价格怎么样？"
```

**验证**：AI 应该记住上一轮对话的内容，基于上下文回答。

---

## 📊 3. 功能验证

| 功能 | 测试命令 | 验证点 |
|------|---------|--------|
| 健康检查 | `curl .../health` | 返回 UP 状态 |
| 基础对话 | message="你好" | 友好回复 |
| 产品搜索 | message="推荐景点" | 调用 searchProducts 工具 |
| 产品详情 | message="第一个产品的详情" | 调用 getProductDetail 工具 |
| 多轮对话 | 连续提问 | 记住上下文 |
| 分类识别 | message="酒店" | 自动识别"酒店住宿"分类 |
| 价格筛选 | message="200元以下的景点" | 筛选价格 |
| 区域筛选 | message="昌江区的景点" | 筛选区域 |

---

## 🔍 4. 日志观察

### 正常流程日志

```
🤖 [LangChain4j] 开始对话: sessionId=xxx, userId=1
🔍 [LangChain4j] 搜索产品: query=景点
📊 执行产品查询（从product表），条件: query=景点, categoryId=1
✅ 找到 5 个产品
📊 第一个产品示例: id=1, title=景德镇古窑民俗博览区, price=95.0
✅ [LangChain4j] 对话完成: sessionId=xxx
```

### 工具调用日志

```
🔧 [LangChain4j] AI 请求调用工具: searchProducts
🔍 [LangChain4j] 搜索产品: query=景点
✅ 找到 5 个产品
```

### 错误日志

```
❌ [LangChain4j] 对话失败: sessionId=xxx
❌ 搜索产品失败: xxx
```

---

## 🆚 5. 对比测试

### 原接口

```bash
curl -X POST "http://localhost:8080/api/agent/chat/stream" \
  -d "sessionId=old-001" \
  -d "message=推荐景点"
```

### 新接口（LangChain4j）

```bash
curl -X POST "http://localhost:8080/api/langchain/agent/chat/stream" \
  -d "sessionId=new-001" \
  -d "message=推荐景点"
```

**对比项**：
- 响应时间
- 输出质量
- 日志清晰度
- Token 使用量

---

## 🛠️ 6. 常见问题

### Q1: 启动报错 "Bean creation exception"

**原因**：LangChain4j 依赖未正确加载

**解决**：

```bash
mvn clean install
mvn spring-boot:run
```

### Q2: API Key 错误

**原因**：DeepSeek API Key 未配置或无效

**解决**：检查 `application.yml`

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${DEEPSEEK_API_KEY:your-api-key}
```

### Q3: 工具未调用

**原因**：提示词不够明确或工具未注册

**解决**：
1. 检查 `LangChainConfig` 中是否注册了工具
2. 尝试更明确的提问，如"搜索景点门票"

### Q4: 会话不记忆

**原因**：sessionId 不一致

**解决**：确保同一对话使用相同的 sessionId

---

## 📈 7. 性能测试

### 并发测试

```bash
# 使用 ab (Apache Bench) 测试
ab -n 100 -c 10 -p post_data.txt \
  "http://localhost:8080/api/langchain/agent/chat/stream"
```

### 响应时间测试

```bash
# 测试响应时间
time curl -X POST "http://localhost:8080/api/langchain/agent/chat/stream" \
  -d "sessionId=perf-001" \
  -d "message=推荐景点"
```

---

## ✅ 8. 验收标准

- [x] 应用正常启动，无错误日志
- [x] 健康检查接口返回 UP
- [x] 基础对话正常（"你好"）
- [x] 产品搜索正常（"推荐景点"）
- [x] 工具调用正常（日志中看到工具执行）
- [x] 多轮对话正常（记住上下文）
- [x] 流式响应正常（逐字输出）
- [x] 错误处理正常（无效输入返回友好提示）

---

## 🎯 9. 下一步

1. ✅ 完成基础功能测试
2. ⏳ 前端集成测试
3. ⏳ 压力测试
4. ⏳ 生产环境部署

---

**测试完成后，请查看详细的使用指南：`LangChain4j_使用指南.md`**
