<template>
  <div class="home">
    <!-- Banner轮播 -->
    <div class="banner">
      <el-carousel height="400px" :interval="5000">
        <el-carousel-item v-for="(item, index) in banners" :key="index">
          <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="banner-content">
              <h2>{{ item.title }}</h2>
              <p>{{ item.description }}</p>
              <el-button type="primary" size="large" @click="router.push('/products')">
                立即探索
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <!-- 分类导航 -->
      <div class="section">
        <h2 class="section-title">热门分类</h2>
        <div class="categories">
          <div
            v-for="item in categories"
            :key="item.id"
            class="category-item"
            @click="router.push({ path: '/products', query: { categoryId: item.id } })"
          >
            <el-icon :size="48" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
            <h3>{{ item.name }}</h3>
            <p>{{ item.count }}+ 产品</p>
          </div>
        </div>
      </div>

      <!-- AI助手入口 -->
      <div class="section" v-if="userStore.isLoggedIn">
        <h2 class="section-title">AI智能推荐</h2>
        <div class="ai-assistant-card">
          <div class="ai-card-content">
            <div class="ai-icon">
              <el-icon size="48" color="#409eff"><ChatDotRound /></el-icon>
            </div>
            <div class="ai-info">
              <h3>景德镇AI文旅助手</h3>
              <p>告诉我您的需求，我会为您推荐最合适的文旅产品</p>
              <el-button type="primary" @click="router.push('/ai-chat')">
                开始对话
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐产品 -->
      <div class="section">
        <div class="section-header">
          <h2 class="section-title">推荐产品</h2>
          <el-button type="primary" link @click="router.push('/products')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        
        <div class="products" v-loading="loading">
          <div
            v-for="product in recommendProducts"
            :key="product.id"
            class="product-card"
            @click="router.push(`/product/${product.id}`)"
          >
            <div class="product-image">
              <img :src="product.coverImage || '/placeholder.jpg'" :alt="product.title">
              <div class="product-tag" v-if="product.recommend">推荐</div>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <div class="product-price">
                  <span class="price">¥{{ product.price }}</span>
                  <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                </div>
                <div class="product-rating">
                  <el-rate
                    v-model="product.rating"
                    disabled
                    show-score
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商户入驻 -->
      <div class="section merchant-section">
        <el-card class="merchant-card" shadow="hover">
          <div class="merchant-content">
            <div class="merchant-left">
              <el-icon :size="80" color="#F56C6C"><Shop /></el-icon>
            </div>
            <div class="merchant-center">
              <h2>商户入驻</h2>
              <p>加入景德镇文旅服务平台，开启您的线上业务</p>
              <ul class="merchant-benefits">
                <li><el-icon><Check /></el-icon> 免费入驻，快速开店</li>
                <li><el-icon><Check /></el-icon> 海量用户，精准营销</li>
                <li><el-icon><Check /></el-icon> 专业服务，贴心支持</li>
              </ul>
            </div>
            <div class="merchant-right">
              <el-button type="warning" size="large" @click="router.push('/merchant/register')">
                立即入驻
              </el-button>
              <el-button type="default" size="large" @click="router.push('/merchant/login')">
                商户登录
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 平台特色 -->
      <div class="section">
        <h2 class="section-title">平台特色</h2>
        <div class="features">
          <div class="feature-item">
            <el-icon :size="56" color="#409EFF"><ShoppingCart /></el-icon>
            <h3>精选产品</h3>
            <p>严选景德镇优质文旅资源</p>
          </div>
          <div class="feature-item">
            <el-icon :size="56" color="#67C23A"><Document /></el-icon>
            <h3>文化传承</h3>
            <p>传播千年陶瓷文化精髓</p>
          </div>
          <div class="feature-item">
            <el-icon :size="56" color="#E6A23C"><Star /></el-icon>
            <h3>品质保证</h3>
            <p>提供优质的旅游服务体验</p>
          </div>
          <div class="feature-item">
            <el-icon :size="56" color="#F56C6C"><Service /></el-icon>
            <h3>贴心服务</h3>
            <p>7×24小时客服在线支持</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getProductList } from '@/api/product'
