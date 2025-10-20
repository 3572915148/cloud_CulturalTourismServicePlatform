# 景德镇文旅服务平台 - AI功能启动指南

## 🚀 快速启动

### 1. 环境准备

确保您已经完成以下环境配置：
- ✅ Java 8+ 环境
- ✅ Node.js 16+ 环境
- ✅ MySQL 8.0+ 数据库
- ✅ Maven 3.6+ 构建工具

### 2. 数据库配置

确保数据库中存在以下表：
```sql
-- AI推荐记录表
CREATE TABLE `ai_recommendation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT COMMENT '用户ID',
  `query` TEXT NOT NULL COMMENT '用户查询内容',
  `context` TEXT COMMENT '上下文信息',
  `response` TEXT COMMENT 'AI响应内容',
  `recommended_products` TEXT COMMENT '推荐的产品ID列表（JSON数组）',
  `feedback` TINYINT COMMENT '用户反馈：1-有帮助，0-没帮助',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐记录表';
```

### 3. API Key配置

在 `backend/src/main/resources/application.yml` 中配置DeepSeek API Key：

```yaml
spring:
  ai:
    deepseek:
      api-key: sk-your-actual-deepseek-api-key  # 替换为您的真实API Key
      base-url: https://api.deepseek.com
      model: deepseek-chat
      temperature: 0.7
```

**获取DeepSeek API Key的步骤：**
1. 访问 [DeepSeek官网](https://platform.deepseek.com/)
2. 注册账号并登录
3. 在控制台中创建API Key
4. 将API Key配置到application.yml中

### 4. 启动服务

#### 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### 启动前端服务
```bash
cd frontend
npm install
npm run dev
```

### 5. 访问AI功能

1. **用户登录**：访问 http://localhost:3000/login
2. **进入AI助手**：
   - 方式一：首页点击"AI智能推荐"卡片
   - 方式二：导航栏点击"AI助手"菜单
   - 方式三：直接访问 http://localhost:3000/ai-chat

## 🧪 功能测试

### 1. 基础功能测试

#### 测试AI推荐功能
1. 在AI对话界面输入：`我想找一个适合家庭游玩的景点`
2. 观察AI是否返回推荐结果
3. 检查推荐产品是否正确显示

#### 测试推荐历史
1. 点击"查看历史"按钮
2. 确认历史记录正确显示
3. 点击历史记录项，确认能重新查看

#### 测试用户反馈
1. 对推荐结果点击"有帮助"或"没帮助"
2. 确认反馈状态正确更新

### 2. 推荐质量测试

#### 测试不同类型的查询
```
# 景点推荐
"推荐一些适合拍照的景点"
"我想找一个价格实惠的景点"

# 酒店推荐  
"推荐一些性价比高的酒店"
"我想找一个位置方便的酒店"

# 美食推荐
"推荐一些当地特色美食"
"我想找一个适合聚餐的餐厅"

# 陶瓷体验
"推荐一些陶瓷制作体验活动"
"我想学习陶瓷制作"
```

### 3. 边界情况测试

#### 测试空查询
- 输入空内容，确认有适当提示

#### 测试无效查询
- 输入与文旅无关的内容，确认AI能合理回复

#### 测试网络异常
- 模拟网络断开，确认有错误处理

## 🔧 故障排除

### 1. AI推荐无响应

**可能原因：**
- DeepSeek API Key无效或过期
- 网络连接问题
- API调用频率超限

**解决方案：**
1. 检查API Key是否正确配置
2. 确认网络连接正常
3. 检查DeepSeek账户余额
4. 查看后端日志错误信息

### 2. 推荐结果为空

**可能原因：**
- 数据库中没有相关产品数据
- AI解析推荐产品ID失败

**解决方案：**
1. 确认数据库中有产品数据
2. 检查产品状态是否为上架状态
3. 查看AI响应内容是否正确

### 3. 前端界面异常

**可能原因：**
- 前端编译错误
- API接口调用失败

**解决方案：**
1. 检查浏览器控制台错误信息
2. 确认后端服务正常运行
3. 检查API接口地址配置

## 📊 性能监控

### 1. API调用监控

监控以下指标：
- AI推荐请求响应时间
- API调用成功率
- 推荐结果质量评分

### 2. 用户行为分析

分析以下数据：
- 用户查询类型分布
- 推荐产品点击率
- 用户反馈统计

### 3. 系统资源监控

监控以下资源：
- 服务器CPU和内存使用率
- 数据库连接数
- 网络带宽使用情况

## 🎯 优化建议

### 1. 推荐算法优化

- 基于用户历史行为优化推荐
- 增加协同过滤算法
- 实现实时推荐更新

### 2. 用户体验优化

- 增加推荐理由的详细程度
- 支持图片识别推荐
- 添加语音交互功能

### 3. 系统性能优化

- 实现推荐结果缓存
- 优化AI提示词长度
- 增加异步处理机制

## 📝 开发日志

### 已完成功能
- ✅ AI推荐核心功能
- ✅ 推荐历史管理
- ✅ 用户反馈收集
- ✅ 前端对话界面
- ✅ 产品详情跳转

### 待优化功能
- ⏳ 推荐算法优化
- ⏳ 多语言支持
- ⏳ 移动端适配
- ⏳ 性能监控面板

---

**AI功能已完全集成并可以正常使用！如有问题，请参考故障排除部分或查看系统日志。**
