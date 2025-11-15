# 景德镇文旅服务平台

基于 SpringBoot 3 + Vue 3 的智能文旅推荐服务平台，集成 DeepSeek AI 和 MCP Server，为用户提供个性化的文旅服务体验。

## 📋 项目简介

本平台是一个面向景德镇地区的文化旅游服务平台，集成了 AI 智能推荐、产品管理、订单系统、商户管理等功能。通过 AI Agent 技术，为用户提供智能化的旅游推荐和行程规划服务。

### 核心特性

- 🤖 **AI 智能推荐**：基于 DeepSeek AI 的智能推荐系统，支持流式响应和工具自动调用
- 🛠️ **MCP Server**：实现了 Model Context Protocol 服务器，提供 4 个核心工具
- 💾 **Redis 会话管理**：基于 Redis 的分布式会话管理，支持缓存回填策略
- 👥 **用户端功能**：注册登录、个人信息管理、产品浏览、在线预订、收藏、评价反馈
- 🏪 **商户端功能**：商户入驻、店铺管理、产品管理、订单管理、评价管理
- 🎨 **陶瓷文化展示**：展示景德镇特色陶瓷文化、历史、工艺等内容
- 📱 **响应式设计**：支持 PC 端和移动端访问

## 🛠️ 技术栈

### 后端

- **Spring Boot 3.1.5** - 核心框架
- **Spring Security** - 安全认证
- **Spring AI 0.8.1** - AI 集成框架
- **MyBatis Plus 3.5.3.2** - 数据持久化
- **MySQL 8.0+** - 关系型数据库
- **Redis 6.0+** - 缓存和会话存储
- **JWT 0.11.5** - Token 认证
- **FastJSON2 2.0.43** - JSON 处理
- **DeepSeek AI** - AI 大语言模型

### 前端

- **Vue 3.3.4** - 渐进式 JavaScript 框架（Composition API）
- **Vite 5.0.4** - 下一代前端构建工具
- **Element Plus 2.4.4** - Vue 3 UI 组件库
- **Vue Router 4.2.5** - 官方路由管理
- **Pinia 2.1.7** - 状态管理
- **Axios 1.6.2** - HTTP 客户端
- **SCSS** - CSS 预处理器

## 📁 项目结构

```
Zsq_JingdezhenCulturalTourismServicePlatform/
├── backend/                          # 后端项目
│   ├── src/main/java/com/jingdezhen/tourism/
│   │   ├── agent/                    # AI Agent 核心
│   │   │   ├── core/                 # 核心组件
│   │   │   │   └── ConversationContext.java  # 会话上下文
│   │   │   └── tool/                 # Agent 工具
│   │   │       ├── AgentTool.java    # 工具接口
│   │   │       ├── ToolRegistry.java # 工具注册器
│   │   │       └── impl/             # 工具实现
│   │   ├── mcp/                      # MCP Server
│   │   │   ├── model/                # MCP 协议模型
│   │   │   ├── server/               # MCP 服务器
│   │   │   ├── tool/                 # MCP 工具
│   │   │   └── config/               # MCP 配置
│   │   ├── controller/               # 控制器层（17个）
│   │   │   ├── UserController.java
│   │   │   ├── ProductController.java
│   │   │   ├── AiRecommendationController.java
│   │   │   ├── MerchantController.java
│   │   │   └── ...
│   │   ├── service/                  # 服务层（26个）
│   │   │   ├── RedisSessionManager.java        # Redis 会话管理
│   │   │   ├── SessionConsistencyService.java  # 会话一致性服务
│   │   │   ├── AgentService.java               # AI Agent 服务
│   │   │   ├── impl/
│   │   │   └── ...
│   │   ├── mapper/                   # 数据访问层（10个）
│   │   ├── entity/                   # 实体类（11个）
│   │   ├── dto/                      # 数据传输对象（12个）
│   │   ├── vo/                       # 视图对象（9个）
│   │   ├── config/                   # 配置类
│   │   ├── exception/                # 异常处理
│   │   └── utils/                    # 工具类
│   └── src/main/resources/
│       ├── application.yml           # 配置文件
│       ├── schema.sql                # 数据库脚本
│       └── mapper/                   # MyBatis XML
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── api/                      # API 接口封装（12个）
│   │   │   ├── ai.js                 # AI 推荐接口
│   │   │   ├── user.js               # 用户接口
│   │   │   ├── product.js            # 产品接口
│   │   │   └── ...
│   │   ├── views/                    # 页面组件
│   │   │   ├── Home.vue              # 首页
│   │   │   ├── AiChat.vue            # AI 对话页面
│   │   │   ├── Products.vue          # 产品列表
│   │   │   ├── ProductDetail.vue     # 产品详情
│   │   │   ├── Ceramic.vue           # 陶瓷文化
│   │   │   ├── User.vue              # 个人中心
│   │   │   ├── merchant/             # 商户端页面
│   │   │   └── admin/                # 管理端页面
│   │   ├── components/               # 公共组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # 状态管理
│   │   └── utils/                    # 工具函数
│   └── vite.config.js                # Vite 配置
└── docs/                             # 文档
    ├── AI_Agent开发情况分析与改进建议.md
    ├── MCP_SERVER_实现说明.md
    ├── 缓存回填策略分析.md
    └── ...
```

