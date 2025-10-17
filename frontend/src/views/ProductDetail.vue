<template>
  <div class="product-detail" v-loading="loading">
    <div class="detail-content" v-if="product.id">
      <!-- 返回按钮 -->
      <el-button
        type="primary"
        link
        :icon="ArrowLeft"
        @click="router.back()"
        class="back-btn"
      >
        返回
      </el-button>

      <!-- 产品主要信息 -->
      <div class="product-main">
        <!-- 左侧图片 -->
        <div class="product-images">
          <div class="main-image">
            <img :src="currentImage" :alt="product.title">
          </div>
          <div class="image-list" v-if="imageList.length > 1">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="image-item"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" :alt="product.title">
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="product-details">
          <h1 class="product-title">{{ product.title }}</h1>
          
          <div class="product-rating">
            <el-rate
              v-model="product.rating"
              disabled
              show-score
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            />
            <span class="sales-count">已售 {{ product.sales || 0 }} 件</span>
          </div>

          <div class="product-price-box">
            <div class="price-main">
              <span class="label">价格</span>
              <span class="price">¥{{ product.price }}</span>
              <span class="original-price" v-if="product.originalPrice">
                ¥{{ product.originalPrice }}
              </span>
            </div>
          </div>

          <div class="product-info-list">
            <div class="info-item" v-if="product.region">
              <span class="label">所在区域：</span>
              <span class="value">{{ product.region }}</span>
            </div>
            <div class="info-item" v-if="product.address">
              <span class="label">详细地址：</span>
              <span class="value">{{ product.address }}</span>
            </div>
            <div class="info-item" v-if="product.stock">
              <span class="label">剩余库存：</span>
              <span class="value">{{ product.stock }} 件</span>
            </div>
          </div>

          <div class="product-tags" v-if="tagList.length > 0">
            <el-tag v-for="tag in tagList" :key="tag" type="info" size="small">
              {{ tag }}
            </el-tag>
          </div>

          <div class="product-actions">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock || 999"
              size="large"
            />
            <el-button
              type="primary"
              size="large"
              :icon="ShoppingCart"
              @click="handleBuyNow"
              :disabled="product.status !== 1"
            >
              立即预订
            </el-button>
            <el-button
              size="large"
              :icon="Star"
              :type="isFavorite ? 'warning' : 'default'"
              @click="handleCollect"
            >
              {{ isFavorite ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 产品详细描述 -->
      <div class="product-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="产品详情" name="detail">
            <div class="tab-content">
              <h3>产品描述</h3>
              <p class="description">{{ product.description }}</p>
              
              <div v-if="product.features">
                <h3>产品特色</h3>
                <div class="features" v-html="product.features"></div>
              </div>
              
              <div v-if="product.notice">
                <h3>预订须知</h3>
                <div class="notice" v-html="product.notice"></div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="用户评价" name="reviews">
            <div class="tab-content">
              <div class="reviews-summary">
                <div class="rating-score">
                  <span class="score">{{ product.rating || 5.0 }}</span>
                  <el-rate
                    v-model="product.rating"
                    disabled
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  />
                  <span class="review-count">共 {{ reviewsTotal }} 条评价</span>
                </div>
              </div>
              
              <div class="reviews-list" v-loading="reviewsLoading">
                <el-empty v-if="reviews.length === 0" description="暂无评价" />
                
                <div v-else>
                  <div 
                    v-for="review in reviews" 
                    :key="review.id"
                    class="review-item"
                  >
                    <div class="review-header">
                      <div class="user-info">
                        <el-avatar :size="40">
                          {{ review.userNickname ? review.userNickname.charAt(0) : '用' }}
                        </el-avatar>
                        <div class="user-detail">
                          <div class="user-name">{{ review.userNickname || '匿名用户' }}</div>
                          <el-rate 
                            :model-value="review.rating" 
                            disabled 
                            size="small"
                            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                          />
                        </div>
                      </div>
                      <div class="review-date">{{ formatReviewTime(review.createTime) }}</div>
                    </div>
                    
                    <div class="review-content">{{ review.content }}</div>
                    
                    <div v-if="review.images" class="review-images">
                      <el-image
                        v-for="(img, index) in JSON.parse(review.images || '[]')"
                        :key="index"
                        :src="img"
                        fit="cover"
                        class="review-image"
                        :preview-src-list="JSON.parse(review.images || '[]')"
                        :initial-index="index"
                        preview-teleported
                      />
                    </div>
                    
                    <div v-if="review.replyContent" class="merchant-reply">
                      <div class="reply-label">商户回复：</div>
                      <div class="reply-content">{{ review.replyContent }}</div>
                      <div class="reply-time">{{ formatReviewTime(review.replyTime) }}</div>
                    </div>
                  </div>
                  
                  <el-pagination
                    v-if="reviewsTotal > reviewsPageSize"
                    class="review-pagination"
                    :current-page="reviewsPage"
                    :page-size="reviewsPageSize"
                    :total="reviewsTotal"
                    layout="prev, pager, next"
                    @current-change="handleReviewPageChange"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 预订对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="确认预订"
      width="500px"
    >
      <div class="order-confirm">
        <div class="order-item">
          <span class="label">产品名称：</span>
          <span>{{ product.title }}</span>
        </div>
        <div class="order-item">
          <span class="label">预订数量：</span>
          <span>{{ quantity }}</span>
        </div>
        <div class="order-item">
          <span class="label">单价：</span>
          <span class="price">¥{{ product.price }}</span>
        </div>
        <div class="order-item total">
          <span class="label">总价：</span>
          <span class="price">¥{{ (product.price * quantity).toFixed(2) }}</span>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOrder">确认预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProductById } from '@/api/product'
import { createOrder, payOrder } from '@/api/order'
import { getProductReviews } from '@/api/review'
import { addFavorite, removeFavorite, checkFavorite, toggleFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const product = ref({})
const quantity = ref(1)
const currentImage = ref('')
const activeTab = ref('detail')
const orderDialogVisible = ref(false)
const isFavorite = ref(false)

// 评价相关
const reviews = ref([])
const reviewsLoading = ref(false)
const reviewsTotal = ref(0)
const reviewsPage = ref(1)
const reviewsPageSize = ref(10)

// 图片列表
const imageList = computed(() => {
  const images = []
  if (product.value.coverImage) {
    images.push(product.value.coverImage)
  }
  if (product.value.images) {
    try {
      const parsed = JSON.parse(product.value.images)
      images.push(...parsed)
    } catch (e) {
      // 忽略解析错误
    }
  }
  return images.length > 0 ? images : ['/placeholder.jpg']
})

// 标签列表
const tagList = computed(() => {
  if (!product.value.tags) return []
  try {
    return JSON.parse(product.value.tags)
  } catch (e) {
    return []
  }
})

// 获取产品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const res = await getProductById(route.params.id)
    if (res.data) {
      product.value = res.data
      currentImage.value = imageList.value[0]
    }
  } catch (error) {
    console.error('获取产品详情失败：', error)
    ElMessage.error('产品不存在或已下架')
    router.back()
  } finally {
    loading.value = false
  }
}

// 获取评价列表
const fetchReviews = async () => {
  if (!route.params.id) return
  
  reviewsLoading.value = true
  try {
    const res = await getProductReviews(route.params.id, {
      current: reviewsPage.value,
      size: reviewsPageSize.value
    })
    if (res.data) {
      reviews.value = res.data.records || []
      reviewsTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取评价列表失败：', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 评价分页改变
const handleReviewPageChange = (page) => {
  reviewsPage.value = page
  fetchReviews()
}

// 格式化时间
const formatReviewTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

// 立即购买
const handleBuyNow = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  orderDialogVisible.value = true
}

// 确认订单
const confirmOrder = async () => {
  try {
    // 创建订单
    const orderData = {
      productId: product.value.id,
      quantity: quantity.value,
      contactName: userStore.userInfo.nickname || userStore.userInfo.username,
      contactPhone: userStore.userInfo.phone || '未填写',
      useDate: '',
      useCount: quantity.value,
      remark: ''
    }
    
    const createRes = await createOrder(orderData)
    
    if (createRes.data) {
      orderDialogVisible.value = false
      ElMessage.success('订单创建成功！')
      
      // 模拟支付
      setTimeout(async () => {
        try {
          await payOrder(createRes.data.id)
          ElMessage.success('支付成功！您可以在个人中心查看订单')
        } catch (error) {
          console.error('支付失败：', error)
        }
      }, 500)
    }
  } catch (error) {
    console.error('创建订单失败：', error)
  }
}

// 检查是否已收藏（从API）
const checkIsFavoriteStatus = async () => {
  if (!userStore.isLoggedIn || !product.value.id) {
    isFavorite.value = false
    return
  }
  
  try {
    const res = await checkFavorite(product.value.id)
    isFavorite.value = res.data || false
  } catch (error) {
    console.error('检查收藏状态失败：', error)
    isFavorite.value = false
  }
}

// 收藏/取消收藏（使用智能切换API）
const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    // 使用智能切换API，后端会自动判断当前状态并切换
    const res = await toggleFavorite(product.value.id)
    isFavorite.value = res.data
    
    if (res.data) {
      ElMessage.success('收藏成功')
    } else {
      ElMessage.success('取消收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败：', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  fetchProductDetail()
  fetchReviews()
  // 页面加载时检查收藏状态
  if (userStore.isLoggedIn) {
    checkIsFavoriteStatus()
  }
})

// 监听产品变化，更新收藏状态
watch(() => product.value.id, (newId) => {
  if (newId && userStore.isLoggedIn) {
    checkIsFavoriteStatus()
  }
})

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn && product.value.id) {
    checkIsFavoriteStatus()
  } else {
    isFavorite.value = false
  }
})

