# AI功能问题排查指南

## 🚨 当前问题

用户反馈：AI推荐失败，出现AxiosError错误

## 🔍 问题诊断步骤

### 步骤1：检查服务状态

#### 1.1 后端服务检查
```bash
# 检查后端是否运行
curl http://localhost:8080/api/user/login -X POST -H "Content-Type: application/json" -d '{"username":"test","password":"test"}'

# 如果返回错误信息，说明后端正常运行
# 如果连接失败，说明后端未启动
```

#### 1.2 前端服务检查
```bash
# 检查前端是否运行
curl http://localhost:3000

# 如果返回HTML内容，说明前端正常运行
```

### 步骤2：使用AI测试页面

#### 2.1 访问测试页面
1. 登录系统后，访问：http://localhost:3000/ai-test
2. 点击"运行测试"按钮
3. 查看测试结果

#### 2.2 手动测试
1. 在测试页面输入测试查询
2. 点击"发送测试请求"
3. 查看详细的错误信息

### 步骤3：检查具体错误

#### 3.1 浏览器开发者工具
1. 按F12打开开发者工具
2. 切换到Network标签
3. 尝试发送AI推荐请求
4. 查看请求状态和响应

#### 3.2 常见错误类型

**404错误 - 接口不存在**
```
原因：AI控制器未正确注册
解决：检查后端启动日志，确认AI控制器映射
```

**401错误 - 认证失败**
```
原因：用户未登录或Token无效
解决：重新登录，检查Token是否正确
```

**500错误 - 服务器内部错误**
```
原因：DeepSeek API调用失败或数据库错误
解决：检查API Key配置和数据库连接
```

**网络错误**
```
原因：网络连接问题或CORS配置
解决：检查网络连接和跨域配置
```

## 🛠️ 解决方案

### 方案1：重新启动服务

#### 重启后端
```bash
cd backend
# 停止当前服务（Ctrl+C）
mvn spring-boot:run
```

#### 重启前端
```bash
cd frontend
# 停止当前服务（Ctrl+C）
npm run dev
```

### 方案2：检查配置

#### 检查DeepSeek API Key
```yaml
# application.yml
spring:
  ai:
    deepseek:
      api-key: sk-your-actual-api-key  # 确保是真实的API Key
```

#### 检查数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jingdezhen_tourism
    username: root
    password: your_password
```

### 方案3：检查数据库表

#### 确认AI推荐表存在
```sql
-- 检查表是否存在
SHOW TABLES LIKE 'ai_recommendation';

-- 如果不存在，创建表
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

### 方案4：检查产品数据

#### 确认有产品数据
```sql
-- 检查产品表是否有数据
SELECT COUNT(*) FROM product WHERE status = 1;

-- 如果没有数据，需要先导入产品数据
```

## 🧪 测试方法

### 测试1：基础连接测试
```bash
# 测试用户登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"your_username","password":"your_password"}'
```

### 测试2：AI接口测试
```bash
# 获取Token后测试AI推荐
curl -X POST http://localhost:8080/api/ai/recommend \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"query":"测试查询"}'
```

### 测试3：前端测试
1. 访问 http://localhost:3000/ai-test
2. 运行自动测试
3. 进行手动测试

## 📊 问题排查清单

- [ ] 后端服务正常启动
- [ ] 前端服务正常启动
- [ ] 用户已登录
- [ ] Token有效
- [ ] DeepSeek API Key正确
- [ ] 数据库连接正常
- [ ] ai_recommendation表存在
- [ ] 产品数据存在
- [ ] 网络连接正常
- [ ] 无CORS错误

## 🎯 快速修复

### 如果所有检查都正常，尝试以下步骤：

1. **清除浏览器缓存**
   - 按Ctrl+Shift+R强制刷新
   - 清除localStorage中的token
   - 重新登录

2. **检查控制台错误**
   - 查看浏览器控制台的具体错误信息
   - 查看后端控制台的错误日志

3. **使用测试页面**
   - 访问/ai-test页面进行详细测试
   - 查看具体的错误响应

4. **联系技术支持**
   - 提供具体的错误信息
   - 提供测试页面的测试结果

## 📝 错误日志收集

### 收集以下信息：
1. 浏览器控制台错误
2. 网络请求详情
3. 后端启动日志
4. 测试页面结果
5. 具体的错误消息

---

**按照以上步骤进行排查，大部分问题都能得到解决！**