## 🎯 核心功能

### 1. AI 智能推荐系统

- ✅ **DeepSeek AI 集成**：支持 Function Calling 和流式响应
- ✅ **会话管理**：基于 Redis 的分布式会话管理，支持会话持久化
- ✅ **工具自动调用**：AI 自动调用搜索、推荐等工具
- ✅ **缓存回填策略**：先从 Redis 查找，未命中则从数据库恢复并回填
- ✅ **历史记录管理**：保存推荐历史，支持历史记录查看和恢复

**相关文件**：
- `backend/src/main/java/com/jingdezhen/tourism/service/impl/AgentServiceImpl.java`
- `backend/src/main/java/com/jingdezhen/tourism/service/RedisSessionManager.java`
- `backend/src/main/java/com/jingdezhen/tourism/service/SessionConsistencyService.java`

### 2. MCP Server

实现了完整的 Model Context Protocol 服务器，提供 4 个核心工具：

- ✅ **search_attractions** - 景点搜索工具
- ✅ **recommend_daily_plan** - 每日行程推荐工具
- ✅ **find_accommodations** - 住宿推荐工具
- ✅ **get_travel_budget** - 旅行预算计算工具

**相关文件**：
- `backend/src/main/java/com/jingdezhen/tourism/mcp/`
- `backend/src/main/java/com/jingdezhen/tourism/mcp/README.md`

### 3. 用户端功能

- ✅ 用户注册、登录（JWT 认证）
- ✅ 个人信息管理
- ✅ 产品浏览（分类、筛选、搜索、分页）
- ✅ 产品详情查看
- ✅ 在线预订
- ✅ 产品收藏
- ✅ 订单管理
- ✅ 评价反馈
- ✅ AI 智能推荐对话

### 4. 商户端功能

- ✅ 商户注册、登录
- ✅ 店铺信息管理
- ✅ 产品管理（增删改查）
- ✅ 订单管理
- ✅ 评价管理

### 5. 陶瓷文化展示

- ✅ 陶瓷文化内容展示
- ✅ 分类浏览
- ✅ 详情查看

## 🗄️ 数据库设计

数据库包含以下核心表：

- `user` - 用户表
- `merchant` - 商户表
- `product_category` - 产品分类表
- `product` - 产品表（景点、酒店、餐厅等）
- `orders` - 订单表
- `review` - 评价表
- `feedback` - 反馈表
- `favorite` - 收藏表
- `ceramic_content` - 陶瓷文化内容表
- `ai_recommendation` - AI 推荐记录表
- `admin` - 管理员表

详细的数据库设计见 `backend/src/main/resources/schema.sql` 文件。

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Node.js 16+**
- **npm 或 yarn**

### 配置步骤

#### 1. 克隆项目

```bash
git clone https://gitee.com/ShiRenAn/zsq_-jingdezhen-cultural-tourism-service-platform.git
cd Zsq_JingdezhenCulturalTourismServicePlatform
```

#### 2. 数据库配置

```bash
# 创建数据库
mysql -u root -p

# 执行数据库脚本
mysql -u root -p < backend/src/main/resources/schema.sql

# 导入测试数据（可选）
mysql -u root -p jingdezhen_tourism < backend/src/main/resources/ceramic_content_test_data.sql
mysql -u root -p jingdezhen_tourism < backend/src/main/resources/merchant_product_test_data.sql
```

#### 3. 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jingdezhen_tourism?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如果有密码
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  
  ai:
    deepseek:
      api-key: your_deepseek_api_key  # DeepSeek API Key
      model: deepseek-chat
      base-url: https://api.deepseek.com

