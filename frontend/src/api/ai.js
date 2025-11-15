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

// 获取AI推荐（流式SSE）- 使用Agent接口
export function getAiRecommendationStream(data, callbacks) {
  const token = localStorage.getItem('token')
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  
  // 生成或获取sessionId
  let sessionId = localStorage.getItem('agent_session_id')
  if (!sessionId) {
    sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem('agent_session_id', sessionId)
  }
  
  const requestData = {
    sessionId: sessionId,
    message: data.query || data.message || ''
  }
  
  console.log('发起Agent流式请求:', `${baseURL}/agent/chat/stream`, requestData)
  
  let hasCompleted = false  // 标记是否已完成
  let isUserStopped = false  // 标记是否是用户主动停止
  let hasReceivedContent = false  // 标记是否收到过内容
  let currentEvent = null  // 当前事件类型
  let eventData = ''  // 当前事件的数据缓冲区
  let reader = null  // 保存reader引用，以便取消
  let timeoutId = null  // 保存超时ID
  
  // 创建 AbortController 用于取消请求
  const abortController = new AbortController()
  
  // 停止函数：取消请求
  const stop = () => {
    if (hasCompleted) {
      console.log('已经停止，无需重复停止')
      return
    }
    console.log('用户主动停止AI生成')
    hasCompleted = true
    isUserStopped = true  // 标记为用户主动停止
    clearTimeout(timeoutId)
    
    // 先取消reader，阻止继续读取（静默处理错误）
    if (reader) {
      reader.cancel().catch(() => {
        // 静默处理，这是正常的取消操作
      })
    }
    
    // 然后取消fetch请求
    abortController.abort()
    
    // 不调用onComplete，因为这是用户主动停止，不是正常完成
    console.log('已停止AI生成，不再接收新内容')
  }
  
  // 设置超时（30秒）
  timeoutId = setTimeout(() => {
    if (!hasCompleted && !isUserStopped) {
      console.warn('流式请求超时，取消请求')
      hasCompleted = true
      abortController.abort()
      if (reader) {
        reader.cancel().catch(() => {
          // 静默处理取消错误
        })
      }
      callbacks.onError && callbacks.onError('请求超时，请检查网络连接或稍后重试')
    }
  }, 30000) // 30秒超时
  
  // 立即返回停止函数，不等待Promise完成
  const result = {
    stop: stop
  }
  
  // 异步处理fetch，但不阻塞返回
  fetch(`${baseURL}/agent/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(requestData),
    signal: abortController.signal  // 添加取消信号
  }).then(response => {
    console.log('收到响应:', response.status, response.statusText)
    
    if (!response.ok) {
      clearTimeout(timeoutId)
      throw new Error(`网络请求失败: ${response.status}`)
    }
    
    reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''  // 缓冲区，处理跨块的数据
    
    // 读取流式数据
    function readStream() {
      if (hasCompleted) {
        // 如果已经完成，停止读取
        return Promise.resolve()
      }
      
      return reader.read().then(({ done, value }) => {
        if (hasCompleted) {
          // 如果已经完成，停止读取
          return
        }
        
        if (done) {
          console.log('流式数据读取完成，已接收内容:', hasReceivedContent)
          clearTimeout(timeoutId)
          
          // 处理最后可能剩余的不完整数据
          if (buffer.trim()) {
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
        
        // 按行分割处理（SSE格式是每行一个事件）
        const lines = buffer.split('\n')
        
        // 保留最后一个可能不完整的行
        buffer = lines.pop() || ''
        
        // 处理每一行
        for (const line of lines) {
          if (hasCompleted) {
            console.log('已停止，停止处理剩余行')
            break
          }
          processLine(line)
        }
        
        // 继续读取（如果未完成且未停止）
        if (!hasCompleted) {
          return readStream()
        } else {
          console.log('已停止，不再继续读取')
          return Promise.resolve()
        }
      }).catch(error => {
        clearTimeout(timeoutId)
        
        // 如果是取消错误且是用户主动停止，静默处理
        if (error.name === 'AbortError') {
          if (isUserStopped) {
            console.log('用户已停止，请求已取消')
          } else {
            console.log('请求已取消')
          }
          return
        }
        
        // 只有在不是用户主动停止时才显示错误
        if (!hasCompleted && !isUserStopped) {
          console.error('读取流失败:', error)
          hasCompleted = true
          callbacks.onError && callbacks.onError(error.message || '网络连接失败，请检查后端服务是否运行')
        }
      })
    }
    
    // 处理单行数据（标准SSE格式）
    function processLine(line) {
      // 如果已经停止，不再处理任何数据
      if (hasCompleted) {
        return
      }
      
      const trimmedLine = line.trim()
      if (!trimmedLine) {
        // 空行表示一个事件结束，处理累积的数据
        if (currentEvent && eventData && !hasCompleted) {
          handleEvent(currentEvent, eventData)
          currentEvent = null
          eventData = ''
        }
        return
      }
      
      // SSE格式: "event: content" 或 "data: ..."
      if (trimmedLine.startsWith('event:')) {
        currentEvent = trimmedLine.substring(6).trim()
      } else if (trimmedLine.startsWith('data:')) {
        const data = trimmedLine.substring(5).trim()
        if (data && !hasCompleted) {
          eventData += data
        }
      } else if (trimmedLine.startsWith('id:')) {
        // 忽略id字段
      } else if (trimmedLine.startsWith('retry:')) {
        // 忽略retry字段
      } else {
        // 可能是直接的数据行（非标准格式）
        if (!hasCompleted) {
          eventData += trimmedLine
        }
      }
    }
    
    // 处理完整的事件
    function handleEvent(eventType, data) {
      // 如果已经停止，不再处理任何事件（静默忽略，不打印日志）
      if (hasCompleted) {
        return
      }
      
      // 只在非content事件或重要事件时打印日志，减少日志噪音
      if (eventType !== 'content') {
        console.log('收到SSE事件:', eventType, '数据长度:', data?.length || 0)
      }
      
      try {
        if (eventType === 'content') {
          // content事件直接是文本内容
          if (data && data.length > 0 && !hasCompleted) {
            hasReceivedContent = true
            // 不打印content事件的日志，减少控制台噪音
            if (callbacks.onContent && !hasCompleted) {
              callbacks.onContent(data)
            }
          }
        } else if (eventType === 'tool_call') {
          // 工具调用事件
          try {
            const toolInfo = JSON.parse(data)
            console.log('处理tool_call事件，工具:', toolInfo.tool)
            // 可以在这里显示工具调用提示
          } catch (e) {
            console.warn('解析tool_call数据失败:', e)
          }
        } else if (eventType === 'tool_result') {
          // 工具执行结果
          try {
            const result = JSON.parse(data)
            console.log('处理tool_result事件，成功:', result.success, '数据:', result)
            // 如果工具返回了产品数据，提取出来
            if (result.success && result.data) {
              // 检查是否是产品列表
              if (Array.isArray(result.data) && result.data.length > 0) {
                // 更严格地判断是否是产品：必须有 price 或 coverImage 字段（产品特有）
                // 分类数据只有 id、name、icon 等，没有 price 和 coverImage
                const firstItem = result.data[0]
                const isProduct = firstItem.id && 
                                 (firstItem.title || firstItem.name) && 
                                 (firstItem.price !== undefined || firstItem.coverImage !== undefined || firstItem.price !== null || firstItem.coverImage !== null)
                
                if (isProduct) {
                  console.log('工具返回产品，数量:', result.data.length, '第一个产品:', firstItem)
                  // 过滤掉非产品数据（确保都是真正的产品）
                  const products = result.data.filter(item => 
                    item.id && 
                    (item.title || item.name) && 
                    (item.price !== undefined || item.coverImage !== undefined || item.price !== null || item.coverImage !== null)
                  )
                  
                  if (products.length > 0 && callbacks.onProducts) {
                    callbacks.onProducts(products)
                  } else {
                    console.warn('过滤后没有有效产品数据')
                  }
                } else {
                  console.log('数据不是产品格式（可能是分类或其他数据），第一个元素:', firstItem)
                }
              } else {
                console.log('工具返回的数据不是数组或为空')
              }
            } else {
              console.log('工具返回失败或没有数据:', result)
            }
          } catch (e) {
            console.error('解析tool_result数据失败:', e, '原始数据:', data?.substring(0, 200))
          }
        } else if (eventType === 'complete') {
          console.log('处理complete事件')
          clearTimeout(timeoutId)
          if (!hasCompleted) {
            hasCompleted = true
            if (callbacks.onComplete) {
              callbacks.onComplete()
            }
          }
        } else if (eventType === 'error') {
          console.error('处理error事件:', data)
          clearTimeout(timeoutId)
          if (!hasCompleted) {
            hasCompleted = true
            if (callbacks.onError) {
              callbacks.onError(data || '未知错误')
            }
          }
        } else {
          console.warn('未知的SSE事件类型:', eventType)
        }
      } catch (e) {
        console.error('处理事件失败:', e, '事件类型:', eventType, '数据:', data?.substring(0, 100))
      }
    }
    
    return readStream()
  }).catch(error => {
    clearTimeout(timeoutId)
    
    // 如果是取消错误且是用户主动停止，静默处理
    if (error.name === 'AbortError') {
      if (isUserStopped) {
        console.log('用户已停止，请求已取消')
      } else {
        console.log('请求已取消')
      }
      hasCompleted = true
      return
    }
    
    // 只有在不是用户主动停止时才显示错误
    if (!hasCompleted && !isUserStopped) {
      console.error('流式请求失败:', error)
      hasCompleted = true
      const errorMessage = error.message || '网络连接失败，请检查后端服务是否运行'
      callbacks.onError && callbacks.onError(errorMessage)
    }
  })
  
  // 返回包含stop方法的对象，允许外部取消请求
  return result
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
