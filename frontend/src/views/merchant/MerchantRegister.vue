<template>
  <div class="merchant-register-page">
    <!-- 动态背景 -->
    <div class="animated-background">
      <!-- 渐变背景 -->
      <div class="gradient-bg"></div>
      
      <!-- 浮动的陶瓷碎片 -->
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
        <div class="shape shape-6"></div>
        <div class="shape shape-7"></div>
        <div class="shape shape-8"></div>
      </div>
      
      <!-- 光晕效果 -->
      <div class="glow glow-1"></div>
      <div class="glow glow-2"></div>
      <div class="glow glow-3"></div>
      
      <!-- 波浪效果 -->
      <div class="wave wave-1"></div>
      <div class="wave wave-2"></div>
      <div class="wave wave-3"></div>
    </div>

    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h1>商户注册</h1>
          <p>加入景德镇文旅服务平台</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          label-width="100px"
        >
          <el-form-item label="商户账号" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入商户账号"
              maxlength="50"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6-20位）"
              show-password
              maxlength="20"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              maxlength="20"
            />
          </el-form-item>

          <el-divider content-position="left">店铺信息</el-divider>

          <el-form-item label="店铺名称" prop="shopName">
            <el-input
              v-model="registerForm.shopName"
              placeholder="请输入店铺名称"
              maxlength="100"
            />
          </el-form-item>

          <el-form-item label="店铺介绍" prop="shopIntroduction">
            <el-input
              v-model="registerForm.shopIntroduction"
              type="textarea"
              :rows="3"
              placeholder="请简要介绍您的店铺"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-divider content-position="left">联系信息</el-divider>

          <el-form-item label="联系人" prop="contactPerson">
            <el-input
              v-model="registerForm.contactPerson"
              placeholder="请输入联系人姓名"
              maxlength="50"
            />
          </el-form-item>

          <el-form-item label="联系电话" prop="contactPhone">
            <el-input
              v-model="registerForm.contactPhone"
              placeholder="请输入联系电话"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input
              v-model="registerForm.contactEmail"
              placeholder="请输入联系邮箱（可选）"
            />
          </el-form-item>

          <el-form-item label="店铺地址" prop="address">
            <el-input
              v-model="registerForm.address"
              placeholder="请输入店铺地址（可选）"
              maxlength="255"
            />
          </el-form-item>

          <el-divider content-position="left">资质信息</el-divider>

          <el-form-item label="营业执照">
            <el-upload
              class="upload-demo"
              :action="uploadAction"
              :headers="uploadHeaders"
              :on-success="handleBusinessLicenseSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :limit="1"
              list-type="picture-card"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  请上传营业执照图片，支持jpg/png，不超过2MB
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="身份证正面">
            <el-upload
              class="upload-demo"
              :action="uploadAction"
              :headers="uploadHeaders"
              :on-success="handleIdCardFrontSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :limit="1"
              list-type="picture-card"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  请上传身份证正面照片
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="身份证反面">
            <el-upload
              class="upload-demo"
              :action="uploadAction"
              :headers="uploadHeaders"
              :on-success="handleIdCardBackSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :limit="1"
              list-type="picture-card"
              accept="image/*"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  请上传身份证反面照片
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-alert
            title="审核说明"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          >
            提交注册后，我们的工作人员将在1-3个工作日内审核您的资质信息，请耐心等待。
          </el-alert>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              class="register-button"
            >
              提交注册
            </el-button>
            <el-button @click="goToLogin">
              返回登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { merchantRegister } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

const registerFormRef = ref()
const loading = ref(false)

// 文件上传配置
const uploadAction = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/file/upload' || 'http://localhost:8080/api/file/upload'
})

