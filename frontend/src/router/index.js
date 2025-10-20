import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/products',
        name: 'Products',
        component: () => import('@/views/Products.vue'),
        meta: { title: '文旅产品' }
      },
      {
        path: '/product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '产品详情' }
      },
      {
        path: '/ceramic',
        name: 'Ceramic',
        component: () => import('@/views/Ceramic.vue'),
        meta: { title: '陶瓷文化' }
      },
      {
        path: '/ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue'),
        meta: { title: 'AI文旅助手', requireAuth: true }
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '个人中心', requireAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  // 商户端路由
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/merchant/MerchantLogin.vue'),
    meta: { title: '商户登录' }
  },
  {
    path: '/merchant/register',
    name: 'MerchantRegister',
    component: () => import('@/views/merchant/MerchantRegister.vue'),
    meta: { title: '商户注册' }
  },
  {
    path: '/merchant',
    component: () => import('@/views/merchant/MerchantLayout.vue'),
    redirect: '/merchant/dashboard',
    meta: { requireMerchantAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue'),
        meta: { title: '商户工作台' }
      },
      {
        path: 'shop',
        name: 'MerchantShopInfo',
        component: () => import('@/views/merchant/ShopInfo.vue'),
        meta: { title: '店铺信息' }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/ProductManage.vue'),
        meta: { title: '产品管理' }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'reviews',
        name: 'MerchantReviews',
        component: () => import('@/views/merchant/ReviewManage.vue'),
        meta: { title: '评价管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 景德镇文旅服务平台` : '景德镇文旅服务平台'
  
  // 检查用户端是否需要登录
  if (to.meta.requireAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
  }
  
  // 检查商户端是否需要登录
  if (to.meta.requireMerchantAuth) {
    const merchantToken = localStorage.getItem('merchantToken')
    if (!merchantToken) {
      next('/merchant/login')
      return
    }
  }
  
  next()
})

export default router

