# 景德镇文旅平台 MCP Server

## 简介

这是一个基于 **Model Context Protocol (MCP)** 的旅游推荐服务器，为AI Agent提供访问景德镇旅游数据的能力。

## 架构设计

```
mcp/
├── model/              # MCP协议数据模型
│   ├── McpRequest.java
│   ├── McpResponse.java
│   └── ToolDefinition.java
├── server/             # MCP服务器核心
│   └── McpServer.java
├── tool/               # MCP工具接口和实现
│   ├── McpTool.java
│   ├── AbstractMcpTool.java
│   └── impl/
│       ├── SearchAttractionsTool.java
│       ├── RecommendDailyPlanTool.java
│       ├── FindAccommodationsTool.java
│       └── GetTravelBudgetTool.java
└── config/             # 配置类
    └── McpServerConfig.java
```

## 可用工具

### 1. search_attractions - 景点搜索

根据关键词、价格、评分等条件搜索景点。

**参数：**
- `keyword` (string, 可选): 搜索关键词
- `region` (string, 可选): 所在区域
- `minPrice` (number, 可选): 最低价格
- `maxPrice` (number, 可选): 最高价格
- `minRating` (number, 可选): 最低评分 (1-5)
- `sortBy` (string, 可选): 排序方式 (price_asc, price_desc, rating_desc, default)
- `page` (integer, 可选): 页码，默认1
- `pageSize` (integer, 可选): 每页数量，默认10

**示例：**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "search_attractions",
    "arguments": {
      "keyword": "古窑",
      "minRating": 4.5,
      "sortBy": "rating_desc",
      "page": 1,
      "pageSize": 10
    }
  }
}
```

### 2. recommend_daily_plan - 每日行程推荐

根据天数、预算和兴趣生成旅游行程。

**参数：**
- `days` (integer, 必填): 旅游天数 (1-30)
- `budget` (number, 可选): 总预算
- `interests` (string, 可选): 兴趣偏好，逗号分隔
- `pace` (string, 可选): 旅游节奏 (relaxed, moderate, intense)
- `startDate` (string, 可选): 开始日期 (YYYY-MM-DD)

**示例：**
```json
{
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
}
```

### 3. find_accommodations - 住宿推荐

根据位置、价格、设施等条件搜索酒店。

**参数：**
- `region` (string, 可选): 所在区域
- `checkInDate` (string, 可选): 入住日期 (YYYY-MM-DD)
- `checkOutDate` (string, 可选): 退房日期 (YYYY-MM-DD)
- `minPrice` (number, 可选): 最低价格/晚
- `maxPrice` (number, 可选): 最高价格/晚
- `minRating` (number, 可选): 最低评分 (1-5)
- `facilities` (string, 可选): 设施要求，逗号分隔
- `hotelType` (string, 可选): 酒店类型
- `sortBy` (string, 可选): 排序方式
- `page` (integer, 可选): 页码
- `pageSize` (integer, 可选): 每页数量

**示例：**
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "find_accommodations",
    "arguments": {
      "region": "昌江区",
      "checkInDate": "2025-12-01",
      "checkOutDate": "2025-12-03",
      "minRating": 4.0,
      "maxPrice": 300
    }
  }
}
```

### 4. get_travel_budget - 旅行预算计算

计算详细的旅行预算清单。

**参数：**
- `days` (integer, 必填): 旅游天数 (1-30)
- `people` (integer, 可选): 旅游人数，默认1
- `accommodationLevel` (string, 可选): 住宿标准 (budget, standard, comfort, luxury)
- `mealLevel` (string, 可选): 用餐标准 (budget, standard, premium)
- `includeTransport` (boolean, 可选): 是否包含交通费
- `transportType` (string, 可选): 交通方式 (train, plane, car, bus)
- `departureCity` (string, 可选): 出发城市
- `shoppingBudget` (number, 可选): 购物预算/人
- `includeShopping` (boolean, 可选): 是否包含购物

