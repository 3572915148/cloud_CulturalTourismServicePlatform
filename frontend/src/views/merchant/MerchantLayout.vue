<template>
  <el-container class="merchant-layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h2>商户管理后台</h2>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/merchant/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        
        <el-menu-item index="/merchant/products">
          <el-icon><Box /></el-icon>
          <span>产品管理</span>
        </el-menu-item>
        
        <el-menu-item index="/merchant/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        
        <el-menu-item index="/merchant/reviews">
          <el-icon><ChatDotSquare /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        
        <el-menu-item index="/merchant/shop">
          <el-icon><Shop /></el-icon>
          <span>店铺信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header height="60px" class="header">
        <div class="header-content">
          <div class="header-left">
            <span class="shop-name">{{ merchantStore.merchantInfo.shopName || '商户管理系统' }}</span>
            <el-tag v-if="merchantStore.merchantInfo.auditStatus === 0" type="warning" size="small">
              待审核
            </el-tag>
            <el-tag v-else-if="merchantStore.merchantInfo.auditStatus === 1" type="success" size="small">
              已审核
            </el-tag>
            <el-tag v-else-if="merchantStore.merchantInfo.auditStatus === 2" type="danger" size="small">
              审核拒绝
            </el-tag>
          </div>

          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32">{{ merchantStore.merchantInfo.username?.charAt(0) || 'M' }}</el-avatar>
                <span class="username">{{ merchantStore.merchantInfo.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info">
                    <el-icon><User /></el-icon>
                    商户信息
                  </el-dropdown-item>
                  <el-dropdown-item command="home">
                    <el-icon><HomeFilled /></el-icon>
                    浏览用户端
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  Box, 
  Document, 
  ChatDotSquare, 
  Shop, 
  User, 
  ArrowDown, 
  SwitchButton 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const merchantStore = useMerchantStore()

const activeMenu = computed(() => route.path)

// 菜单选择
const handleMenuSelect = (index) => {
  router.push(index)
}

// 下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'info':
      router.push('/merchant/shop')
      break
    case 'home':
      window.open('/', '_blank')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    merchantStore.logout()
    ElMessage.success('已退出登录')
    router.push('/merchant/login')
  })
}

onMounted(() => {
  // 获取最新的商户信息
  if (merchantStore.isLoggedIn) {
    merchantStore.fetchMerchantInfo()
  }
})
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
}

.sidebar {
  background: #001529;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
  background: #001529;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: #1890ff;
}

.header {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>

