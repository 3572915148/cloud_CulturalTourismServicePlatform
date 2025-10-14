<template>
  <div class="products-page">
    <div class="page-header">
      <h1>文旅产品</h1>
      <p>发现景德镇优质旅游资源</p>
    </div>

    <div class="products-content">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-item">
          <span class="filter-label">分类：</span>
          <el-radio-group v-model="filters.categoryId" @change="handleFilterChange">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="1">景点门票</el-radio-button>
            <el-radio-button :label="2">酒店住宿</el-radio-button>
            <el-radio-button :label="3">特色美食</el-radio-button>
            <el-radio-button :label="4">陶瓷体验</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <span class="filter-label">区域：</span>
          <el-select
            v-model="filters.region"
            placeholder="选择区域"
            clearable
            @change="handleFilterChange"
            style="width: 200px"
          >
            <el-option label="珠山区" value="珠山区" />
            <el-option label="昌江区" value="昌江区" />
            <el-option label="浮梁县" value="浮梁县" />
            <el-option label="乐平市" value="乐平市" />
          </el-select>
        </div>

        <div class="filter-item">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索产品名称"
            clearable
            @keyup.enter="handleFilterChange"
            style="width: 300px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button :icon="Search" @click="handleFilterChange" />
            </template>
          </el-input>
        </div>
      </div>

      <!-- 产品列表 -->
      <div class="products-list" v-loading="loading">
        <el-empty v-if="!loading && products.length === 0" description="暂无产品数据" />
        
        <div v-else class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="router.push(`/product/${product.id}`)"
          >
            <div class="product-image">
              <img :src="product.coverImage || '/placeholder.jpg'" :alt="product.title">
              <div class="product-tags">
                <el-tag v-if="product.recommend" type="danger" size="small">推荐</el-tag>
                <el-tag v-if="product.status === 1" type="success" size="small">在售</el-tag>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              
              <div class="product-meta">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ product.region || '景德镇' }}</span>
                </div>
                <div class="meta-item">
                  <el-rate
                    v-model="product.rating"
                    disabled
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  />
                  <span>{{ product.rating || 5.0 }}</span>
                </div>
              </div>

              <div class="product-footer">
                <div class="product-price">
                  <span class="price">¥{{ product.price }}</span>
                  <span class="original-price" v-if="product.originalPrice">
                    ¥{{ product.originalPrice }}
                  </span>
                </div>
                <div class="product-sales">
                  已售{{ product.sales || 0 }}件
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 30, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchProducts"
            @current-change="fetchProducts"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProductList } from '@/api/product'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const products = ref([])

const filters = reactive({
  categoryId: null,
  region: '',
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 12,
  total: 0
})

// 获取产品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...filters
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') {
        delete params[key]
      }
    })
    
    const res = await getProductList(params)
    if (res.data) {
      products.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取产品列表失败：', error)
  } finally {
    loading.value = false
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  pagination.current = 1
  fetchProducts()
}

onMounted(() => {
  // 从路由参数中获取分类ID
  if (route.query.categoryId) {
    filters.categoryId = parseInt(route.query.categoryId)
  }
  fetchProducts()
})
</script>

<style scoped>
.products-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.page-header h1 {
  font-size: 36px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}

.products-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.filter-bar {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.filter-label {
  font-weight: 500;
  color: #333;
  margin-right: 16px;
  min-width: 60px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.product-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409EFF;
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

.product-tags {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
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
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 40px;
}

.product-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
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

.product-sales {
  font-size: 13px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}
</style>

