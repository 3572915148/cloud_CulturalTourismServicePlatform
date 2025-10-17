<template>
  <div class="user-page">
    <div class="user-header">
      <div class="user-info-card">
        <el-avatar :size="80" :src="userForm.avatar || userStore.userInfo.avatar">
          {{ userStore.userInfo.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="user-details">
          <h2>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h2>
          <p class="user-id">ID: {{ userStore.userInfo.id }}</p>
          <div class="user-stats">
            <div class="stat-item" @click="activeTab = 'orders'">
              <span class="stat-value">{{ statsData.orderCount }}</span>
              <span class="stat-label">订单</span>
            </div>
            <div class="stat-item" @click="activeTab = 'favorites'">
              <span class="stat-value">{{ statsData.favoriteCount }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item" @click="activeTab = 'reviews'">
              <span class="stat-value">{{ statsData.reviewCount }}</span>
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
              <!-- 头像上传 -->
              <el-form-item label="头像">
                <div class="avatar-upload">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    :http-request="uploadAvatar"
                    accept="image/*"
                  >
                    <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                  <div class="avatar-tip">
                    <p>支持 JPG、PNG 格式</p>
                    <p>文件大小不超过 2MB</p>
                  </div>
                </div>
              </el-form-item>

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
              
              <el-form-item label="生日">
                <el-date-picker
                  v-model="userForm.birthday"
                  type="date"
                  placeholder="选择生日"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="个人简介">
                <el-input
                  v-model="userForm.introduction"
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
                        @click="handleReview(order)"
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
          <div class="tab-content" v-loading="favoritesLoading">
            <el-empty v-if="favoritesList.length === 0" description="暂无收藏" />
            <div v-else class="favorites-grid">
              <el-card 
                v-for="item in favoritesList" 
                :key="item.id"
                class="favorite-item"
                shadow="hover"
              >
                <img 
                  :src="item.productCoverImage || '/placeholder.jpg'" 
                  :alt="item.productTitle"
                  class="favorite-image"
                  @click="router.push(`/product/${item.productId}`)"
                >
                <div class="favorite-info">
                  <h4 @click="router.push(`/product/${item.productId}`)">{{ item.productTitle }}</h4>
                  <p class="favorite-price">¥{{ item.productPrice }}</p>
                  <p class="favorite-time">收藏于 {{ formatTime(item.createTime) }}</p>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="handleRemoveFavorite(item.productId)"
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
          <div class="tab-content" v-loading="reviewsLoading">
            <el-empty v-if="reviewsList.length === 0" description="暂无评价" />
            
            <div v-else class="reviews-container">
              <el-card
                v-for="review in reviewsList"
                :key="review.id"
                class="review-card"
                shadow="hover"
              >
                <div class="review-header">
                  <div class="product-info">
                    <img 
                      v-if="review.productCoverImage"
                      :src="review.productCoverImage" 
                      class="product-image"
                      @click="router.push(`/product/${review.productId}`)"
                    >
                    <div class="product-detail">
                      <h4 @click="router.push(`/product/${review.productId}`)">
                        {{ review.productTitle }}
                      </h4>
                      <el-rate 
                        :model-value="review.rating" 
                        disabled 
                        size="small"
                        :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                      />
                    </div>
                  </div>
                  <div class="review-time">{{ formatTime(review.createTime) }}</div>
                </div>
                
                <div class="review-content">{{ review.content }}</div>
                
                <div v-if="review.images" class="review-images">
                  <el-image
                    v-for="(img, index) in JSON.parse(review.images || '[]')"
                    :key="index"
                    :src="img"
                    fit="cover"
                    class="review-image-thumb"
                    :preview-src-list="JSON.parse(review.images || '[]')"
                    :initial-index="index"
                    preview-teleported
                  />
                </div>
                
                <div v-if="review.replyContent" class="merchant-reply-box">
                  <div class="reply-header">
                    <span class="reply-label">商户回复</span>
                    <span class="reply-time">{{ formatTime(review.replyTime) }}</span>
                  </div>
                  <div class="reply-content">{{ review.replyContent }}</div>
                </div>
                
                <div class="review-actions">
                  <el-button 
                    type="danger" 
                    size="small"
                    @click="handleDeleteReview(review.id)"
                  >
                    删除评价
                  </el-button>
                </div>
              </el-card>
            </div>
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
              <p class="info-text">为了您的账号安全，修改密码需要验证您的手机号</p>
              <el-form
                :model="passwordForm"
                label-width="100px"
                style="max-width: 600px"
              >
                <el-form-item label="手机号">
                  <el-input
                    v-model="passwordForm.phone"
                    placeholder="请输入手机号码进行验证"
                    maxlength="11"
                  >
                    <template #prefix>
                      <el-icon><Phone /></el-icon>
                    </template>
                  </el-input>
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

  <!-- 评价对话框 -->
  <el-dialog
    v-model="reviewDialogVisible"
    title="发表评价"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form :model="reviewForm" label-width="80px">
      <el-form-item label="产品">
        <div class="product-name">{{ reviewForm.productTitle }}</div>
      </el-form-item>
      
      <el-form-item label="评分" required>
        <el-rate 
          v-model="reviewForm.rating" 
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          show-text
          :texts="['极差', '失望', '一般', '满意', '惊喜']"
        />
      </el-form-item>
      
      <el-form-item label="评价内容" required>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="6"
          placeholder="请分享您的使用体验..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="上传图片">
        <div class="review-images-upload">
          <el-upload
            :auto-upload="false"
            :on-change="handleReviewImageUpload"
            :show-file-list="false"
            accept="image/*"
            multiple
          >
            <el-button size="small" type="primary">选择图片</el-button>
          </el-upload>
          
          <div v-if="reviewForm.images.length > 0" class="uploaded-images">
            <div 
              v-for="(img, index) in reviewForm.images" 
              :key="index"
              class="image-item"
            >
              <el-image :src="img" fit="cover" class="preview-image" />
              <el-icon class="remove-icon" @click="handleRemoveReviewImage(index)">
                <Close />
              </el-icon>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="reviewDialogVisible = false">取消</el-button>
      <el-button 
        type="primary" 
        :loading="reviewSubmitting"
        @click="handleSubmitReview"
      >
        提交评价
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { createFeedback, getMyFeedbacks } from '@/api/feedback'
import { getMyOrders, cancelOrder, finishOrder } from '@/api/order'
import { getCurrentUserInfo, updateUserInfo, changePassword, deleteAccount } from '@/api/user'
import { uploadFile } from '@/api/file'
import { getMyReviews, deleteReview, createReview } from '@/api/review'
import { getMyFavorites, removeFavorite as removeFavoriteApi, getFavoriteCount } from '@/api/favorite'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('info')

// 统计数据
const statsData = reactive({
  orderCount: 0,
  favoriteCount: 0,
  reviewCount: 0
})

const userForm = reactive({
  username: userStore.userInfo.username || '',
  nickname: userStore.userInfo.nickname || '',
  phone: userStore.userInfo.phone || '',
  email: userStore.userInfo.email || '',
  avatar: userStore.userInfo.avatar || '',
  gender: userStore.userInfo.gender || 0,
  birthday: userStore.userInfo.birthday || '',
  introduction: userStore.userInfo.introduction || ''
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

// 评价列表
const reviewsList = ref([])
const reviewsLoading = ref(false)

// 评价对话框
const reviewDialogVisible = ref(false)
const reviewForm = reactive({
  orderId: null,
  productId: null,
  productTitle: '',
  rating: 5,
  content: '',
  images: []
})
const reviewSubmitting = ref(false)

const passwordForm = reactive({
  phone: '',
  newPassword: '',
  confirmPassword: ''
})

// 收藏列表（从后端API获取）
const favoritesList = ref([])
const favoritesLoading = ref(false)

// 获取收藏列表
const fetchMyFavorites = async () => {
  favoritesLoading.value = true
  try {
    const res = await getMyFavorites({ current: 1, size: 100 })
    if (res.data && res.data.records) {
      favoritesList.value = res.data.records
    }
  } catch (error) {
    console.error('获取收藏列表失败：', error)
  } finally {
    favoritesLoading.value = false
  }
}

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义上传方法
const uploadAvatar = async ({ file }) => {
  try {
    const response = await uploadFile(file)
    if (response.data) {
      userForm.avatar = response.data
      ElMessage.success('头像上传成功')
      
      // 自动保存头像到服务器
      await updateUserInfo({
        avatar: response.data
      })
      
      // 更新本地用户信息
      userStore.updateUserInfo({ avatar: response.data })
    }
  } catch (error) {
    console.error('头像上传失败：', error)
    ElMessage.error('头像上传失败，请重试')
  }
}

// 头像上传成功回调
const handleAvatarSuccess = (response) => {
  if (response.data) {
    userForm.avatar = response.data
  }
}

// 更新个人信息
const handleUpdateInfo = async () => {
  try {
    // 调用后端API更新用户信息
    const response = await updateUserInfo({
      nickname: userForm.nickname,
      phone: userForm.phone,
      email: userForm.email,
      avatar: userForm.avatar,
      gender: userForm.gender,
      birthday: userForm.birthday,
      introduction: userForm.introduction
    })
    
    // 更新本地用户信息
    if (response.data) {
      userStore.updateUserInfo(response.data)
    }
    
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新个人信息失败：', error)
    ElMessage.error(error.response?.data?.message || '更新失败，请稍后重试')
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
  // 验证手机号
  if (!passwordForm.phone) {
    ElMessage.warning('请输入手机号码')
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(passwordForm.phone)) {
    ElMessage.warning('请输入正确的手机号码')
    return
  }
  
  // 验证新密码
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    ElMessage.warning('密码长度必须在6-20位之间')
    return
  }
  
  // 验证确认密码
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    // 调用后端API修改密码（只需手机号验证，不需要原密码）
    await changePassword({
      phone: passwordForm.phone,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功，请重新登录')
    
    // 清空表单
    passwordForm.phone = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 延迟后退出登录
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('修改密码失败：', error)
    ElMessage.error(error.response?.data?.message || '修改密码失败，请检查手机号是否正确')
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
      // 调用后端API注销账号
      await deleteAccount()
      
      ElMessage.success('账号已注销')
      userStore.logout()
      router.push('/home')
    } catch (error) {
      console.error('注销账号失败：', error)
      ElMessage.error(error.response?.data?.message || '注销失败，请稍后重试')
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

// 获取我的评价列表
const fetchMyReviews = async () => {
  reviewsLoading.value = true
  try {
    const res = await getMyReviews({ current: 1, size: 100 })
    if (res.data && res.data.records) {
      reviewsList.value = res.data.records
    }
  } catch (error) {
    console.error('获取评价列表失败：', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 删除评价
const handleDeleteReview = (reviewId) => {
  ElMessageBox.confirm('确定要删除这条评价吗？删除后无法恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReview(reviewId)
      ElMessage.success('评价已删除')
      fetchMyReviews()
      // 更新统计数据
      fetchStats()
    } catch (error) {
      console.error('删除评价失败：', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 打开评价对话框
const handleReview = (order) => {
  reviewForm.orderId = order.id
  reviewForm.productId = order.productId
  reviewForm.productTitle = order.productTitle
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.images = []
  reviewDialogVisible.value = true
}

// 提交评价
const handleSubmitReview = async () => {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  reviewSubmitting.value = true
  try {
    const data = {
      orderId: reviewForm.orderId,
      productId: reviewForm.productId,
      rating: reviewForm.rating,
      content: reviewForm.content,
      images: reviewForm.images.length > 0 ? JSON.stringify(reviewForm.images) : null
    }
    
    await createReview(data)
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    
    // 刷新订单列表和统计数据
    fetchMyOrders()
    fetchStats()
  } catch (error) {
    console.error('提交评价失败：', error)
    ElMessage.error(error.response?.data?.message || '评价失败')
  } finally {
    reviewSubmitting.value = false
  }
}

// 上传评价图片
const handleReviewImageUpload = async (file) => {
  // file.raw 是原始文件对象
  if (!file.raw) {
    ElMessage.error('文件无效')
    return false
  }
  
  try {
    const res = await uploadFile(file.raw)
    if (res.data) {
      reviewForm.images.push(res.data)
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    console.error('图片上传失败：', error)
    ElMessage.error(error.response?.data?.message || '图片上传失败')
  }
  return false // 阻止自动上传
}

// 删除评价图片
const handleRemoveReviewImage = (index) => {
  reviewForm.images.splice(index, 1)
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
const handleRemoveFavorite = async (productId) => {
  try {
    await removeFavoriteApi(productId)
    ElMessage.success('取消收藏成功')
    // 刷新收藏列表和统计数据
    fetchMyFavorites()
    fetchStats()
  } catch (error) {
    console.error('取消收藏失败：', error)
    ElMessage.error(error.response?.data?.message || '取消收藏失败')
  }
}

// 监听activeTab变化，加载对应数据
watch(activeTab, (newVal) => {
  if (newVal === 'orders') {
    fetchMyOrders()
  } else if (newVal === 'feedback') {
    fetchMyFeedbacks()
  } else if (newVal === 'reviews') {
    fetchMyReviews()
  } else if (newVal === 'favorites') {
    fetchMyFavorites()
  }
})

// 获取用户信息并填充表单
const fetchUserInfo = async () => {
  try {
    const response = await getCurrentUserInfo()
    if (response.data) {
      const userData = response.data
      
      // 更新 userStore
      userStore.updateUserInfo(userData)
      
      // 更新表单数据
      userForm.username = userData.username || ''
      userForm.nickname = userData.nickname || ''
      userForm.phone = userData.phone || ''
      userForm.email = userData.email || ''
      userForm.avatar = userData.avatar || ''
      userForm.gender = userData.gender || 0
      userForm.birthday = userData.birthday || ''
      userForm.introduction = userData.introduction || ''
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    // 如果获取失败，尝试从 localStorage 读取
    const cachedUserInfo = userStore.userInfo
    if (cachedUserInfo.id) {
      userForm.username = cachedUserInfo.username || ''
      userForm.nickname = cachedUserInfo.nickname || ''
      userForm.phone = cachedUserInfo.phone || ''
      userForm.email = cachedUserInfo.email || ''
      userForm.gender = cachedUserInfo.gender || 0
      userForm.birthday = cachedUserInfo.birthday || ''
      userForm.introduction = cachedUserInfo.introduction || ''
    }
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 获取订单数量
    const orderRes = await getMyOrders({ current: 1, size: 1 })
    statsData.orderCount = orderRes.data?.total || 0
    
    // 获取收藏数量（从API）
    const favoriteCountRes = await getFavoriteCount()
    statsData.favoriteCount = favoriteCountRes.data || 0

    // 获取评价数量
    const reviewRes = await getMyReviews({ current: 1, size: 1 })
    statsData.reviewCount = reviewRes.data?.total || 0
  } catch (error) {
    console.error('获取统计数据失败：', error)
  }
}

onMounted(() => {
  // 获取用户信息
  fetchUserInfo()
  
  // 获取统计数据
  fetchStats()
  
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

/* 头像上传样式 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
}

.avatar-tip p {
  margin: 4px 0;
}

.user-info-card {
  background: linear-gradient(135deg, #5B8DB8 0%, #7BA7CC 50%, #A8C5DD 100%);
  border-radius: 12px;
  padding: 40px;
  display: flex;
  align-items: center;
  gap: 32px;
  box-shadow: 0 8px 24px rgba(91, 141, 184, 0.25);
  position: relative;
  overflow: hidden;
}

.user-info-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.user-info-card :deep(.el-avatar) {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  border: 3px solid rgba(255, 255, 255, 0.5);
  z-index: 1;
  position: relative;
}

.user-details {
  color: #fff;
  z-index: 1;
  position: relative;
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
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
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

.info-text {
  color: #909399;
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 1.5;
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

/* 评价列表样式 */
.reviews-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.review-card .review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.review-card .product-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.review-card .product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.review-card .product-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.review-card .product-detail h4 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0;
  cursor: pointer;
}

.review-card .product-detail h4:hover {
  color: #409EFF;
}

.review-card .review-time {
  font-size: 14px;
  color: #999;
}

.review-card .review-content {
  font-size: 15px;
  line-height: 1.6;
  color: #666;
  margin-bottom: 12px;
}

.review-card .review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-card .review-image-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  cursor: pointer;
}

.review-card .merchant-reply-box {
  background: #f7f8fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.review-card .reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.review-card .reply-label {
  font-size: 14px;
  font-weight: 500;
  color: #409EFF;
}

.review-card .reply-time {
  font-size: 12px;
  color: #999;
}

.review-card .reply-content {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}

.review-card .review-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 评价对话框样式 */
.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.review-images-upload {
  width: 100%;
}

.uploaded-images {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.uploaded-images .image-item {
  position: relative;
  width: 80px;
  height: 80px;
}

.uploaded-images .preview-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.uploaded-images .remove-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #f56c6c;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
}

.uploaded-images .remove-icon:hover {
  background: #f78989;
}
</style>

