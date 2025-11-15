# MCP Server 快速开始指南

## 什么是MCP Server？

MCP (Model Context Protocol) Server 是一个标准化的协议服务器，允许AI模型（如Claude、GPT等）通过标准化的工具接口访问你的数据和服务。

本项目实现了一个专门用于景德镇旅游推荐的MCP Server，提供4个核心工具。

## 快速开始

### 1. 启动项目

```bash
cd backend
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

### 2. 验证服务

```bash
# 检查健康状态
curl http://localhost:8080/api/mcp/health

# 查看服务器信息
curl http://localhost:8080/api/mcp/info

# 列出所有工具
curl http://localhost:8080/api/mcp/tools
```

### 3. 测试工具调用

#### 示例1: 搜索景点

```bash
curl -X POST http://localhost:8080/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "search_attractions",
      "arguments": {
        "keyword": "陶瓷",
        "minRating": 4.0,
        "page": 1,
        "pageSize": 5
      }
    }
  }'
```

#### 示例2: 生成3天旅游行程

```bash
curl -X POST http://localhost:8080/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "recommend_daily_plan",
      "arguments": {
        "days": 3,
        "budget": 2000,
        "interests": "陶瓷文化,历史古迹",
        "pace": "moderate",
        "startDate": "2025-12-01"
      }
    }
  }'
```

#### 示例3: 查找酒店

```bash
curl -X POST http://localhost:8080/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "find_accommodations",
      "arguments": {
        "region": "昌江区",
        "checkInDate": "2025-12-01",
        "checkOutDate": "2025-12-04",
        "maxPrice": 300,
        "minRating": 4.0
      }
    }
  }'
```

#### 示例4: 计算旅行预算

```bash
curl -X POST http://localhost:8080/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "4",
    "method": "tools/call",
    "params": {
      "name": "get_travel_budget",
      "arguments": {
        "days": 3,
        "people": 2,
        "accommodationLevel": "standard",
        "mealLevel": "standard",
        "includeTransport": true,
        "transportType": "train"
      }
    }
  }'
```

## 集成到AI应用

### 方式1: 使用HTTP API直接调用

适合任何编程语言，通过HTTP请求调用MCP工具。

**Python示例:**

```python
import requests
import json

def call_mcp_tool(tool_name, arguments):
    url = "http://localhost:8080/api/mcp/message"
    payload = {
        "jsonrpc": "2.0",
        "id": "1",
        "method": "tools/call",
        "params": {
            "name": tool_name,
            "arguments": arguments
        }
    }
    
    response = requests.post(url, json=payload)
    return response.json()

# 搜索景点
result = call_mcp_tool("search_attractions", {
    "keyword": "古窑",
    "minRating": 4.5
})

print(json.dumps(result, indent=2, ensure_ascii=False))
```

**JavaScript示例:**

```javascript
async function callMcpTool(toolName, args) {
  const response = await fetch('http://localhost:8080/api/mcp/message', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      jsonrpc: '2.0',
      id: '1',
      method: 'tools/call',
      params: {
        name: toolName,
        arguments: args
      }
    })
  });
  
  return await response.json();
}

// 使用
const result = await callMcpTool('search_attractions', {
  keyword: '陶瓷博物馆'
});
console.log(result);
```

### 方式2: 集成到Claude Desktop

在Claude Desktop的配置文件中添加：

**位置**: `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS)

```json
{
  "mcpServers": {
    "jingdezhen-tourism": {
      "command": "curl",
      "args": [
        "-X", "POST",
        "http://localhost:8080/api/mcp/message",
        "-H", "Content-Type: application/json",
        "-d", "@-"
      ]
    }
  }
}
```

重启Claude Desktop后，你可以直接在对话中使用：

```
帮我搜索景德镇评分4星以上的陶瓷相关景点

帮我规划一个3天2夜的景德镇之旅，预算3000元，我对陶瓷文化和历史古迹感兴趣

帮我找一个昌江区300元以下的酒店，12月1日入住，12月3日退房

帮我计算2个人去景德镇玩3天需要多少钱
```

### 方式3: 集成到LangChain

```python
from langchain.tools import Tool
from langchain.agents import initialize_agent, AgentType
from langchain.chat_models import ChatOpenAI
import requests

class JingdezhenMcpClient:
    def __init__(self, base_url="http://localhost:8080/api/mcp"):
        self.base_url = base_url
        self.request_id = 0
    
    def call_tool(self, tool_name, arguments):
        self.request_id += 1
        response = requests.post(
            f"{self.base_url}/message",
            json={
                "jsonrpc": "2.0",
                "id": str(self.request_id),
                "method": "tools/call",
                "params": {
                    "name": tool_name,
                    "arguments": arguments
                }
            }
        )
        return response.json()

# 创建MCP客户端
mcp_client = JingdezhenMcpClient()

# 定义工具
tools = [
    Tool(
        name="search_attractions",
        func=lambda query: str(mcp_client.call_tool("search_attractions", {"keyword": query})),
        description="搜索景德镇的景点，输入关键词即可"
    ),
    Tool(
        name="recommend_plan",
        func=lambda input: str(mcp_client.call_tool("recommend_daily_plan", eval(input))),
        description="生成旅游行程，输入格式：{'days': 3, 'budget': 2000}"
    ),
    Tool(
        name="find_hotels",
        func=lambda input: str(mcp_client.call_tool("find_accommodations", eval(input))),
        description="查找酒店，输入格式：{'region': '昌江区', 'maxPrice': 300}"
    ),
    Tool(
        name="calculate_budget",
        func=lambda input: str(mcp_client.call_tool("get_travel_budget", eval(input))),
        description="计算旅行预算，输入格式：{'days': 3, 'people': 2}"
    )
]

# 初始化Agent
llm = ChatOpenAI(temperature=0)
agent = initialize_agent(
    tools,
    llm,
    agent=AgentType.ZERO_SHOT_REACT_DESCRIPTION,
    verbose=True
)

# 使用Agent
response = agent.run("帮我规划一个3天的景德镇之旅，预算3000元，我想了解陶瓷文化")
print(response)
```