const uploadHeaders = computed(() => {
  return {
    // 如果需要token，可以在这里添加
  }
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  shopName: '',
  shopIntroduction: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  businessLicense: '',
  idCardFront: '',
  idCardBack: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入商户账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const { confirmPassword, ...registerData } = registerForm
      await merchantRegister(registerData)
      
      ElMessage.success('注册成功！请等待管理员审核')
      
      // 3秒后跳转到登录页
      setTimeout(() => {
        router.push('/merchant/login')
      }, 3000)
    } catch (error) {
      console.error('注册失败：', error)
      ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/merchant/login')
}

// 文件上传处理
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

const handleBusinessLicenseSuccess = (response) => {
  if (response.code === 200) {
    registerForm.businessLicense = response.data
    ElMessage.success('营业执照上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleIdCardFrontSuccess = (response) => {
  if (response.code === 200) {
    registerForm.idCardFront = response.data
    ElMessage.success('身份证正面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleIdCardBackSuccess = (response) => {
  if (response.code === 200) {
    registerForm.idCardBack = response.data
    ElMessage.success('身份证反面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = (error) => {
  console.error('上传失败：', error)
  ElMessage.error('文件上传失败，请重试')
}
</script>

<style scoped>
.merchant-register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

/* ========== 动态背景 ========== */
.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 渐变背景 - 青花瓷主题色 */
.gradient-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(-45deg, #4a90e2, #5fa8d3, #8ab8d6, #b8d8eb);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 浮动形状 - 代表陶瓷碎片 */
.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  border-radius: 50%;
  animation: floatUp 20s infinite ease-in-out;
}

.shape-1 { width: 80px; height: 80px; left: 10%; bottom: -80px; animation-delay: 0s; animation-duration: 18s; }
.shape-2 { width: 60px; height: 60px; left: 20%; bottom: -60px; animation-delay: 2s; animation-duration: 22s; }
.shape-3 { width: 100px; height: 100px; left: 35%; bottom: -100px; animation-delay: 4s; animation-duration: 20s; }
.shape-4 { width: 70px; height: 70px; left: 50%; bottom: -70px; animation-delay: 6s; animation-duration: 19s; }
.shape-5 { width: 90px; height: 90px; left: 65%; bottom: -90px; animation-delay: 8s; animation-duration: 21s; }
.shape-6 { width: 110px; height: 110px; left: 75%; bottom: -110px; animation-delay: 10s; animation-duration: 23s; }
.shape-7 { width: 85px; height: 85px; left: 85%; bottom: -85px; animation-delay: 12s; animation-duration: 17s; }
.shape-8 { width: 75px; height: 75px; left: 95%; bottom: -75px; animation-delay: 14s; animation-duration: 24s; }

@keyframes floatUp {
  0% { transform: translateY(0) rotate(0deg); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-120vh) rotate(360deg); opacity: 0; }
}

/* 光晕效果 */
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
  animation: glowPulse 8s ease-in-out infinite;
}

.glow-1 { width: 400px; height: 400px; background: rgba(74, 144, 226, 0.25); top: -100px; left: -100px; animation-delay: 0s; }
.glow-2 { width: 350px; height: 350px; background: rgba(95, 168, 211, 0.25); bottom: -100px; right: -100px; animation-delay: 3s; }
.glow-3 { width: 450px; height: 450px; background: rgba(138, 184, 214, 0.25); top: 50%; left: 50%; transform: translate(-50%, -50%); animation-delay: 6s; }

@keyframes glowPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.2); opacity: 0.6; }
}

/* 波浪效果 */
.wave {
  position: absolute;
  bottom: 0;
  width: 200%;
  height: 150px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  animation: waveMove 10s infinite linear;
}

.wave-1 { left: -50%; animation-delay: 0s; animation-duration: 15s; }
.wave-2 { left: -50%; animation-delay: 3s; animation-duration: 18s; }
.wave-3 { left: -50%; animation-delay: 6s; animation-duration: 12s; }

@keyframes waveMove {
  0% { transform: translateX(0) translateY(0); }
  100% { transform: translateX(50%) translateY(-20px); }
}

.register-container {
  width: 100%;
  max-width: 700px;
  position: relative;
  z-index: 1;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.register-header p {
  font-size: 14px;
  color: #666;
}

.register-form {
  margin-top: 32px;
}

.register-button {
  width: 200px;
  margin-right: 16px;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #409EFF;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
}

:deep(.el-textarea__inner) {
  padding: 12px 16px;
}

.logo-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.logo-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

:deep(.el-upload__tip) {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

:deep(.el-upload-list__item) {
  transition: all 0.3s;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

/* 返回登录按钮样式 */
.register-form :deep(.el-button--default) {
  color: #666;
}

.register-form :deep(.el-button--default:hover) {
  color: #409EFF;
  border-color: #409EFF;
}
</style>

