<template>
  <div class="register-container">
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

    <!-- 居中的注册表单 -->
    <div class="register-box">
      <!-- 品牌Logo -->
      <div class="brand-logo">
        <div class="logo-icon">
          <el-icon :size="64"><ShoppingBag /></el-icon>
        </div>
        <h1 class="brand-name">景德镇文旅服务平台</h1>
        <p class="brand-tagline">注册账号，开启探索之旅</p>
      </div>

      <div class="register-card">
        <div class="register-header">
          <h2>创建新账号</h2>
          <p>填写信息完成注册</p>
        </div>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          size="large"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名（4-16位字母、数字或下划线）"
              class="form-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（6-20位）"
              class="form-input"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              class="form-input"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称（可选）"
              class="form-input"
            >
              <template #prefix>
                <el-icon><Avatar /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="手机号"
              class="form-input"
              maxlength="11"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱（可选）"
              class="form-input"
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="agreement-row">
            <el-checkbox v-model="agreeTerms">
              我已阅读并同意
              <el-button type="primary" link>《用户协议》</el-button>
              和
              <el-button type="primary" link>《隐私政策》</el-button>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <el-button type="primary" link @click="router.push('/login')">
            立即登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()

const registerFormRef = ref(null)
const loading = ref(false)
const agreeTerms = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须在6-20位之间'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 16, message: '用户名长度必须在4-16位之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  if (!agreeTerms.value) {
    ElMessage.warning('请先阅读并同意用户协议和隐私政策')
    return
  }
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname,
          phone: registerForm.phone,
          email: registerForm.email
        })
        ElMessage.success('注册成功！请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败：', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
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

.shape-1 {
  width: 80px;
  height: 80px;
  left: 10%;
  bottom: -80px;
  animation-delay: 0s;
  animation-duration: 18s;
}

.shape-2 {
  width: 60px;
  height: 60px;
  left: 20%;
  bottom: -60px;
  animation-delay: 2s;
  animation-duration: 22s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  left: 35%;
  bottom: -100px;
  animation-delay: 4s;
  animation-duration: 20s;
}

.shape-4 {
  width: 70px;
  height: 70px;
  left: 50%;
  bottom: -70px;
  animation-delay: 6s;
  animation-duration: 19s;
}

.shape-5 {
  width: 90px;
  height: 90px;
  left: 65%;
  bottom: -90px;
  animation-delay: 8s;
  animation-duration: 21s;
}

.shape-6 {
  width: 110px;
  height: 110px;
  left: 75%;
  bottom: -110px;
  animation-delay: 10s;
  animation-duration: 23s;
}

.shape-7 {
  width: 85px;
  height: 85px;
  left: 85%;
  bottom: -85px;
  animation-delay: 12s;
  animation-duration: 17s;
}

.shape-8 {
  width: 75px;
  height: 75px;
  left: 95%;
  bottom: -75px;
  animation-delay: 14s;
  animation-duration: 24s;
}

@keyframes floatUp {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-120vh) rotate(360deg);
    opacity: 0;
  }
}

/* 光晕效果 */
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
  animation: glowPulse 8s ease-in-out infinite;
}

.glow-1 {
  width: 400px;
  height: 400px;
  background: rgba(74, 144, 226, 0.25);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.glow-2 {
  width: 500px;
  height: 500px;
  background: rgba(138, 184, 214, 0.3);
  bottom: -150px;
  right: -150px;
  animation-delay: 3s;
}

.glow-3 {
  width: 450px;
  height: 450px;
  background: rgba(184, 216, 235, 0.35);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 6s;
}

@keyframes glowPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.4;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.6;
  }
}

/* 波浪效果 */
.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px;
  background: rgba(255, 255, 255, 0.05);
  animation: waveMove 10s linear infinite;
}

.wave-1 {
  animation-duration: 8s;
  opacity: 0.3;
}

.wave-2 {
  animation-duration: 12s;
  opacity: 0.2;
  animation-delay: -5s;
}

.wave-3 {
  animation-duration: 15s;
  opacity: 0.1;
  animation-delay: -2s;
}

@keyframes waveMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

/* ========== 注册卡片 ========== */

.register-box {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 520px;
  animation: scaleIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 品牌Logo */
.brand-logo {
  text-align: center;
  margin-bottom: 32px;
  animation: fadeInDown 0.6s ease-out;
}

.logo-icon {
  display: inline-block;
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.logo-icon .el-icon {
  color: #4a90e2;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.1));
}

.brand-name {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
}

.brand-tagline {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.2);
}

/* 注册卡片 */
.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.register-header p {
  font-size: 14px;
  color: #999;
}

.register-form {
  margin-top: 20px;
}

.form-input {
  height: 48px;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.form-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.3);
}

.agreement-row {
  margin: 8px 0;
}

.agreement-row :deep(.el-form-item__content) {
  line-height: 1.6;
}

.agreement-row :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #666;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #4a90e2 0%, #5fa8d3 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.3);
}

.register-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(74, 144, 226, 0.5);
}

.register-button:active {
  transform: translateY(-1px);
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

/* ========== 动画 ========== */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 响应式设计 ========== */
@media (max-width: 576px) {
  .register-card {
    padding: 32px 24px;
  }
  
  .brand-name {
    font-size: 24px;
  }
  
  .brand-tagline {
    font-size: 14px;
  }
  
  .register-header h2 {
    font-size: 24px;
  }
  
  .shape {
    display: none;
  }
}
</style>
