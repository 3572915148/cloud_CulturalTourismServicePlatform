# 景德镇文旅服务平台 - 前端项目

基于 Vue 3 + Vite + Element Plus 构建的现代化前端应用。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **样式**: SCSS

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口封装
│   │   ├── user.js       # 用户相关接口
│   │   └── product.js    # 产品相关接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── stores/           # 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── Layout.vue    # 布局组件
│   │   ├── Home.vue      # 首页
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   ├── Products.vue  # 产品列表
│   │   ├── ProductDetail.vue  # 产品详情
│   │   ├── Ceramic.vue   # 陶瓷文化
│   │   └── User.vue      # 个人中心
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── package.json          # 项目依赖
├── vite.config.js        # Vite配置
└── README.md             # 项目说明

```

## 功能模块

### 1. 用户模块
- ✅ 用户注册
- ✅ 用户登录
- ✅ 个人信息管理
- ✅ 密码修改
- ✅ 账号注销

### 2. 产品模块
- ✅ 产品列表展示（分页、筛选、搜索）
- ✅ 产品详情查看
- ✅ 在线预订功能
- ✅ 产品收藏

### 3. 陶瓷文化模块
- ✅ 文化内容展示
- ✅ 分类浏览
- ✅ 内容详情查看

### 4. 其他功能
- ✅ 意见反馈
- ✅ 响应式布局
- ✅ 路由守卫
- ✅ 统一错误处理

## 快速开始

### 前置要求

- Node.js >= 16.0.0
- npm >= 7.0.0

### 安装依赖

```bash
cd frontend
npm install
```

### 开发模式

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动。

### 生产构建

```bash
npm run build
```

构建产物将生成在 `dist` 目录。

### 预览构建

```bash
npm run preview
```

## 环境配置

### API代理配置

项目已在 `vite.config.js` 中配置了API代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

前端发送的 `/api/*` 请求会被代理到后端服务 `http://localhost:8080/api/*`。

## 主要特性

### 1. 统一的API请求封装

使用 Axios 拦截器实现：
- 自动添加 Token
- 统一错误处理
- 请求/响应拦截

### 2. 状态管理

使用 Pinia 进行状态管理：
- 用户信息持久化
- Token管理
- 登录状态管理

### 3. 路由守卫

实现了路由级别的权限控制：
- 登录验证
- 页面标题自动设置

### 4. 响应式设计

支持多种屏幕尺寸：
- 桌面端（>= 1200px）
- 平板端（768px - 1199px）
- 移动端（< 768px）

## 页面路由

| 路径 | 页面 | 说明 | 权限要求 |
|------|------|------|---------|
| `/` | 首页 | 平台首页，展示推荐产品 | 无 |
| `/home` | 首页 | 同上 | 无 |
| `/products` | 产品列表 | 浏览所有产品 | 无 |
| `/product/:id` | 产品详情 | 查看产品详细信息 | 无 |
| `/ceramic` | 陶瓷文化 | 浏览陶瓷文化内容 | 无 |
| `/user` | 个人中心 | 管理个人信息、订单等 | 需登录 |
| `/login` | 登录 | 用户登录 | 无 |
| `/register` | 注册 | 用户注册 | 无 |

## API接口

### 用户接口

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/{id}` - 获取用户信息

### 产品接口

- `GET /api/product/list` - 获取产品列表
- `GET /api/product/{id}` - 获取产品详情
- `POST /api/product` - 创建产品
- `PUT /api/product` - 更新产品
- `DELETE /api/product/{id}` - 删除产品

## 开发规范

### 组件命名

- 页面组件：使用 PascalCase，如 `ProductDetail.vue`
- 公共组件：使用 PascalCase，如 `UserCard.vue`

### 代码风格

- 使用 Composition API
- 使用 `<script setup>` 语法
- 遵循 Vue 3 官方风格指南

### 提交规范

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

## 常见问题

### 1. 端口被占用

修改 `vite.config.js` 中的 `server.port` 配置。

### 2. API请求失败

确保后端服务已启动在 `http://localhost:8080`。

### 3. 图片显示问题

项目使用了 Unsplash 的图片服务，需要网络连接。

## 未来计划

- [ ] 添加单元测试
- [ ] 实现订单管理功能
- [ ] 添加支付功能
- [ ] 实现实时聊天客服
- [ ] PWA支持
- [ ] 性能优化

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或联系开发团队。

