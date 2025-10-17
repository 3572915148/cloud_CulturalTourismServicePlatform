<template>
  <div class="product-manage">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">产品管理</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 24px">
      <div class="toolbar">
        <el-button type="primary" @click="openEdit()">新增产品</el-button>
        <el-select v-model="query.status" placeholder="上/下架" style="width: 120px" @change="fetchData">
          <el-option label="全部" :value="undefined" />
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </div>

      <el-table :data="list" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="title" label="标题" min-width="200"/>
        <el-table-column prop="price" label="价格" width="120"/>
        <el-table-column prop="stock" label="库存" width="100"/>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="editVisible" :title="editing?.id ? '编辑产品' : '新增产品'" width="780px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="标题" prop="title" :rules="[{required:true,message:'请输入标题',trigger:'blur'}]">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" filterable style="width: 260px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price" :rules="[{required:true,message:'请输入价格',trigger:'blur'}]">
          <el-input v-model.number="form.price" type="number" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input v-model.number="form.originalPrice" type="number" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input v-model.number="form.stock" type="number" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="cover-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            accept="image/*"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="产品图片">
          <el-upload
            list-type="picture-card"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #file="{ file }">
              <img class="el-upload-list__item-thumbnail" :src="file.url || file.response?.data" alt="" />
            </template>
          </el-upload>
          <div class="tip">最多上传9张，单张不超过2MB</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="推荐">
          <el-switch v-model="form.recommend" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4"/>
        </el-form-item>
        <el-form-item label="所在区域">
          <el-input v-model="form.region" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="经纬度">
          <div class="coord-row">
            <el-input v-model.number="form.latitude" type="number" placeholder="纬度" />
            <el-input v-model.number="form.longitude" type="number" placeholder="经度" />
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="tagsInput" placeholder="以逗号分隔，如：亲子,海景,早餐" />
        </el-form-item>
        <el-form-item label="产品特色">
          <el-input v-model="form.features" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="预订须知">
          <el-input v-model="form.notice" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listMyProducts, createMyProduct, updateMyProduct, deleteMyProduct } from '@/api/merchant'
import { getCategoryList } from '@/api/productCategory'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useMerchantStore } from '@/stores/merchant'

const router = useRouter()
const merchantStore = useMerchantStore()

const query = reactive({ current: 1, size: 10, status: undefined })
const list = ref([])
const total = ref(0)
const categories = ref([])

const fetchData = async () => {
  const res = await listMyProducts(query)
  list.value = res.data?.records || []
  total.value = Number(res.data?.total || 0)
}

onMounted(async () => {
  fetchData()
  // 加载分类
  const cRes = await getCategoryList({ parentId: 0 })
  categories.value = cRes.data || []
})

const editVisible = ref(false)
const formRef = ref()
const editing = ref(null)
const saving = ref(false)
const form = reactive({ title: '', price: 0, stock: 0, status: 0, description: '', coverImage: '', images: [], categoryId: undefined, originalPrice: undefined, region: '', address: '', latitude: undefined, longitude: undefined, recommend: 0, features: '', notice: '', tags: [] })

const tagsInput = ref('')

const uploadAction = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/file/upload' || 'http://localhost:8080/api/file/upload'
})
const uploadHeaders = computed(() => ({ 'Authorization': 'Bearer ' + merchantStore.merchantToken }))

const openEdit = (row) => {
  editing.value = row || null
  Object.assign(form, { title: '', price: 0, stock: 0, status: 0, description: '', coverImage: '', images: [], categoryId: undefined, originalPrice: undefined, region: '', address: '', latitude: undefined, longitude: undefined, recommend: 0, features: '', notice: '', tags: [] })
  if (row) {
    Object.assign(form, row)
    if (typeof form.images === 'string') {
      try { form.images = JSON.parse(form.images || '[]') } catch { form.images = [] }
    }
    // tags 可能是字符串(JSON)或逗号分隔
    if (typeof form.tags === 'string') {
      try {
        const parsed = JSON.parse(form.tags)
        form.tags = Array.isArray(parsed) ? parsed : String(form.tags).split(',').map(s=>s.trim()).filter(Boolean)
      } catch {
        form.tags = String(form.tags).split(',').map(s=>s.trim()).filter(Boolean)
      }
    }
  }
  // 同步 tags 输入框
  tagsInput.value = (form.tags || []).join(',')
  editVisible.value = true
}

const handleCoverSuccess = (res) => {
  if (res.code === 200) form.coverImage = res.data
}
const handleImageSuccess = (res, file, fileList) => {
  if (res.code === 200) {
    form.images = (fileList || []).map(f => f.url || f.response?.data).filter(Boolean)
  }
}
const handleImageRemove = (file, fileList) => {
  form.images = (fileList || []).map(f => f.url || f.response?.data).filter(Boolean)
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      // 同步 tagsInput -> form.tags
      form.tags = tagsInput.value.split(',').map(s=>s.trim()).filter(Boolean)
      const payload = { ...form, images: JSON.stringify(form.images || []), tags: JSON.stringify(form.tags || []) }
      if (editing.value?.id) {
        await updateMyProduct(editing.value.id, payload)
      } else {
        await createMyProduct(payload)
      }
      ElMessage.success('保存成功')
      editVisible.value = false
      fetchData()
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该产品吗？', '提示', { type: 'warning' }).then(async ()=>{
    await deleteMyProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}
</script>

<style scoped>
.product-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 12px;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

/* ===== 封面上传区域，限制尺寸，避免超出弹窗 ===== */
.cover-uploader {
  display: inline-block;
}
.cover-uploader :deep(.el-upload) {
  width: 360px;
  height: 220px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.cover-image {
  width: 360px;
  height: 220px;
  object-fit: cover;
  display: block;
}
.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

/* 弹窗体高度限制，内容过多时滚动 */
:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow: auto;
}
.coord-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
</style>

