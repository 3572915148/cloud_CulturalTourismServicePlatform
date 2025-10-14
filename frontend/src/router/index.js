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
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
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
  
  // 检查是否需要登录
  if (to.meta.requireAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
  }
  
  next()
})

export default router