**示例：**
```json
{
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
      "transportType": "train",
      "departureCity": "上海"
    }
  }
}
```

## HTTP API 使用

### 1. 服务器信息

```bash
GET /api/mcp/info
```

### 2. 健康检查

```bash
GET /api/mcp/health
```

### 3. 列出所有工具

```bash
GET /api/mcp/tools
```

### 4. 执行MCP请求

```bash
POST /api/mcp/message
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "id": "request-1",
  "method": "tools/call",
  "params": {
    "name": "search_attractions",
    "arguments": {
      "keyword": "陶瓷",
      "page": 1
    }
  }
}
```

## 完整使用示例

### 使用curl测试

```bash
# 1. 检查服务器状态
curl http://localhost:8080/api/mcp/health

# 2. 获取服务器信息
curl http://localhost:8080/api/mcp/info

# 3. 搜索景点
curl -X POST http://localhost:8080/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "search_attractions",
      "arguments": {
        "keyword": "古窑",
        "minRating": 4.0
      }
    }
  }'

# 4. 生成行程计划
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
        "pace": "moderate"
      }
    }
  }'

# 5. 计算预算
curl -X POST http://localhost:8080/api/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "get_travel_budget",
      "arguments": {
        "days": 3,
        "people": 2,
        "accommodationLevel": "standard"
      }
    }
  }'
```

### 使用Python客户端

```python
import requests
import json

class McpClient:
    def __init__(self, base_url="http://localhost:8080/api/mcp"):
        self.base_url = base_url
        self.request_id = 0
    
    def call_tool(self, tool_name, arguments):
        self.request_id += 1
        request = {
            "jsonrpc": "2.0",
            "id": str(self.request_id),
            "method": "tools/call",
            "params": {
                "name": tool_name,
                "arguments": arguments
            }
        }
        
        response = requests.post(
            f"{self.base_url}/message",
            json=request,
            headers={"Content-Type": "application/json"}
        )
        
        return response.json()

# 使用示例
client = McpClient()

# 搜索景点
result = client.call_tool("search_attractions", {
    "keyword": "陶瓷博物馆",
    "minRating": 4.5
})
print(json.dumps(result, indent=2, ensure_ascii=False))

# 生成行程
result = client.call_tool("recommend_daily_plan", {
    "days": 3,
    "budget": 3000,
    "interests": "陶瓷文化,历史"
})
print(json.dumps(result, indent=2, ensure_ascii=False))
```

### 使用JavaScript/TypeScript客户端

```typescript
class McpClient {
  private baseUrl: string;
  private requestId: number = 0;

  constructor(baseUrl: string = "http://localhost:8080/api/mcp") {
    this.baseUrl = baseUrl;
  }

  async callTool(toolName: string, args: any): Promise<any> {
    this.requestId++;
    
    const request = {
      jsonrpc: "2.0",
      id: this.requestId.toString(),
      method: "tools/call",
      params: {
        name: toolName,
        arguments: args
      }
    };

    const response = await fetch(`${this.baseUrl}/message`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(request)
    });

    return await response.json();
  }
}

// 使用示例
const client = new McpClient();

// 搜索景点
const attractions = await client.callTool("search_attractions", {
  keyword: "古窑",
  minRating: 4.0
});
console.log(attractions);

// 查找酒店
const hotels = await client.callTool("find_accommodations", {
  region: "昌江区",
  maxPrice: 300,
  minRating: 4.0
});
console.log(hotels);
```

## 集成到AI Agent

### Claude Desktop配置

在 `claude_desktop_config.json` 中添加：

```json
{
  "mcpServers": {
    "jingdezhen-tourism": {
      "url": "http://localhost:8080/api/mcp/message",
      "timeout": 30000
    }
  }
}
```

### 使用LangChain集成

