# 景德镇文旅服务平台

> 基于 Spring Boot 3 + Vue 3 的智能文旅推荐服务平台，集成 DeepSeek AI 和 MCP Server，为用户提供个性化的文旅服务体验。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.3.4-4FC08D.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 📋 项目简介

景德镇文旅服务平台是一个面向景德镇地区的文化旅游服务平台，集成了 **AI 智能推荐**、**产品管理**、**订单系统**、**商户管理**等功能。通过 **AI Agent 技术**和 **MCP Server**，为用户提供智能化的旅游推荐、行程规划和产品预订服务。

### 核心特性

- 🤖 **AI 智能推荐系统**：基于 DeepSeek AI 的智能 Agent，支持流式响应和工具自动调用
- 🛠️ **MCP Server**：实现了完整的 Model Context Protocol 服务器，提供 4 个核心工具
- 💾 **Redis 会话管理**：基于 Redis 的分布式会话管理，支持缓存回填策略
- ⚡ **性能优化**：7 个专用线程池，支持异步处理和并行执行
- 👥 **多端支持**：用户端、商户端、管理端三端分离
- 🎨 **陶瓷文化展示**：展示景德镇特色陶瓷文化、历史、工艺等内容
- 📱 **响应式设计**：支持 PC 端和移动端访问

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.1.5 | 核心框架 |
| **Spring Security** | 3.1.5 | 安全认证框架 |
| **MyBatis Plus** | 3.5.3.2 | 数据持久化框架 |
| **MySQL** | 8.0+ | 关系型数据库 |
| **Redis** | 6.0+ | 缓存和会话存储 |
| **JWT** | 0.11.5 | Token 认证 |
| **FastJSON2** | 2.0.43 | JSON 处理 |
| **DeepSeek AI** | - | AI 大语言模型（HTTP API） |
| **Lombok** | - | 代码简化工具 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.3.4 | 渐进式 JavaScript 框架（Composition API） |
| **Vite** | 5.0.4 | 下一代前端构建工具 |
| **Element Plus** | 2.4.4 | Vue 3 UI 组件库 |
| **Vue Router** | 4.2.5 | 官方路由管理 |
| **Pinia** | 2.1.7 | 状态管理 |
| **Axios** | 1.6.2 | HTTP 客户端 |
| **SCSS** | 1.69.5 | CSS 预处理器 |

---

## 📁 项目结构

```
Zsq_JingdezhenCulturalTourismServicePlatform/
├── backend/              # 后端项目（Spring Boot）
│   ├── agent/            # AI Agent 核心框架
│   ├── mcp/              # MCP Server 实现
│   ├── controller/       # 控制器层
│   ├── service/          # 服务层
│   ├── mapper/           # 数据访问层
│   └── config/           # 配置类
├── frontend/             # 前端项目（Vue 3）
│   ├── api/              # API 接口封装
│   ├── views/            # 页面组件
│   ├── components/       # 公共组件
│   └── router/           # 路由配置
└── docs/                 # 文档目录
```

---

## 🎯 核心功能

### 1. AI 智能推荐系统 ⭐

#### 功能特性

- ✅ **DeepSeek AI 集成**：通过 HTTP API 调用 DeepSeek AI，支持 Function Calling 和流式响应
- ✅ **会话管理**：基于 Redis 的分布式会话管理，支持会话持久化和恢复
- ✅ **工具自动调用**：AI 根据用户需求自动调用相应工具（9 个 Agent 工具）
- ✅ **缓存回填策略**：采用 Cache-Aside 模式，先从 Redis 查找，未命中则从数据库恢复并回填
- ✅ **历史记录管理**：保存推荐历史，支持历史记录查看和会话恢复
- ✅ **流式响应**：支持 SSE（Server-Sent Events）流式输出，提升用户体验

#### Agent 工具列表（9个）

**基础产品工具（5个）**：
1. `search_products` - 产品搜索工具（智能分类识别、多条件搜索）
2. `get_product_detail` - 产品详情查询工具
3. `get_product_categories` - 产品分类查询工具
4. `create_order` - 订单创建工具（AI 直接帮用户下单）
5. `smart_recommendation` - 智能推荐工具（基于用户行为的协同过滤算法）

**MCP 工具包装器（4个）**：
6. `mcp_search_attractions` - MCP 景点搜索
7. `mcp_recommend_daily_plan` - MCP 行程推荐
8. `mcp_find_accommodations` - MCP 住宿搜索
9. `mcp_get_travel_budget` - MCP 预算计算


---

### 2. MCP Server ⭐

实现了完整的 **Model Context Protocol** 服务器，提供 4 个核心工具：

