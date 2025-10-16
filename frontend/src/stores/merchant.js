import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { merchantLogin, getMerchantInfo } from '@/api/merchant'

export const useMerchantStore = defineStore('merchant', () => {
  // 状态
  const merchantToken = ref(localStorage.getItem('merchantToken') || '')
  const merchantInfo = ref(JSON.parse(localStorage.getItem('merchantInfo') || '{}'))

  // 计算属性
  const isLoggedIn = computed(() => !!merchantToken.value && !!merchantInfo.value.id)

  // 审核状态文本
  const auditStatusText = computed(() => {
    if (!merchantInfo.value.auditStatus) return '未知'
    const statusMap = {
      0: '待审核',
      1: '审核通过',
      2: '审核拒绝'
    }
    return statusMap[merchantInfo.value.auditStatus] || '未知'
  })

  // 商户登录
  const login = async (loginForm) => {
    const res = await merchantLogin(loginForm)
    if (res.data) {
      merchantToken.value = res.data.token
      merchantInfo.value = res.data.merchantInfo
      
      // 保存到localStorage
      localStorage.setItem('merchantToken', res.data.token)
      localStorage.setItem('merchantInfo', JSON.stringify(res.data.merchantInfo))
      
      return res.data
    }
    throw new Error('登录失败')
  }

  // 获取商户信息
  const fetchMerchantInfo = async () => {
    try {
      const res = await getMerchantInfo()
      if (res.data) {
        merchantInfo.value = res.data
        localStorage.setItem('merchantInfo', JSON.stringify(res.data))
      }
    } catch (error) {
      console.error('获取商户信息失败：', error)
    }
  }

  // 更新商户信息（本地）
  const updateMerchantInfo = (newInfo) => {
    merchantInfo.value = { ...merchantInfo.value, ...newInfo }
    localStorage.setItem('merchantInfo', JSON.stringify(merchantInfo.value))
  }

  // 商户登出
  const logout = () => {
    merchantToken.value = ''
    merchantInfo.value = {}
    localStorage.removeItem('merchantToken')
    localStorage.removeItem('merchantInfo')
  }

  return {
    merchantToken,
    merchantInfo,
    isLoggedIn,
    auditStatusText,
    login,
    fetchMerchantInfo,
    updateMerchantInfo,
    logout
  }
})

