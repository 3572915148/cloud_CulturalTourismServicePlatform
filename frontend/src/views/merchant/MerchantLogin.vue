<template>
  <div class="merchant-login-page">
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

    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h1>商户登录</h1>
          <p>景德镇文旅服务平台商户管理系统</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入商户账号"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <span>还没有商户账号？</span>
            <el-button type="primary" link @click="goToRegister">
              立即注册
            </el-button>
          </div>

          <div class="back-to-home">
            <el-button link @click="router.push('/home')">
              <el-icon><ArrowLeft /></el-icon>
              返回用户端
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/stores/merchant'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const merchantStore = useMerchantStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入商户账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await merchantStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/merchant/dashboard')
    } catch (error) {
      console.error('登录失败：', error)
      ElMessage.error(error.response?.data?.message || '登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => {
  router.push('/merchant/register')
}
</script>

<style scoped>
.merchant-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 20px;
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

.login-container {
  width: 100%;
  max-width: 450px;
  position: relative;
  z-index: 1;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #666;
}

.login-form {
  margin-top: 32px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.back-to-home {
  text-align: center;
  margin-top: 16px;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
}
</style>

