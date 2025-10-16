<template>
  <div class="dashboard">
    <div class="welcome-card">
      <div class="welcome-content">
        <h1>欢迎回来，{{ merchantStore.merchantInfo.shopName }}！</h1>
        <p>这是您的商户管理工作台</p>
      </div>
      <div class="merchant-status">
        <el-tag :type="statusType" size="large">
          {{ merchantStore.auditStatusText }}
        </el-tag>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-cards">
        <div class="action-card" @click="router.push('/merchant/products')">
          <el-icon :size="40" color="#409EFF"><Box /></el-icon>
          <h3>产品管理</h3>
          <p>管理您的文旅产品</p>
        </div>
        
        <div class="action-card" @click="router.push('/merchant/orders')">
          <el-icon :size="40" color="#67C23A"><Document /></el-icon>
          <h3>订单管理</h3>
          <p>查看和处理订单</p>
        </div>
        
        <div class="action-card" @click="router.push('/merchant/reviews')">
          <el-icon :size="40" color="#E6A23C"><ChatDotSquare /></el-icon>
          <h3>评价管理</h3>
          <p>查看用户评价并回复</p>
        </div>
        
        <div class="action-card" @click="router.push('/merchant/shop')">
          <el-icon :size="40" color="#F56C6C"><Shop /></el-icon>
          <h3>店铺信息</h3>
          <p>编辑店铺信息</p>
        </div>
      </div>
    </div>

    <!-- 商户信息卡片 -->
    <div class="info-section">
      <h2>店铺信息</h2>
      <el-card class="info-card">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商户账号">
            {{ merchantStore.merchantInfo.username }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺名称">
            {{ merchantStore.merchantInfo.shopName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ merchantStore.merchantInfo.contactPerson }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ merchantStore.merchantInfo.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱" :span="2">
            {{ merchantStore.merchantInfo.contactEmail || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺地址" :span="2">
            {{ merchantStore.merchantInfo.address || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusType">{{ merchantStore.auditStatusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatTime(merchantStore.merchantInfo.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <!-- 审核提示 -->
    <el-alert
      v-if="merchantStore.merchantInfo.auditStatus === 0"
      title="审核提示"
      type="warning"
      description="您的商户账号正在审核中，请耐心等待管理员审核。审核通过后即可正常使用所有功能。"
      :closable="false"
      show-icon
      style="margin-top: 24px"
    />

    <el-alert
      v-if="merchantStore.merchantInfo.auditStatus === 2"
      title="审核未通过"
      type="error"
      :description="`审核拒绝原因：${merchantStore.merchantInfo.auditRemark || '未说明'}`"
      :closable="false"
      show-icon
      style="margin-top: 24px"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import { Box, Document, ChatDotSquare, Shop } from '@element-plus/icons-vue'

const router = useRouter()
const merchantStore = useMerchantStore()

// 审核状态类型
const statusType = computed(() => {
  const status = merchantStore.merchantInfo.auditStatus
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 40px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.welcome-content h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.welcome-content p {
  font-size: 16px;
  opacity: 0.9;
}

.quick-actions {
  margin-bottom: 32px;
}

.quick-actions h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.action-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.action-card h3 {
  font-size: 18px;
  color: #333;
  margin: 16px 0 8px;
}

.action-card p {
  font-size: 14px;
  color: #666;
}

.info-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.info-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>

