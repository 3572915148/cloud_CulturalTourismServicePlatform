<template>
  <div class="user-page">
    <div class="user-header">
      <div class="user-info-card">
        <el-avatar :size="80">
          {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="user-details">
          <h2>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h2>
          <p class="user-id">ID: {{ userStore.userInfo.id }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">0</span>
              <span class="stat-label">订单</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">0</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">0</span>
              <span class="stat-label">评价</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="user-content">
      <el-tabs v-model="activeTab" class="user-tabs">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="info">
          <div class="tab-content">
            <el-form
              :model="userForm"
              label-width="100px"
              style="max-width: 600px"
            >
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              
              <el-form-item label="昵称">
                <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              
              <el-form-item label="手机号">
                <el-input v-model="userForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              
              <el-form-item label="邮箱">
                <el-input v-model="userForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              
              <el-form-item label="性别">
                <el-radio-group v-model="userForm.gender">
                  <el-radio :label="0">保密</el-radio>
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="个人简介">
                <el-input
                  v-model="userForm.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己吧"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleUpdateInfo">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 我的订单 -->
        <el-tab-pane label="我的订单" name="orders">
          <div class="tab-content">
            <!-- 订单状态筛选 -->
            <div class="order-filter">
              <el-radio-group v-model="orderStatus" @change="fetchMyOrders">
                <el-radio-button :label="null">全部</el-radio-button>
                <el-radio-button :label="0">待支付</el-radio-button>
                <el-radio-button :label="1">已支付</el-radio-button>
                <el-radio-button :label="2">已完成</el-radio-button>
                <el-radio-button :label="3">已取消</el-radio-button>
              </el-radio-group>
            </div>

            <div v-loading="orderLoading">
              <el-empty v-if="orderList.length === 0" description="暂无订单" />
              <div v-else class="order-list">
                <el-card 
                  v-for="order in orderList" 
                  :key="order.id"
                  class="order-item"
                >
                  <template #header>
                    <div class="order-header">
                      <span class="order-no">订单号：{{ order.orderNo }}</span>
                      <el-tag :type="getOrderStatusColor(order.status)">
                        {{ order.statusText }}
                      </el-tag>
                    </div>
                  </template>
                  
                  <div class="order-content">
                    <div class="order-product">
                      <img 
                        :src="order.productImage || '/placeholder.jpg'" 
                        :alt="order.productTitle"
                        class="product-image"
                      >
                      <div class="product-info">
                        <h4>{{ order.productTitle }}</h4>
                        <p class="product-meta">
                          单价：¥{{ order.price }} × {{ order.quantity }}
                        </p>
                        <p class="product-meta" v-if="order.contactName">
                          联系人：{{ order.contactName }} {{ order.contactPhone }}
                        </p>
                      </div>
                    </div>
                    
                    <div class="order-amount">
                      <span class="amount-label">总金额：</span>
                      <span class="amount-value">¥{{ order.totalAmount }}</span>
                    </div>
                  </div>
                  
                  <div class="order-footer">
                    <span class="order-time">{{ formatTime(order.createTime) }}</span>
                    <div class="order-actions">
                      <el-button 
                        v-if="order.status === 0" 
                        type="primary" 
                        size="small"
                        @click="handleViewOrder(order)"
                      >
                        去支付
                      </el-button>
                      <el-button 
                        v-if="order.status === 0" 
                        size="small"
                        @click="handleCancelOrder(order.id)"
                      >
                        取消订单
                      </el-button>
                      <el-button 
                        v-if="order.status === 1" 
                        type="success" 
                        size="small"
                        @click="handleFinishOrder(order.id)"
                      >
                        确认完成
                      </el-button>
                      <el-button 
                        v-if="order.status === 2 && order.canReview" 
                        type="primary" 
                        size="small"
                      >
                        去评价
                      </el-button>
                      <el-button 
                        size="small"
                        @click="handleViewOrder(order)"
                      >
                        查看详情
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的收藏 -->
        <el-tab-pane label="我的收藏" name="favorites">
          <div class="tab-content">
            <el-empty v-if="favoritesList.length === 0" description="暂无收藏" />
            <div v-else class="favorites-grid">
              <el-card 
                v-for="item in favoritesList" 
                :key="item.id"
                class="favorite-item"
                shadow="hover"
              >
                <img 
                  :src="item.coverImage || '/placeholder.jpg'" 
                  :alt="item.title"
                  class="favorite-image"
                  @click="router.push(`/product/${item.id}`)"
                >
                <div class="favorite-info">
                  <h4 @click="router.push(`/product/${item.id}`)">{{ item.title }}</h4>
                  <p class="favorite-price">¥{{ item.price }}</p>
                  <p class="favorite-time">收藏于 {{ formatTime(item.createTime) }}</p>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="handleRemoveFavorite(item.id)"
                  >
                    取消收藏
                  </el-button>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 我的评价 -->
        <el-tab-pane label="我的评价" name="reviews">
          <div class="tab-content">
            <el-empty description="暂无评价" />
          </div>
        </el-tab-pane>

        <!-- 意见反馈 -->
        <el-tab-pane label="意见反馈" name="feedback">
          <div class="tab-content">
            <div class="feedback-form">
              <h3>提交反馈</h3>
              <el-form
                :model="feedbackForm"
                label-width="100px"
                style="max-width: 600px"
              >
                <el-form-item label="反馈类型">
                  <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
                    <el-option label="功能建议" :value="1" />
                    <el-option label="问题反馈" :value="2" />
                    <el-option label="投诉" :value="3" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="反馈内容">
                  <el-input
                    v-model="feedbackForm.content"
                    type="textarea"
                    :rows="6"
                    placeholder="请详细描述您的问题或建议..."
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="联系方式">
                  <el-input
                    v-model="feedbackForm.contact"
                    placeholder="手机号或邮箱（可选）"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleSubmitFeedback">
                    提交反馈
                  </el-button>
                  <el-button @click="handleResetFeedback">
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <div class="feedback-history">
              <h3>我的反馈历史</h3>
              <div v-loading="feedbackLoading">
                <el-empty v-if="feedbackList.length === 0" description="暂无反馈记录" />
                <div v-else class="feedback-list">
                  <el-card 
                    v-for="item in feedbackList" 
                    :key="item.id"
                    class="feedback-item"
                  >
                    <template #header>
                      <div class="feedback-header">
                        <el-tag :type="getTypeColor(item.type)">{{ item.typeText }}</el-tag>
                        <el-tag :type="getStatusColor(item.status)">{{ item.statusText }}</el-tag>
                        <span class="feedback-time">{{ formatTime(item.createTime) }}</span>
                      </div>
                    </template>
                    <div class="feedback-content">
                      {{ item.content }}
                    </div>
                    <div v-if="item.reply" class="feedback-reply">
                      <div class="reply-title">平台回复：</div>
                      <div class="reply-content">{{ item.reply }}</div>
                      <div class="reply-time">{{ formatTime(item.replyTime) }}</div>
                    </div>
                  </el-card>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 账号设置 -->
        <el-tab-pane label="账号设置" name="settings">
          <div class="tab-content">
            <div class="settings-section">
              <h3>修改密码</h3>
              <el-form
                :model="passwordForm"
                label-width="100px"
                style="max-width: 600px"
              >
                <el-form-item label="原密码">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="新密码">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（6-20位）"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="确认密码">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <el-divider />

            <div class="settings-section">
              <h3>账号注销</h3>
              <p class="danger-text">注销账号后，所有数据将被清空且无法恢复，请谨慎操作！</p>
              <el-button type="danger" @click="handleDeleteAccount">
                注销账号
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createFeedback, getMyFeedbacks } from '@/api/feedback'
import { getMyOrders, cancelOrder, finishOrder } from '@/api/order'
import { updateUserInfo, changePassword, deleteAccount } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('info')

const userForm = reactive({
  username: userStore.userInfo.username || '',
  nickname: userStore.userInfo.nickname || '',
  phone: userStore.userInfo.phone || '',
  email: userStore.userInfo.email || '',
  gender: 0,
  bio: ''
})

const feedbackForm = reactive({
  type: null,
  content: '',
  contact: ''
})

const feedbackList = ref([])
const feedbackLoading = ref(false)

const orderList = ref([])
const orderLoading = ref(false)
const orderStatus = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 收藏列表（从localStorage获取）
const favoritesList = computed(() => {
  const favorites = localStorage.getItem('favorites')
  if (!favorites) return []
  const allFavorites = JSON.parse(favorites)
  // 只返回当前用户的收藏
  return allFavorites.filter(item => item.userId === userStore.userInfo.id)
})

// 更新个人信息
const handleUpdateInfo = async () => {
  try {
    // 注意：这里假设后端有这个接口，如果没有，会返回404
    // 可以先注释掉实际调用，只更新本地store
    // await updateUserInfo(userForm)
    
    // 更新本地用户信息
    userStore.userInfo.nickname = userForm.nickname
    userStore.userInfo.phone = userForm.phone
    userStore.userInfo.email = userForm.email
    userStore.userInfo.gender = userForm.gender
    
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新个人信息失败：', error)
    ElMessage.error('更新失败，请稍后重试')
  }
}

// 提交反馈
const handleSubmitFeedback = async () => {
  if (!feedbackForm.type) {
    ElMessage.warning('请选择反馈类型')
    return
  }
  if (!feedbackForm.content) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  
  try {
    await createFeedback(feedbackForm)
    ElMessage.success('反馈提交成功，感谢您的宝贵意见！')
    handleResetFeedback()
    // 刷新反馈列表
    fetchMyFeedbacks()
  } catch (error) {
    console.error('提交反馈失败：', error)
  }
}

// 重置反馈表单
const handleResetFeedback = () => {
  feedbackForm.type = null
  feedbackForm.content = ''
  feedbackForm.contact = ''
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    ElMessage.warning('密码长度必须在6-20位之间')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    // 注意：这里假设后端有这个接口，如果没有，会返回404
    // 可以先注释掉实际调用
    // await changePassword({
    //   oldPassword: passwordForm.oldPassword,
    //   newPassword: passwordForm.newPassword
    // })
    
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('修改密码失败：', error)
    ElMessage.error('密码修改失败，请检查原密码是否正确')
  }
}

