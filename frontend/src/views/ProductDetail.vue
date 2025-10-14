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
                </div>
              </div>
              
              <div class="reviews-list">
                <el-empty description="暂无评价" />
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

// 获取收藏列表（从localStorage）
const getFavorites = () => {
  const favorites = localStorage.getItem('favorites')
  return favorites ? JSON.parse(favorites) : []
}

// 保存收藏列表（到localStorage）
const saveFavorites = (favorites) => {
  localStorage.setItem('favorites', JSON.stringify(favorites))
}

// 检查是否已收藏
const checkIsFavorite = () => {
  if (!userStore.isLoggedIn || !product.value.id) return false
  const favorites = getFavorites()
  return favorites.some(item => item.id === product.value.id && item.userId === userStore.userInfo.id)
}

// 收藏/取消收藏
const handleCollect = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const favorites = getFavorites()
  const index = favorites.findIndex(item => 
    item.id === product.value.id && item.userId === userStore.userInfo.id
  )
  
  if (index > -1) {
    // 已收藏，取消收藏
    favorites.splice(index, 1)
    isFavorite.value = false
    ElMessage.success('取消收藏成功')
  } else {
    // 未收藏，添加收藏
    favorites.push({
      id: product.value.id,
      userId: userStore.userInfo.id,
      title: product.value.title,
      price: product.value.price,
      coverImage: product.value.coverImage,
      createTime: new Date().toISOString()
    })
    isFavorite.value = true
    ElMessage.success('收藏成功')
  }
  
  saveFavorites(favorites)
}

onMounted(() => {
  fetchProductDetail()
})

// 监听产品变化，更新收藏状态
watch(() => product.value.id, () => {
  isFavorite.value = checkIsFavorite()
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

