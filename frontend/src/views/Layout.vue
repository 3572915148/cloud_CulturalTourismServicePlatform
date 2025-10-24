<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/home')">
          <el-icon :size="32" color="#409EFF"><ShoppingBag /></el-icon>
          <span>景德镇文旅服务平台</span>
        </div>
        
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          class="nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/products">文旅产品</el-menu-item>
          <el-menu-item index="/ceramic">陶瓷文化</el-menu-item>
          <el-menu-item index="/ai-chat" v-if="userStore.isLoggedIn">
            <el-icon><ChatDotRound /></el-icon>
            AI助手
          </el-menu-item>
        </el-menu>
        
        <div class="user-actions">
          <!-- 商户入口 -->
          <el-button 
            type="warning" 
            :icon="Shop" 
            @click="router.push('/merchant/login')"
            style="margin-right: 16px"
          >
            商户中心
          </el-button>

          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar">
                  {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="user">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" link @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <!-- 主体内容 -->
    <el-main class="main">
      <router-view v-slot="{ Component }">
        <keep-alive :include="['AiChat']">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </el-main>
    
    <!-- 底部 -->
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-info">
          <h3>景德镇文旅服务平台</h3>
          <p>探索千年瓷都，感受陶瓷文化魅力</p>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h4>关于我们</h4>
            <a href="#">平台介绍</a>
            <a href="#">联系我们</a>
          </div>
          <div class="link-group">
            <h4>服务支持</h4>
            <a href="#">帮助中心</a>
            <a href="#">用户协议</a>
          </div>
          <div class="link-group">
            <h4>商户服务</h4>
            <a @click="router.push('/merchant/login')" style="cursor: pointer">商户登录</a>
            <a @click="router.push('/merchant/register')" style="cursor: pointer">商户入驻</a>
          </div>
        </div>
      </div>
      <div class="copyright">
        © 2024 景德镇文旅服务平台. All rights reserved.
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return '/' + route.path.split('/')[1]
})

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/home')
    })
  } else if (command === 'user') {
    router.push('/user')
  } else if (command === 'orders') {
    router.push('/user')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 64px;
  line-height: 64px;
  padding: 0 !important;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #409EFF;
  cursor: pointer;
  user-select: none;
}

.nav-menu {
  flex: 1;
  border: none;
  margin: 0 48px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
}

.footer {
  background: #2c3e50;
  color: #fff;
  padding: 48px 24px 24px;
  height: auto !important;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  margin-bottom: 32px;
}

.footer-info h3 {
  font-size: 20px;
  margin-bottom: 12px;
}

.footer-info p {
  color: #b0b0b0;
}

.footer-links {
  display: flex;
  gap: 80px;
}

.link-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.link-group h4 {
  font-size: 16px;
  margin-bottom: 8px;
}

.link-group a {
  color: #b0b0b0;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.link-group a:hover {
  color: #409EFF;
}

.copyright {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: #b0b0b0;
  font-size: 14px;
}
</style>

