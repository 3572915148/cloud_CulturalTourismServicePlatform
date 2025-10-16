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
              <span class="date">{{ formatDate(item.createTime) }}</span>
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
          <span class="date">{{ formatDate(currentContent.createTime) }}</span>
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
import { getCeramicContentList, incrementCeramicViews } from '@/api/ceramic'
import { ElMessage } from 'element-plus'

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
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取内容列表
const fetchContent = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    
    // 如果不是"全部"分类，添加category参数
    if (activeCategory.value !== 'all') {
      params.category = activeCategory.value
    }
    
    const res = await getCeramicContentList(params)
    if (res.data) {
      contentList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取陶瓷文化内容失败：', error)
    ElMessage.error('获取内容失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 切换分类
const handleCategoryChange = () => {
  pagination.current = 1
  fetchContent()
}

// 查看详情
const handleViewDetail = async (item) => {
  currentContent.value = item
  detailDialogVisible.value = true
  
  // 增加浏览量
  try {
    await incrementCeramicViews(item.id)
    // 更新本地浏览量显示
    item.views = (item.views || 0) + 1
  } catch (error) {
    console.error('增加浏览量失败：', error)
    // 不影响用户体验，静默失败
  }
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

