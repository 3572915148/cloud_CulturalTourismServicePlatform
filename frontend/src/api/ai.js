import request from '@/utils/request'

/**
 * AI推荐相关API
 */

// 获取AI推荐（非流式）
export function getAiRecommendation(data) {
  return request({
    url: '/ai/recommend',
    method: 'post',
    data
  })
}

// 获取AI推荐（流式SSE）
export function getAiRecommendationStream(data, callbacks) {
  const token = localStorage.getItem('token')
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  
  console.log('发起流式请求:', `${baseURL}/ai/recommend/stream`, data)
  
  let hasCompleted = false  // 标记是否已完成
  let hasReceivedContent = false  // 标记是否收到过内容
  
  return fetch(`${baseURL}/ai/recommend/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data)
  }).then(response => {
    console.log('收到响应:', response.status, response.statusText)
    
    if (!response.ok) {
      throw new Error(`网络请求失败: ${response.status}`)
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''  // 缓冲区，处理跨块的数据
    
    // 读取流式数据
    function readStream() {
      return reader.read().then(({ done, value }) => {
        if (done) {
          console.log('流式数据读取完成，已接收内容:', hasReceivedContent)
          
          // 处理最后可能剩余的不完整数据
          if (buffer.trim()) {
            console.log('处理剩余缓冲数据:', buffer.substring(0, 100))
            processLine(buffer)
          }
          
          // 确保onComplete只被调用一次
          if (!hasCompleted) {
            hasCompleted = true
            callbacks.onComplete && callbacks.onComplete()
          }
          return
        }
        
        // 解码数据并添加到缓冲区
        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk
        
        // 按行分割处理
        const lines = buffer.split('\n')
        
        // 保留最后一个可能不完整的行
        buffer = lines.pop() || ''
        
        // 处理每一行
        for (const line of lines) {
          processLine(line)
        }
        
        // 继续读取
        return readStream()
      }).catch(error => {
        console.error('读取流失败:', error)
        if (!hasCompleted) {
          hasCompleted = true
          callbacks.onError && callbacks.onError(error.message)
        }
      })
    }
    
    // 处理单行数据
    function processLine(line) {
      const trimmedLine = line.trim()
      if (!trimmedLine) return
      
      // SSE格式可能是 "data: {...}"、"data:{...}" 或直接是 "{...}"
      let dataStr = trimmedLine
      if (trimmedLine.startsWith('data:')) {
        // 处理 "data: " 和 "data:" 两种格式
        dataStr = trimmedLine.substring(5).trim()
      }
      
      if (!dataStr) return
      
      try {
        const data = JSON.parse(dataStr)
        console.log('收到SSE事件:', data.type, '内容长度:', data.content?.length || 0)
        
        // 根据类型调用不同的回调
        if (data.type === 'content') {
          if (data.content && data.content.length > 0) {
            hasReceivedContent = true
            console.log('处理content事件，内容:', data.content.substring(0, 50))
            if (callbacks.onContent) {
              callbacks.onContent(data.content)
            } else {
              console.warn('onContent回调未定义')
            }
          }
        } else if (data.type === 'products') {
          console.log('处理products事件，产品数量:', data.products?.length)
          if (callbacks.onProducts) {
            callbacks.onProducts(data.products, data.productIds)
          } else {
            console.warn('onProducts回调未定义')
          }
        } else if (data.type === 'complete') {
          console.log('处理complete事件，ID:', data.recommendationId, 'apiSuccess:', data.apiSuccess)
          if (!hasCompleted) {
            hasCompleted = true
            if (callbacks.onComplete) {
              callbacks.onComplete(data.recommendationId)
            } else {
              console.warn('onComplete回调未定义')
            }
          } else {
            console.warn('complete事件重复调用，已忽略')
          }
        } else if (data.type === 'error') {
          console.error('处理error事件:', data.message)
          if (!hasCompleted) {
            hasCompleted = true
            if (callbacks.onError) {
              callbacks.onError(data.message)
            } else {
              console.warn('onError回调未定义')
            }
          }
        } else {
          console.warn('未知的SSE事件类型:', data.type)
        }
      } catch (e) {
        console.error('解析SSE数据失败:', e.message)
        console.error('原始数据:', dataStr.substring(0, 200))
      }
    }
    
    return readStream()
  }).catch(error => {
    console.error('流式请求失败:', error)
    if (!hasCompleted) {
      hasCompleted = true
      callbacks.onError && callbacks.onError(error.message)
    }
  })
}

// 获取推荐历史
export function getRecommendationHistory(params) {
  return request({
    url: '/ai/history',
    method: 'get',
    params
  })
}

// 提交反馈
export function submitFeedback(data) {
  return request({
    url: '/ai/feedback',
    method: 'post',
    data
  })
}

// 获取推荐统计
export function getRecommendationStats() {
  return request({
    url: '/ai/stats',
    method: 'get'
  })
}