// 监听切换到评价标签时刷新评价列表
watch(() => activeTab.value, (newTab) => {
  if (newTab === 'reviews') {
    fetchReviews()
  }
})
</script>

<style scoped>
.product-detail {
  max-width: 1400px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 24px;
}

.detail-content {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  margin-bottom: 48px;
}

.product-images {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.main-image {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
  border: 1px solid #f0f0f0;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.image-item {
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.image-item.active {
  border-color: #409EFF;
}

.image-item:hover {
  border-color: #409EFF;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.sales-count {
  color: #999;
  font-size: 14px;
}

.product-price-box {
  background: #f7f8fa;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-main .label {
  font-size: 14px;
  color: #666;
}

.price-main .price {
  font-size: 32px;
  font-weight: 600;
  color: #f56c6c;
}

.price-main .original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.product-info-list {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  padding: 12px 0;
  font-size: 14px;
}

.info-item .label {
  color: #666;
  min-width: 100px;
}

.info-item .value {
  color: #333;
}

.product-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 32px;
}

.product-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.product-tabs {
  margin-top: 48px;
}

.tab-content {
  padding: 24px 0;
}

.tab-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 24px 0 16px;
}

.tab-content h3:first-child {
  margin-top: 0;
}

.description {
  font-size: 15px;
  line-height: 1.8;
  color: #666;
}

.features, .notice {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
}

.reviews-summary {
  padding: 24px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.rating-score {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rating-score .score {
  font-size: 48px;
  font-weight: 600;
  color: #f56c6c;
}

.review-count {
  margin-left: 16px;
  font-size: 14px;
  color: #666;
}

.review-item {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.review-date {
  font-size: 14px;
  color: #999;
}

.review-content {
  font-size: 15px;
  line-height: 1.6;
  color: #666;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
}

.merchant-reply {
  background: #f7f8fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.reply-label {
  font-size: 14px;
  font-weight: 500;
  color: #409EFF;
  margin-bottom: 8px;
}

.reply-content {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  margin-bottom: 4px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.review-pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.order-confirm {
  padding: 24px 0;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 15px;
}

.order-item .label {
  color: #666;
}

.order-item .price {
  color: #f56c6c;
  font-weight: 600;
}

.order-item.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 18px;
}

.order-item.total .price {
  font-size: 24px;
}

@media (max-width: 768px) {
  .product-main {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .product-images {
    position: static;
  }
}
</style>