## 工具详细说明

### search_attractions - 景点搜索

**功能**: 根据条件搜索景点

**参数**:
- `keyword`: 搜索关键词（如"陶瓷"、"古窑"）
- `region`: 区域筛选（如"昌江区"）
- `minPrice` / `maxPrice`: 价格范围
- `minRating`: 最低评分(1-5)
- `sortBy`: 排序方式
  - `price_asc`: 价格从低到高
  - `price_desc`: 价格从高到低
  - `rating_desc`: 评分从高到低
  - `default`: 默认排序
- `page` / `pageSize`: 分页参数

**返回**: 景点列表及总数、分页信息

### recommend_daily_plan - 每日行程推荐

**功能**: 生成多日旅游行程计划

**参数**:
- `days`: 旅游天数（必填）
- `budget`: 总预算（可选）
- `interests`: 兴趣偏好，逗号分隔（如"陶瓷文化,历史古迹"）
- `pace`: 旅游节奏
  - `relaxed`: 轻松（每天2个景点）
  - `moderate`: 适中（每天3个景点）
  - `intense`: 紧凑（每天4个景点）
- `startDate`: 开始日期（YYYY-MM-DD格式）

**返回**: 每日详细行程，包括景点、餐饮、住宿安排和费用估算

### find_accommodations - 住宿推荐

**功能**: 搜索酒店住宿

**参数**:
- `region`: 区域（如"昌江区"）
- `checkInDate` / `checkOutDate`: 入住/退房日期
- `minPrice` / `maxPrice`: 每晚价格范围
- `minRating`: 最低评分
- `facilities`: 设施要求（如"免费WiFi,停车场,早餐"）
- `hotelType`: 酒店类型（如"经济型"、"舒适型"）
- `sortBy`: 排序方式

**返回**: 酒店列表及总价（如果提供了入住天数）

### get_travel_budget - 旅行预算计算

**功能**: 计算详细预算清单

**参数**:
- `days`: 旅游天数（必填）
- `people`: 旅游人数（默认1）
- `accommodationLevel`: 住宿标准
  - `budget`: 经济型(150元/晚)
  - `standard`: 标准型(250元/晚)
  - `comfort`: 舒适型(400元/晚)
  - `luxury`: 豪华型(800元/晚)
- `mealLevel`: 用餐标准
  - `budget`: 经济(60元/人/天)
  - `standard`: 标准(120元/人/天)
  - `premium`: 高档(200元/人/天)
- `includeTransport`: 是否包含往返交通
- `transportType`: 交通方式（train/plane/car/bus）
- `departureCity`: 出发城市
- `shoppingBudget`: 购物预算（元/人）
- `includeShopping`: 是否包含购物

**返回**: 详细预算清单，包括各项费用、总费用、人均费用等

## 常见问题

### Q1: 如何确认服务已正常启动？

```bash
curl http://localhost:8080/api/mcp/health
```

应该返回:
```json
{
  "status": "UP",
  "timestamp": 1234567890,
  "toolsRegistered": 4
}
```

### Q2: 工具返回空结果怎么办？

可能原因：
1. 数据库中没有匹配的数据 - 需要先运行测试数据SQL脚本
2. 搜索条件太严格 - 放宽筛选条件
3. 数据库连接失败 - 检查application.yml配置

### Q3: 如何添加自定义工具？

1. 在 `mcp/tool/impl/` 下创建新的工具类
2. 继承 `AbstractMcpTool`
3. 实现必要的方法
4. 添加 `@Component` 注解
5. Spring会自动注册

示例：
```java
@Component
public class MyCustomTool extends AbstractMcpTool {
    @Override
    public String getName() {
        return "my_custom_tool";
    }
    
    @Override
    protected Object doExecute(Map<String, Object> params) {
        // 实现逻辑
        return result;
    }
    
    // ... 其他方法
}
```

### Q4: 如何调试工具执行？

启用DEBUG日志：

在 `application.yml` 中添加：
```yaml
logging:
  level:
    com.jingdezhen.tourism.mcp: DEBUG
```

### Q5: 性能优化建议？

1. 启用Redis缓存热门查询
2. 使用数据库索引优化查询
3. 添加连接池配置
4. 对频繁调用的API添加限流

## 进阶使用

### 自定义工具响应格式

可以在工具中自定义返回格式，MCP Server会自动处理JSON序列化。

### 添加认证

在 `McpController` 中添加Spring Security配置：

```java
@PreAuthorize("hasRole('USER')")
@PostMapping("/message")
public CompletableFuture<ResponseEntity<McpResponse>> handleMessage(...) {
    // ...
}
```

### 添加限流

使用Spring Cloud Alibaba Sentinel或Resilience4j：

```java
@RateLimiter(name = "mcpApi", fallbackMethod = "rateLimitFallback")
@PostMapping("/message")
public CompletableFuture<ResponseEntity<McpResponse>> handleMessage(...) {
    // ...
}
```

## 联系支持

- 查看详细文档: `/backend/src/main/java/com/jingdezhen/tourism/mcp/README.md`
- 查看代码示例: `/backend/src/test/java/com/jingdezhen/tourism/mcp/`
- 提交问题: GitHub Issues

---

**祝你使用愉快！** 🎉

