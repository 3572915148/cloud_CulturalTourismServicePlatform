<template>
  <div class="shop-info">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">店铺信息管理</span>
      </template>
    </el-page-header>

    <el-card class="info-card" style="margin-top: 24px">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" @click="editMode = !editMode">
            {{ editMode ? '取消编辑' : '编辑信息' }}
          </el-button>
        </div>
      </template>

      <el-form
        ref="shopFormRef"
        :model="shopForm"
        :rules="shopRules"
        label-width="120px"
        :disabled="!editMode"
      >
        <el-form-item label="商户账号">
          <el-input v-model="shopForm.username" disabled />
        </el-form-item>

        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="shopForm.shopName" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="店铺Logo">
          <el-upload
            v-if="editMode"
            class="logo-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="shopForm.shopLogo" :src="shopForm.shopLogo" class="logo-image" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <img v-else-if="shopForm.shopLogo" :src="shopForm.shopLogo" class="logo-image" />
          <span v-else>未上传</span>
        </el-form-item>

        <el-form-item label="店铺介绍" prop="shopIntroduction">
          <el-input
            v-model="shopForm.shopIntroduction"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="shopForm.contactPerson" maxlength="50" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="shopForm.contactPhone" maxlength="11" />
        </el-form-item>

        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="shopForm.contactEmail" />
        </el-form-item>

        <el-form-item label="店铺地址">
          <el-input v-model="shopForm.address" maxlength="255" />
        </el-form-item>

        <el-form-item label="审核状态">
          <el-tag :type="statusType" size="large">
            {{ auditStatusText }}
          </el-tag>
          <span v-if="shopForm.auditRemark" style="margin-left: 12px; color: #f56c6c">
            备注：{{ shopForm.auditRemark }}
          </span>
        </el-form-item>

        <el-form-item v-if="editMode">
          <el-button type="primary" :loading="loading" @click="handleSave">
            保存修改
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="info-card" style="margin-top: 24px" v-if="!editMode">
      <template #header>
        <span>资质信息</span>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="营业执照">
          <el-image
            v-if="shopForm.businessLicense"
            :src="shopForm.businessLicense"
            fit="contain"
            style="width: 200px; height: 150px"
            :preview-src-list="[shopForm.businessLicense]"
            preview-teleported
          />
          <span v-else>未上传</span>
        </el-descriptions-item>

        <el-descriptions-item label="身份证正面">
          <el-image
            v-if="shopForm.idCardFront"
            :src="shopForm.idCardFront"
            fit="contain"
            style="width: 200px; height: 150px"
            :preview-src-list="[shopForm.idCardFront]"
            preview-teleported
          />
          <span v-else>未上传</span>
        </el-descriptions-item>

        <el-descriptions-item label="身份证反面">
          <el-image
            v-if="shopForm.idCardBack"
            :src="shopForm.idCardBack"
            fit="contain"
            style="width: 200px; height: 150px"
            :preview-src-list="[shopForm.idCardBack]"
            preview-teleported
          />
          <span v-else>未上传</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-alert
        type="info"
        :closable="false"
        style="margin-top: 16px"
      >
        资质信息在注册时已提交，如需修改请联系管理员
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import { updateMerchantInfo } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const merchantStore = useMerchantStore()

const shopFormRef = ref()
const editMode = ref(false)
const loading = ref(false)

const shopForm = reactive({
  username: '',
  shopName: '',
  shopLogo: '',
  shopIntroduction: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  businessLicense: '',
  idCardFront: '',
  idCardBack: '',
  auditStatus: 0,
  auditRemark: ''
})

// 备份原始数据
let originalData = {}

const uploadAction = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/file/upload' || 'http://localhost:8080/api/file/upload'
})

const uploadHeaders = computed(() => {
  return {
    'Authorization': 'Bearer ' + merchantStore.merchantToken
  }
})

const statusType = computed(() => {
  if (shopForm.auditStatus === 0) return 'warning'
  if (shopForm.auditStatus === 1) return 'success'
  if (shopForm.auditStatus === 2) return 'danger'
  return 'info'
})

const auditStatusText = computed(() => {
  const statusMap = {
    0: '待审核',
    1: '审核通过',
    2: '审核拒绝'
  }
  return statusMap[shopForm.auditStatus] || '未知'
})

const shopRules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 加载商户信息
const loadMerchantInfo = () => {
  const info = merchantStore.merchantInfo
  Object.keys(shopForm).forEach(key => {
    if (info[key] !== undefined) {
      shopForm[key] = info[key]
    }
  })
  // 备份原始数据
  originalData = JSON.parse(JSON.stringify(shopForm))
}

// 文件上传
const beforeUpload = (file) => {
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

const handleLogoSuccess = (response) => {
  if (response.code === 200) {
    shopForm.shopLogo = response.data
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 保存修改
const handleSave = async () => {
  if (!shopFormRef.value) return

  await shopFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const updateData = {
        shopName: shopForm.shopName,
        shopLogo: shopForm.shopLogo,
        shopIntroduction: shopForm.shopIntroduction,
        contactPerson: shopForm.contactPerson,
        contactPhone: shopForm.contactPhone,
        contactEmail: shopForm.contactEmail,
        address: shopForm.address
      }

      const res = await updateMerchantInfo(updateData)
      if (res.data) {
        merchantStore.updateMerchantInfo(res.data)
        ElMessage.success('保存成功')
        editMode.value = false
        originalData = JSON.parse(JSON.stringify(shopForm))
      }
    } catch (error) {
      console.error('保存失败：', error)
      ElMessage.error(error.response?.data?.message || '保存失败')
    } finally {
      loading.value = false
    }
  })
}

// 取消编辑
const handleCancel = () => {
  Object.keys(originalData).forEach(key => {
    shopForm[key] = originalData[key]
  })
  editMode.value = false
}

onMounted(() => {
  loadMerchantInfo()
})
</script>

<style scoped>
.shop-info {
  max-width: 1000px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.logo-uploader {
  display: inline-block;
}

.logo-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 148px;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.logo-image {
  width: 148px;
  height: 148px;
  object-fit: cover;
  display: block;
}
</style>