| 工具 | 功能 | 参数 |
|------|------|------|
| `search_attractions` | 景点搜索 | keyword, minRating, region |
| `recommend_daily_plan` | 每日行程推荐 | days, budget, preferences |
| `find_accommodations` | 住宿推荐 | region, maxPrice, minRating |
| `get_travel_budget` | 旅行预算计算 | days, people, accommodationType |

**特性**：
- ✅ JSON-RPC 2.0 标准协议实现
- ✅ 完整的 HTTP API 接口
- ✅ 工具自动注册和发现机制

---

### 3. 用户端功能

- ✅ **用户认证**：注册、登录（JWT 认证）、密码修改
- ✅ **个人信息管理**：个人资料编辑、头像上传
- ✅ **产品浏览**：分类浏览、筛选（区域、价格、评分）、搜索、分页
- ✅ **产品详情**：详情查看、图片轮播、评价展示
- ✅ **在线预订**：产品预订、订单创建、库存检查
- ✅ **订单管理**：订单列表、订单详情、订单状态跟踪
- ✅ **产品收藏**：收藏/取消收藏、收藏列表
- ✅ **评价反馈**：产品评价、反馈提交
- ✅ **AI 智能推荐**：AI 对话、流式响应、历史记录查看

---

### 4. 商户端功能

- ✅ **商户认证**：商户注册、登录、审核流程
- ✅ **店铺管理**：店铺信息编辑、Logo 上传、营业执照上传
- ✅ **产品管理**：产品增删改查、图片上传、库存管理
- ✅ **订单管理**：订单列表、订单详情、订单状态更新
- ✅ **评价管理**：评价列表、评价回复
- ✅ **数据统计**：销售数据、订单统计（工作台）

---

### 5. 管理端功能

- ✅ **商户审核**：商户注册审核、审核通过/拒绝
- ✅ **系统管理**：用户管理、商户管理、产品管理

---

### 6. 陶瓷文化展示

- ✅ **文化内容展示**：陶瓷文化、历史、工艺等内容
- ✅ **分类浏览**：按分类浏览文化内容
- ✅ **详情查看**：内容详情、图片展示

---

## ⚡ 性能优化

### 线程池优化

项目实现了 **7 个专用线程池**，针对不同业务场景进行优化，包括库存同步、AI 工具执行、流式响应处理等。优化后，库存同步性能提升 90%，工具并行执行性能提升 3-5 倍。

### 缓存策略

- **Redis 会话管理**：分布式会话存储，支持自动过期和随机过期时间（防止缓存雪崩）
- **缓存回填策略**：采用 Cache-Aside 模式，实现 Redis 和 MySQL 的双层缓存

---

## 🗄️ 数据库设计

数据库包含以下核心表：

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `merchant` | 商户表 |
| `product_category` | 产品分类表 |
| `product` | 产品表（景点、酒店、餐厅等） |
| `orders` | 订单表 |
| `review` | 评价表 |
| `feedback` | 反馈表 |
| `favorite` | 收藏表 |
| `ceramic_content` | 陶瓷文化内容表 |
| `ai_recommendation` | AI 推荐记录表 |
| `admin` | 管理员表 |

详细的数据库设计见 `backend/src/main/resources/schema.sql` 文件。

---

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

编辑 `backend/src/main/resources/application.yml`，配置数据库、Redis、DeepSeek API Key 等信息。

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

---

## 📡 API 文档

主要接口包括：

- **AI 推荐**：`POST /api/ai/recommend/stream`（流式推荐）、`GET /api/ai/history`（推荐历史）
- **Agent 对话**：`POST /api/agent/chat/stream`（流式对话）
- **用户管理**：`POST /api/user/register`、`POST /api/user/login`
- **产品管理**：`GET /api/product/list`、`GET /api/product/{id}`
- **MCP 工具**：`POST /api/mcp/tools/call`

更多 API 文档请参考代码中的 Controller 类。

---

## 🔧 核心特性说明

### AI Agent 架构

- **工具注册系统**：Spring 自动发现和注册 Agent 工具
- **会话上下文管理**：维护对话历史和上下文变量
- **流式响应**：支持 SSE 流式输出，提升用户体验
- **工具自动调用**：AI 根据用户需求自动调用相应工具
- **并行执行**：多工具调用时自动并行执行，提升性能

### Redis 会话管理

- **分布式会话存储**：支持分布式部署
- **自动过期机制**：会话 30 分钟无活动自动过期
- **缓存回填策略**：采用 Cache-Aside 模式，实现 Redis 和 MySQL 的双层缓存

---

## 📄 许可证

本项目采用 MIT 许可证。

---

## 👥 联系方式

如有问题，请联系项目维护者。

---

**最后更新**：2025-01-15
