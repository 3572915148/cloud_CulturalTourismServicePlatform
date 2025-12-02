# 景德镇文旅服务平台

基于Spring Cloud微服务架构的文旅服务平台，提供产品管理、订单处理、用户服务、AI推荐等功能。

## 技术栈

- **框架**: Spring Boot 3.2.12, Spring Cloud 2023.0.3
- **服务注册与配置**: Nacos
- **消息队列**: RabbitMQ
- **数据库**: MySQL
- **缓存**: Redis
- **ORM**: MyBatis Plus
- **网关**: Spring Cloud Gateway

## 项目结构

```
CulturalTourismServicePlatform/
├── tourism-common/          # 公共模块（实体类、DTO、工具类等）
├── tourism-gateway/         # API网关
├── user-service/            # 用户服务
├── merchant-service/        # 商户服务
├── product-service/         # 产品服务
├── order-service/          # 订单服务
├── review-service/          # 评论服务
├── favorite-service/       # 收藏服务
├── feedback-service/        # 反馈服务
├── content-service/         # 内容服务
├── ai-service/              # AI推荐服务
└── file-service/            # 文件服务
```

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.0+
- RabbitMQ 3.8+

## 快速开始

### 1. 配置环境变量

项目使用环境变量管理敏感信息，请参考 [ENV_CONFIG.md](ENV_CONFIG.md) 配置以下环境变量：

- `DB_PASSWORD`: MySQL数据库密码
- `REDIS_PASSWORD`: Redis密码
- `JWT_SECRET`: JWT密钥
- `RABBITMQ_PASSWORD`: RabbitMQ密码
- `DEEPSEEK_API_KEY`: DeepSeek API密钥

### 2. 启动依赖服务

```bash
# 启动MySQL
# 启动Redis
# 启动Nacos
# 启动RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 3. 初始化数据库

创建数据库 `jingdezhen_tourism` 并导入表结构。

### 4. 启动服务

按以下顺序启动服务：

1. **product-service** - 产品服务（消息消费者，需先启动）
2. **user-service** - 用户服务
3. **merchant-service** - 商户服务
4. **order-service** - 订单服务
5. **review-service** - 评论服务
6. **其他服务** - 按需启动
7. **tourism-gateway** - API网关（最后启动）

### 5. 访问服务

- API网关: http://localhost:8080
- RabbitMQ管理界面: http://localhost:15672 (guest/guest)
- Nacos控制台: http://localhost:8848/nacos (nacos/nacos)

## 功能特性

### 消息队列集成

项目已集成RabbitMQ，实现以下异步处理：

1. **订单支付后更新产品销量** - 异步处理，提升支付响应速度
2. **评论创建/删除后更新产品评分** - 异步处理，提升评论响应速度
3. **订单取消后恢复库存** - 异步处理，提升取消订单响应速度

详细说明请参考代码中的消息队列实现。

## 配置说明

### 数据库配置

所有服务的 `application.yml` 中数据库密码使用环境变量：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:your-db-password}
```

### Redis配置

```yaml
spring:
  data:
    redis:
      password: ${REDIS_PASSWORD:your-redis-password}
```

### JWT配置

```yaml
jwt:
  secret: ${JWT_SECRET:your-jwt-secret-key}
```

## 开发指南

### 添加新服务

1. 在根目录 `pom.xml` 的 `<modules>` 中添加新模块
2. 创建服务目录，参考现有服务结构
3. 配置 `application.yml`
4. 在网关中添加路由配置

### 消息队列使用

参考 `order-service` 和 `review-service` 中的 `MessageProducerService` 实现消息发送。

## 注意事项

⚠️ **重要**: 
- 不要将包含真实密码的配置文件提交到Git仓库
- 生产环境建议使用配置中心（Nacos）管理敏感信息
- 定期更换密钥和密码

## License

MIT