import { ElMessage } from 'element-plus'
import { Shop, Check, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const recommendProducts = ref([])

const banners = [
  {
    title: '探索千年瓷都魅力',
    description: '感受景德镇独特的陶瓷文化与艺术氛围',
    image: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&auto=format&fit=crop'
  },
  {
    title: '品味传统工艺之美',
    description: '近距离接触大师作品，体验制瓷乐趣',
    image: 'https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=1200&auto=format&fit=crop'
  },
  {
    title: '开启文化之旅',
    description: '精心策划的文旅线路，带您深度游览景德镇',
    image: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=1200&auto=format&fit=crop'
  }
]

const categories = [
  { id: 1, name: '景点门票', icon: 'Place', color: '#409EFF', count: 50 },
  { id: 2, name: '酒店住宿', icon: 'House', color: '#67C23A', count: 120 },
  { id: 3, name: '特色美食', icon: 'Food', color: '#E6A23C', count: 80 },
  { id: 4, name: '陶瓷体验', icon: 'Van', color: '#F56C6C', count: 30 },
  { id: 5, name: '文化活动', icon: 'Ticket', color: '#909399', count: 40 },
  { id: 6, name: '旅游套餐', icon: 'Suitcase', color: '#C45656', count: 25 }
]

// 获取推荐产品
const fetchRecommendProducts = async () => {
  loading.value = true
  try {
    const res = await getProductList({
      current: 1,
      size: 8
    })
    if (res.data && res.data.records) {
      recommendProducts.value = res.data.records.map(item => ({
        ...item,
        rating: item.rating || 5.0
      }))
    }
  } catch (error) {
    console.error('获取推荐产品失败：', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecommendProducts()
})
</script>

<style scoped>
.home {
  margin: -24px;
}

.banner {
  margin-bottom: 48px;
}

.banner-item {
  height: 400px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.banner-content h2 {
  font-size: 48px;
  margin-bottom: 16px;
  font-weight: 600;
}

.banner-content p {
  font-size: 20px;
  margin-bottom: 32px;
}

.content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.section {
  margin-bottom: 64px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin-bottom: 32px;
}

.categories {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 24px;
}

.category-item {
  background: #fff;
  padding: 32px;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.category-item h3 {
  font-size: 18px;
  color: #333;
  margin: 16px 0 8px;
}

.category-item p {
  font-size: 14px;
  color: #999;
}

.products {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  position: relative;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.product-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #f56c6c;
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

/* AI助手卡片样式 */
.ai-assistant-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.ai-card-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.ai-icon {
  flex-shrink: 0;
}

.ai-info h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.ai-info p {
  margin: 0 0 16px 0;
  font-size: 16px;
  opacity: 0.9;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 32px;
}

.feature-item {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.feature-item h3 {
  font-size: 20px;
  color: #333;
  margin: 16px 0 8px;
}

.feature-item p {
  font-size: 14px;
  color: #666;
}

/* 商户入驻卡片 */
.merchant-section {
  margin: 48px 0;
}

.merchant-card {
  background: linear-gradient(135deg, #fff5f5 0%, #fff9e6 100%);
  border: none;
}

.merchant-content {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 20px;
}

.merchant-left {
  flex-shrink: 0;
}

.merchant-center {
  flex: 1;
}

.merchant-center h2 {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.merchant-center > p {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.merchant-benefits {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.merchant-benefits li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.merchant-benefits li .el-icon {
  color: #67C23A;
}

.merchant-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.merchant-right .el-button {
  width: 160px;
}

@media (max-width: 768px) {
  .merchant-content {
    flex-direction: column;
    text-align: center;
  }

  .merchant-right {
    width: 100%;
  }

  .merchant-right .el-button {
    width: 100%;
  }
}
</style>