```python
from langchain.tools import Tool
from langchain.agents import initialize_agent, AgentType
from langchain.llms import OpenAI

# 创建MCP工具包装器
def search_attractions(query: str) -> str:
    client = McpClient()
    result = client.call_tool("search_attractions", {"keyword": query})
    return json.dumps(result, ensure_ascii=False)

def recommend_plan(days: int, budget: float) -> str:
    client = McpClient()
    result = client.call_tool("recommend_daily_plan", {
        "days": days,
        "budget": budget
    })
    return json.dumps(result, ensure_ascii=False)

# 创建工具列表
tools = [
    Tool(
        name="SearchAttractions",
        func=search_attractions,
        description="搜索景德镇的景点和旅游项目"
    ),
    Tool(
        name="RecommendPlan",
        func=recommend_plan,
        description="生成景德镇旅游行程计划"
    )
]

# 初始化Agent
llm = OpenAI(temperature=0)
agent = initialize_agent(
    tools,
    llm,
    agent=AgentType.ZERO_SHOT_REACT_DESCRIPTION,
    verbose=True
)

# 使用Agent
response = agent.run("帮我规划一个3天的景德镇之旅，预算3000元")
print(response)
```

## 错误处理

MCP Server使用JSON-RPC 2.0标准错误代码：

- `-32700`: 解析错误
- `-32600`: 无效的请求
- `-32601`: 方法不存在
- `-32602`: 无效的参数
- `-32603`: 内部错误

错误响应示例：

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "error": {
    "code": -32602,
    "message": "参数验证失败: 页码必须大于0",
    "data": null
  }
}
```

## 性能优化

1. **异步处理**: 所有工具调用都是异步的，使用 `CompletableFuture`
2. **数据库优化**: 使用MyBatis Plus的分页查询，避免全表扫描
3. **连接池**: 使用HikariCP连接池管理数据库连接
4. **缓存**: 可选择启用Redis缓存热门查询结果

## 安全考虑

1. **参数验证**: 所有输入参数都经过严格验证
2. **SQL注入防护**: 使用MyBatis Plus的参数化查询
3. **访问控制**: 可以通过Spring Security添加认证授权
4. **速率限制**: 建议添加API限流机制

## 扩展开发

### 添加新工具

1. 创建工具类继承 `AbstractMcpTool`：

```java
@Component
public class MyNewTool extends AbstractMcpTool {
    
    @Override
    public String getName() {
        return "my_new_tool";
    }
    
    @Override
    public String getDescription() {
        return "我的新工具描述";
    }
    
    @Override
    public ToolDefinition getDefinition() {
        // 定义工具参数
        return ToolDefinition.builder()
            .name(getName())
            .description(getDescription())
            .inputSchema(/* ... */)
            .build();
    }
    
    @Override
    protected Object doExecute(Map<String, Object> params) {
        // 实现工具逻辑
        return result;
    }
}
```

2. Spring会自动注册新工具到MCP服务器

## 故障排查

### 常见问题

1. **工具未注册**: 检查工具类是否添加了 `@Component` 注解
2. **数据库连接失败**: 检查 `application.yml` 中的数据库配置
3. **参数验证失败**: 查看日志中的详细错误信息
4. **超时**: 调整数据库查询的超时时间或优化查询

### 日志配置

在 `application.yml` 中启用详细日志：

```yaml
logging:
  level:
    com.jingdezhen.tourism.mcp: DEBUG
```

## 部署建议

1. **生产环境**: 使用 `java -jar` 或容器化部署
2. **负载均衡**: 使用Nginx或HAProxy进行负载均衡
3. **监控**: 集成Prometheus + Grafana监控
4. **日志**: 使用ELK Stack收集和分析日志

## 许可证

MIT License

## 联系方式

- 项目主页: [GitHub](https://github.com/yourusername/jingdezhen-tourism)
- 问题反馈: [Issues](https://github.com/yourusername/jingdezhen-tourism/issues)

