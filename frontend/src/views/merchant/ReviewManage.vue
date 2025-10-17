<template>
  <div class="review-manage">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">评价管理</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 24px">
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="全部" style="width: 140px" @change="fetchData">
          <el-option label="全部" :value="undefined"/>
          <el-option label="已回复" :value="'replied'"/>
          <el-option label="未回复" :value="'unreplied'"/>
        </el-select>
      </div>

      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="productTitle" label="产品" min-width="180"/>
        <el-table-column prop="username" label="用户" width="120"/>
        <el-table-column prop="rating" label="评分" width="100"/>
        <el-table-column prop="content" label="内容" min-width="260"/>
        <el-table-column label="回复" min-width="220">
          <template #default="{ row }">
            <div v-if="row.replyContent">
              <div class="reply-content">{{ row.replyContent }}</div>
              <div class="reply-time">{{ row.replyTime }}</div>
            </div>
            <el-button v-else link type="primary" @click="openReply(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="query.size"
          :current-page.sync="query.current"
          @current-change="(p)=>{query.current=p;fetchData()}"
        />
      </div>
    </el-card>

    <el-dialog v-model="replyVisible" title="回复评价" width="520px">
      <el-input v-model="replyContent" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="请输入回复内容"/>
      <template #footer>
        <el-button @click="replyVisible=false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMyReviews, replyReview } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const router = useRouter()

const query = reactive({ current: 1, size: 10, status: undefined })
const list = ref([])
const total = ref(0)

const fetchData = async () => {
  const res = await listMyReviews(query)
  let records = res.data?.records || []
  if (query.status === 'replied') {
    records = records.filter(r => !!r.replyContent)
  } else if (query.status === 'unreplied') {
    records = records.filter(r => !r.replyContent)
  }
  list.value = records
  total.value = Number(res.data?.total || records.length)
}

onMounted(fetchData)

const replyVisible = ref(false)
const replying = ref(false)
const replyingRow = ref(null)
const replyContent = ref('')

const openReply = (row) => {
  replyingRow.value = row
  replyContent.value = ''
  replyVisible.value = true
}

const handleReply = async () => {
  if (!replyContent.value.trim()) return
  replying.value = true
  try {
    await replyReview(replyingRow.value.id, replyContent.value)
    ElMessage.success('回复成功')
    replyVisible.value = false
    fetchData()
  } finally {
    replying.value = false
  }
}
</script>

<style scoped>
.review-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.toolbar { margin-bottom: 12px; display: flex; gap: 12px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
.reply-content { color: #333; }
.reply-time { color: #999; font-size: 12px; }
</style>

