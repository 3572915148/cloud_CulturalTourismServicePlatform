<template>
  <div class="ceramic-page">
    <div class="page-header">
      <h1>陶瓷文化</h1>
      <p>千年瓷都，匠心传承</p>
    </div>

    <div class="ceramic-content">
      <!-- 分类导航 -->
      <div class="category-tabs">
        <el-radio-group v-model="activeCategory" @change="handleCategoryChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="history">陶瓷历史</el-radio-button>
          <el-radio-button label="craft">制作工艺</el-radio-button>
          <el-radio-button label="master">陶瓷名家</el-radio-button>
          <el-radio-button label="culture">文化传承</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 内容列表 -->
      <div class="content-list" v-loading="loading">
        <div
          v-for="item in contentList"
          :key="item.id"
          class="content-card"
          @click="handleViewDetail(item)"
        >
          <div class="content-image">
            <img :src="item.coverImage || '/placeholder.jpg'" :alt="item.title">
            <div class="content-category">
              {{ getCategoryLabel(item.category) }}
            </div>
          </div>
          <div class="content-info">
            <h3 class="content-title">{{ item.title }}</h3>
            <p class="content-summary">{{ item.summary }}</p>
            <div class="content-meta">
              <span class="author">{{ item.author }}</span>
              <span class="date">{{ formatDate(item.publishTime) }}</span>
              <span class="views">
                <el-icon><View /></el-icon>
                {{ item.views || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[9, 18, 27, 36]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchContent"
          @current-change="fetchContent"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentContent.title"
      width="800px"
      top="5vh"
    >
      <div class="content-detail">
        <div class="detail-meta">
          <el-tag type="info">{{ getCategoryLabel(currentContent.category) }}</el-tag>
          <span class="author">作者：{{ currentContent.author }}</span>
          <span class="date">{{ formatDate(currentContent.publishTime) }}</span>
        </div>
        
        <div class="detail-image" v-if="currentContent.coverImage">
          <img :src="currentContent.coverImage" :alt="currentContent.title">
        </div>
        
        <div class="detail-content" v-html="currentContent.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const activeCategory = ref('all')
const contentList = ref([])
const detailDialogVisible = ref(false)
const currentContent = ref({})

const pagination = reactive({
  current: 1,
  size: 9,
  total: 0
})

// 模拟数据
const mockData = [
  {
    id: 1,
    title: '景德镇陶瓷的千年历史',
    summary: '景德镇陶瓷历史悠久，始于汉世，盛于唐宋，鼎盛于明清。作为中国陶瓷艺术的重要发源地之一...',
    category: 'history',
    author: '陶瓷研究院',
    publishTime: '2024-01-15',
    views: 1234,
    coverImage: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&auto=format&fit=crop',
    content: '<p>景德镇陶瓷历史悠久，始于汉世，盛于唐宋，鼎盛于明清...</p>'
  },
  {
    id: 2,
    title: '青花瓷的制作工艺',
    summary: '青花瓷是景德镇四大传统名瓷之一，其制作工艺精湛，需要经过选料、练泥、拉坯、印坯、利坯等72道工序...',
    category: 'craft',
    author: '大师工作室',
    publishTime: '2024-02-01',
    views: 856,
    coverImage: 'https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=800&auto=format&fit=crop',
    content: '<p>青花瓷是景德镇四大传统名瓷之一...</p>'
  },
  {
    id: 3,
    title: '当代陶瓷艺术名家',
    summary: '景德镇汇聚了众多陶瓷艺术大师，他们继承传统技艺的同时，不断创新，为陶瓷艺术注入新的活力...',
    category: 'master',
    author: '艺术评论家',
    publishTime: '2024-02-10',
    views: 678,
    coverImage: 'https://images.unsplash.com/photo-1525974160448-038dacadcc71?w=800&auto=format&fit=crop',
    content: '<p>景德镇汇聚了众多陶瓷艺术大师...</p>'
  },
  {
    id: 4,
    title: '陶瓷文化的传承与创新',
    summary: '在保护和传承传统陶瓷文化的同时，景德镇也在积极探索创新之路，让千年瓷都焕发新的生机...',
    category: 'culture',
    author: '文化学者',
    publishTime: '2024-02-20',
    views: 945,
    coverImage: 'https://images.unsplash.com/photo-1493106819501-66d381c466f1?w=800&auto=format&fit=crop',
    content: '<p>在保护和传承传统陶瓷文化的同时...</p>'
  },
  {
    id: 5,
    title: '明清官窑瓷器欣赏',
    summary: '明清时期是景德镇陶瓷发展的鼎盛时期，官窑瓷器代表了当时最高的制作水平...',
    category: 'history',
    author: '陶瓷博物馆',
    publishTime: '2024-03-01',
    views: 1567,
    coverImage: 'https://images.unsplash.com/photo-1610725664285-7c57e6eeac3f?w=800&auto=format&fit=crop',
    content: '<p>明清时期是景德镇陶瓷发展的鼎盛时期...</p>'
  },
  {
    id: 6,
    title: '釉下彩绘技艺解析',
    summary: '釉下彩绘是景德镇传统装饰技法之一，需要在素坯上进行绘画，再施釉后高温烧制...',
    category: 'craft',
    author: '工艺传承人',
    publishTime: '2024-03-10',
    views: 723,
    coverImage: 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=800&auto=format&fit=crop',
    content: '<p>釉下彩绘是景德镇传统装饰技法之一...</p>'
  }
]

const categoryLabels = {
  history: '陶瓷历史',
  craft: '制作工艺',
  master: '陶瓷名家',
  culture: '文化传承'
}

const getCategoryLabel = (category) => {
  return categoryLabels[category] || '其他'
}

const formatDate = (date) => {
  return date || '-'
}

// 获取内容列表
const fetchContent = () => {
  loading.value = true
  
  setTimeout(() => {
    let filteredData = mockData
    if (activeCategory.value !== 'all') {
      filteredData = mockData.filter(item => item.category === activeCategory.value)
    }
    
    const start = (pagination.current - 1) * pagination.size
    const end = start + pagination.size
    
    contentList.value = filteredData.slice(start, end)
    pagination.total = filteredData.length
    loading.value = false
  }, 500)
}

// 切换分类
const handleCategoryChange = () => {
  pagination.current = 1
  fetchContent()
}

// 查看详情
const handleViewDetail = (item) => {
  currentContent.value = item
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchContent()
})
</script>

<style scoped>
.ceramic-page {
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

.ceramic-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-tabs {
  margin-bottom: 32px;
  text-align: center;
}

.content-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
  min-height: 400px;
}

.content-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.content-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409EFF;
}

.content-image {
  width: 100%;
  height: 220px;
  position: relative;
  overflow: hidden;
}

.content-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.content-card:hover .content-image img {
  transform: scale(1.1);
}

.content-category {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
}

.content-info {
  padding: 20px;
}

.content-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.content-meta .author {
  color: #666;
}

.content-meta .views {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}

.pagination {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}

.content-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #666;
}

.detail-image {
  width: 100%;
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.detail-image img {
  width: 100%;
  height: auto;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
}

.detail-content :deep(p) {
  margin-bottom: 16px;
}
</style>

