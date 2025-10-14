# 景德镇文旅服务平台

基于SpringBoot的智能文旅推荐服务平台，集成AI推荐功能，为用户提供个性化的文旅服务体验。

## 项目简介

本平台是一个面向景德镇地区的文化旅游服务平台，主要功能包括：
- 🤖 **AI智能推荐**：基于用户需求提供智能化的文旅推荐
- 👥 **用户端功能**：注册登录、个人信息管理、景点查询、在线预订、陶瓷文化浏览、评价反馈
- 🏪 **商户端功能**：商户入驻、店铺管理、产品管理、评价管理
- 🎨 **陶瓷文化展示**：展示景德镇特色陶瓷文化、历史、工艺等内容

## 技术栈

### 后端
- **SpringBoot 3.2.0** - 核心框架
- **Spring Security** - 安全认证
- **Spring AI** - AI集成
- **MyBatis Plus** - 数据持久化
- **MySQL** - 数据库
- **Redis** - 缓存
- **JWT** - Token认证

### 前端 ✅
- **Vue 3** - 渐进式JavaScript框架
- **Vite 5** - 下一代前端构建工具
- **Element Plus** - Vue 3 UI组件库
- **Vue Router** - 官方路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP客户端

## 项目结构

```
backend/
├── src/main/java/com/jingdezhen/tourism/
│   ├── config/              # 配置类
│   │   ├── MyBatisPlusConfig.java
│   │   ├── SecurityConfig.java
│   │   └── CorsConfig.java
│   ├── controller/          # 控制器层
│   │   ├── UserController.java
│   │   └── ProductController.java
│   ├── service/             # 服务层
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   └── impl/
│   ├── mapper/              # 数据访问层
│   │   ├── UserMapper.java
│   │   └── ProductMapper.java
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Merchant.java
│   │   ├── Product.java
│   │   ├── Orders.java
│   │   └── Review.java
│   ├── dto/                 # 数据传输对象
│   ├── vo/                  # 视图对象
│   ├── utils/               # 工具类
│   │   ├── JwtUtil.java
│   │   └── PasswordUtil.java
│   └── exception/           # 异常处理
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    ├── application.yml      # 配置文件
    └── schema.sql          # 数据库脚本
```

## 数据库设计

数据库包含以下核心表：
- `user` - 用户表
- `merchant` - 商户表
- `product_category` - 产品分类表
- `product` - 产品表（景点、酒店、餐厅等）
- `orders` - 订单表
- `review` - 评价表
- `feedback` - 反馈表
- `ceramic_content` - 陶瓷文化内容表
- `ai_recommendation` - AI推荐记录表
- `admin` - 管理员表

详细的数据库设计见 `schema.sql` 文件。

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 配置步骤

1. **创建数据库**
   ```bash
   # 执行数据库脚本
   mysql -u root -p < backend/src/main/resources/schema.sql
   ```

2. **修改配置文件**
   
   编辑 `backend/src/main/resources/application.yml`，修改以下配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/jingdezhen_tourism
       username: your_username
       password: your_password
     
     data:
       redis:
         host: localhost
         password: your_redis_password
     
     ai:
       openai:
         api-key: your_openai_api_key
   ```

3. **构建并运行**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

4. **启动后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   
   后端服务将在 `http://localhost:8080` 启动

5. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   
   前端服务将在 `http://localhost:3000` 启动

6. **访问应用**
   
   在浏览器中打开：`http://localhost:3000`

> 💡 **提示**：详细的前端启动说明请查看 `前端启动指南.md`

## API文档

### 用户相关接口

#### 用户注册
```
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
```
POST /api/user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

### 产品相关接口

#### 获取产品列表
```
GET /api/product/list?current=1&size=10&categoryId=1&region=昌江区
```

#### 获取产品详情
```
GET /api/product/{id}
```

## 开发计划

### 后端
- [x] 项目框架搭建
- [x] 数据库设计
- [x] 用户认证模块（注册、登录、JWT）
- [x] 产品管理模块（CRUD、筛选、搜索）
- [ ] AI推荐模块（DeepSeek集成）
- [ ] 订单管理模块完善
- [ ] 评价管理模块
- [ ] 商户端功能

### 前端 ✅
- [x] 项目框架搭建（Vue 3 + Vite）
- [x] UI组件库集成（Element Plus）
- [x] 路由和状态管理
- [x] 用户注册登录页面
- [x] 首页（Banner、分类、推荐产品）
- [x] 产品列表页面（筛选、分页）
- [x] 产品详情页面（图片、信息、预订）
- [x] 陶瓷文化页面（分类浏览、详情）
- [x] 个人中心页面（信息管理、订单、反馈）
- [x] 响应式布局设计
- [ ] 移动端优化
- [ ] 性能优化

### 部署
- [ ] Docker容器化
- [ ] 系统部署
- [ ] CI/CD配置

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题，请联系项目维护者。