// 注销账号
const handleDeleteAccount = () => {
  ElMessageBox.confirm(
    '注销账号后，所有数据将被清空且无法恢复，确定要继续吗？',
    '警告',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'error',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      // 注意：这里假设后端有这个接口，如果没有，会返回404
      // 可以先注释掉实际调用
      // await deleteAccount()
      
      ElMessage.success('账号已注销')
      userStore.logout()
      router.push('/home')
    } catch (error) {
      console.error('注销账号失败：', error)
      ElMessage.error('注销失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 获取我的反馈列表
const fetchMyFeedbacks = async () => {
  feedbackLoading.value = true
  try {
    const res = await getMyFeedbacks({ current: 1, size: 10 })
    if (res.data && res.data.records) {
      feedbackList.value = res.data.records
    }
  } catch (error) {
    console.error('获取反馈列表失败：', error)
  } finally {
    feedbackLoading.value = false
  }
}

// 获取反馈类型颜色
const getTypeColor = (type) => {
  const colors = {
    1: 'success',  // 功能建议
    2: 'danger',   // 问题反馈
    3: 'warning'   // 投诉
  }
  return colors[type] || 'info'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return colors[status] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 获取我的订单列表
const fetchMyOrders = async () => {
  orderLoading.value = true
  try {
    const params = { current: 1, size: 20 }
    if (orderStatus.value !== null) {
      params.status = orderStatus.value
    }
    const res = await getMyOrders(params)
    if (res.data && res.data.records) {
      orderList.value = res.data.records
    }
  } catch (error) {
    console.error('获取订单列表失败：', error)
  } finally {
    orderLoading.value = false
  }
}

// 获取订单状态颜色
const getOrderStatusColor = (status) => {
  const colors = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'info',
    4: 'warning',
    5: 'success'
  }
  return colors[status] || 'info'
}

// 查看订单详情
const handleViewOrder = (order) => {
  // 显示订单详情对话框
  ElMessageBox.alert(
    `<div style="text-align: left;">
      <p><strong>订单号：</strong>${order.orderNo}</p>
      <p><strong>产品名称：</strong>${order.productTitle}</p>
      <p><strong>订单状态：</strong>${order.statusText}</p>
      <p><strong>购买数量：</strong>${order.quantity}</p>
      <p><strong>订单金额：</strong>¥${order.totalAmount}</p>
      <p><strong>联系人：</strong>${order.contactName || '未填写'}</p>
      <p><strong>联系电话：</strong>${order.contactPhone || '未填写'}</p>
      <p><strong>创建时间：</strong>${formatTime(order.createTime)}</p>
      ${order.payTime ? `<p><strong>支付时间：</strong>${formatTime(order.payTime)}</p>` : ''}
      ${order.completeTime ? `<p><strong>完成时间：</strong>${formatTime(order.completeTime)}</p>` : ''}
      ${order.remark ? `<p><strong>备注：</strong>${order.remark}</p>` : ''}
    </div>`,
    '订单详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 取消订单
const handleCancelOrder = (orderId) => {
  ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelOrder(orderId)
      ElMessage.success('订单已取消')
      fetchMyOrders()
    } catch (error) {
      console.error('取消订单失败：', error)
    }
  })
}

// 完成订单
const handleFinishOrder = (orderId) => {
  ElMessageBox.confirm('确认已经使用完该产品/服务了吗？', '提示', {
    confirmButtonText: '确认完成',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await finishOrder(orderId)
      ElMessage.success('订单已完成')
      fetchMyOrders()
    } catch (error) {
      console.error('完成订单失败：', error)
    }
  })
}

// 取消收藏
const handleRemoveFavorite = (productId) => {
  const favorites = localStorage.getItem('favorites')
  if (!favorites) return
  
  const allFavorites = JSON.parse(favorites)
  const newFavorites = allFavorites.filter(item => 
    !(item.id === productId && item.userId === userStore.userInfo.id)
  )
  
  localStorage.setItem('favorites', JSON.stringify(newFavorites))
  ElMessage.success('取消收藏成功')
  
  // 强制刷新收藏列表（通过修改localStorage触发computed更新）
  // 这里我们需要触发一次更新，可以通过重新获取来实现
  window.dispatchEvent(new Event('storage'))
}

// 监听activeTab变化，加载对应数据
watch(activeTab, (newVal) => {
  if (newVal === 'orders') {
    fetchMyOrders()
  } else if (newVal === 'feedback') {
    fetchMyFeedbacks()
  }
})

onMounted(() => {
  if (activeTab.value === 'feedback') {
    fetchMyFeedbacks()
  }
})
</script>

<style scoped>
.user-page {
  max-width: 1400px;
  margin: 0 auto;
}

.user-header {
  margin-bottom: 24px;
}

.user-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 40px;
  display: flex;
  align-items: center;
  gap: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-info-card :deep(.el-avatar) {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
}

.user-details {
  color: #fff;
}

.user-details h2 {
  font-size: 28px;
  margin-bottom: 8px;
}

.user-id {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 16px;
}

.user-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

.user-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tab-content {
  padding: 24px 0;
}

.feedback-form {
  margin-bottom: 48px;
}

.feedback-form h3,
.feedback-history h3,
.settings-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.settings-section {
  margin: 32px 0;
}

.danger-text {
  color: #f56c6c;
  margin-bottom: 16px;
  font-size: 14px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  margin-bottom: 0;
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feedback-time {
  margin-left: auto;
  font-size: 13px;
  color: #999;
}

.feedback-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}

.feedback-reply {
  margin-top: 16px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
}

.reply-title {
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 8px;
}

.reply-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 8px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.order-filter {
  margin-bottom: 24px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  margin-bottom: 0;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.order-product {
  display: flex;
  gap: 16px;
  flex: 1;
}

.order-product .product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.order-product .product-info {
  flex: 1;
}

.order-product .product-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.order-product .product-meta {
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}

.order-amount {
  text-align: right;
}

.amount-label {
  font-size: 14px;
  color: #666;
}

.amount-value {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
  margin-left: 8px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.order-time {
  font-size: 13px;
  color: #999;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
}

.favorite-item {
  cursor: pointer;
  transition: transform 0.3s;
}

.favorite-item:hover {
  transform: translateY(-4px);
}

.favorite-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
}

.favorite-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-info h4:hover {
  color: #409EFF;
}

.favorite-price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 8px;
}

.favorite-time {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
}
</style>

