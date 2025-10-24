import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 60000  // 增加到60秒，因为AI API调用需要更长时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 根据请求路径为不同端附带对应的 Token
    const url = typeof config.url === 'string' ? config.url : ''
    const isMerchantApi = url.startsWith('/merchant') || url.includes('/merchant/') || url.includes('/api/merchant/')
    if (isMerchantApi) {
      const merchantToken = localStorage.getItem('merchantToken')
      if (merchantToken) {
        config.headers['Authorization'] = `Bearer ${merchantToken}`
      }
    } else {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的code不是200，则认为是错误
    if (res.code !== 200) {
      // 检查是否是登录相关错误
      const loginErrorMessages = ['未登录', '登录已过期', 'Token解析失败', '请先登录', '请重新登录']
      const needLogin = res.code === 401 || loginErrorMessages.some(msg => res.message?.includes(msg))
      
      if (needLogin) {
        ElMessage.error(res.message || '请重新登录')
        const isMerchantApi = response.config?.url?.startsWith('/merchant')
        if (isMerchantApi) {
          localStorage.removeItem('merchantToken')
          localStorage.removeItem('merchantInfo')
          router.push('/merchant/login')
        } else {
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
        }
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误：', error)
    
    let message = '网络错误，请稍后重试'
    let needLogin = false
    
    if (error.response) {
      const errorMessage = error.response.data?.message || ''
      
      // 检查是否是登录相关错误
      const loginErrorMessages = ['未登录', '登录已过期', 'Token解析失败', '请先登录', '请重新登录']
      needLogin = error.response.status === 401 || 
                  (error.response.status === 400 && loginErrorMessages.some(msg => errorMessage.includes(msg)))
      
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '没有访问权限'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = errorMessage || message
      }
    }
    
    if (needLogin) {
      const isMerchantApi = error.config?.url?.startsWith('/merchant')
      if (isMerchantApi) {
        localStorage.removeItem('merchantToken')
        localStorage.removeItem('merchantInfo')
        router.push('/merchant/login')
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request