# JWT 配置
jwt:
  secret: your_jwt_secret_key  # 建议使用随机字符串
  expiration: 86400000  # 24小时
```

#### 4. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

#### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动（Vite 默认端口）

#### 6. 访问应用

在浏览器中打开：`http://localhost:5173`

## 📡 API 文档

### AI 推荐接口

#### 流式推荐（SSE）

```http
POST /api/ai/recommend/stream
Content-Type: application/json
Authorization: Bearer {token}

{
  "query": "价格实惠的酒店"
}
```

#### 获取推荐历史

```http
GET /api/ai/history?current=1&size=20
Authorization: Bearer {token}
```

#### 恢复会话

```http
POST /api/ai/restore-session?recommendationId=1
Authorization: Bearer {token}
```

### 用户接口

#### 用户注册

```http
POST /api/user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "phone": "13800138000"
}
```

#### 用户登录

```http
POST /api/user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

### 产品接口

#### 获取产品列表

```http
GET /api/product/list?current=1&size=10&categoryId=1&region=昌江区
```

#### 获取产品详情

```http
GET /api/product/{id}
```

### MCP Server 接口

#### 调用工具

```http
POST /api/mcp/tools/call
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "search_attractions",
    "arguments": {
      "keyword": "古窑",
      "minRating": 4.5
    }
  }
}
```

更多 API 文档请参考代码中的 Controller 类。

## 🔧 核心特性说明

### Redis 会话管理

- **会话存储**：使用 Redis 存储会话上下文，支持分布式部署
- **自动过期**：会话 30 分钟无活动自动过期
- **缓存回填**：历史记录访问时，先从 Redis 查找，未命中则从数据库恢复并回填
- **随机过期时间**：防止缓存雪崩（30 分钟 - 2 小时随机）

### 缓存回填策略

实现了经典的 Cache-Aside 模式：

1. **读取流程**：先从 Redis 查找 → 未命中则从 MySQL 查找 → 找到后回填到 Redis
2. **写入流程**：先写入 MySQL → 再写入 Redis → 建立映射关系
3. **优势**：性能提升、数据持久性、按需加载
4. **优化**：随机过期时间、映射关系管理

详细分析见 `缓存回填策略分析.md`

### AI Agent 架构

- **工具注册系统**：自动发现和注册 Agent 工具
- **会话上下文管理**：维护对话历史和上下文变量
- **流式响应**：支持 SSE 流式输出，提升用户体验
- **工具自动调用**：AI 根据用户需求自动调用相应工具

## 📊 项目进度

### 后端 ✅

- [x] 项目框架搭建
- [x] 数据库设计和实现
- [x] 用户认证模块（注册、登录、JWT）
- [x] 产品管理模块（CRUD、筛选、搜索）
- [x] AI 推荐模块（DeepSeek 集成、流式响应）
- [x] MCP Server 实现（4 个核心工具）
- [x] Redis 会话管理
- [x] 缓存回填策略
- [x] 订单管理模块
- [x] 评价管理模块
- [x] 商户端功能
- [ ] 性能优化和监控

### 前端 ✅

- [x] 项目框架搭建（Vue 3 + Vite）
- [x] UI 组件库集成（Element Plus）
- [x] 路由和状态管理
- [x] 用户注册登录页面
- [x] 首页（Banner、分类、推荐产品）
- [x] 产品列表页面（筛选、分页）
- [x] 产品详情页面（图片、信息、预订）
- [x] AI 对话页面（流式响应、历史记录）
- [x] 陶瓷文化页面（分类浏览、详情）
- [x] 个人中心页面（信息管理、订单、反馈）
- [x] 商户端页面（产品管理、订单管理）
- [x] 响应式布局设计
- [ ] 移动端优化
- [ ] 性能优化

### 部署

- [ ] Docker 容器化
- [ ] 系统部署
- [ ] CI/CD 配置

## 📚 相关文档

- [AI Agent 开发情况分析与改进建议](./AI_Agent开发情况分析与改进建议.md)
- [MCP Server 实现说明](./MCP_SERVER_实现说明.md)
- [缓存回填策略分析](./缓存回填策略分析.md)
- [MCP 快速开始指南](./backend/MCP_QUICKSTART.md)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。

## 👥 联系方式

如有问题，请联系项目维护者。

---

**最后更新**：2025-11-15
