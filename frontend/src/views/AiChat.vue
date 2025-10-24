<template>
  <div class="ai-chat-container">
    <!-- 头部 -->
    <div class="chat-header">
      <div class="header-content">
        <div class="ai-avatar">
          <el-icon size="24"><ChatDotRound /></el-icon>
        </div>
        <div class="header-info">
          <h3>景德镇AI文旅助手</h3>
          <p>为您推荐最合适的景德镇文旅产品</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="startNewChat" type="success" plain>
          <el-icon><Plus /></el-icon>
          新建会话
        </el-button>
        <el-button @click="showHistory = !showHistory" type="primary" plain>
          {{ showHistory ? '隐藏历史' : '查看历史' }}
        </el-button>
      </div>
    </div>

    <div class="chat-content">
      <!-- 历史记录侧边栏 -->
      <div v-if="showHistory" class="history-sidebar">
        <div class="sidebar-header">
          <h4>推荐历史</h4>
          <el-button @click="loadHistory" size="small" :loading="historyLoading">
            刷新
          </el-button>
        </div>
        <div class="history-list">
          <div 
            v-for="item in historyList" 
            :key="item.id"
            class="history-item"
            @click="loadHistoryItem(item)"
          >
            <div class="history-query">{{ item.query }}</div>
            <div class="history-time">{{ formatTime(item.createTime) }}</div>
            <div v-if="item.feedback !== null" class="history-feedback">
              <el-icon v-if="item.feedback === 1" color="#67c23a">
                <Check />
              </el-icon>
              <el-icon v-else color="#f56c6c">
                <Close />
              </el-icon>
            </div>
          </div>
        </div>
        <div v-if="historyList.length === 0" class="empty-history">
          暂无推荐历史
        </div>
      </div>

      <!-- 对话区域 -->
      <div class="chat-main">
        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-content">
              <el-icon size="48" color="#409eff"><ChatDotRound /></el-icon>
              <h3>欢迎使用景德镇AI文旅助手</h3>
              <p>告诉我您的需求，我会为您推荐最合适的文旅产品</p>
              <div class="quick-questions">
                <el-tag 
                  v-for="question in quickQuestions" 
                  :key="question"
                  @click="sendQuickQuestion(question)"
                  class="quick-tag"
                  effect="plain"
                >
                  {{ question }}
                </el-tag>
              </div>
            </div>
          </div>

          <div v-for="message in messages" :key="message.id" class="message-item">
            <!-- 用户消息 -->
            <div v-if="message.type === 'user'" class="user-message">
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.time) }}</div>
              </div>
              <div class="user-avatar">
                <el-icon><User /></el-icon>
              </div>
            </div>

            <!-- AI消息 -->
            <div v-else class="ai-message">
              <div class="ai-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="message-content">
                <div class="message-text">
                  <span v-html="formatAiResponse(message.content)"></span>
                  <span v-if="message.isStreaming" class="typing-cursor">|</span>
                </div>
                
                <!-- 推荐产品 -->
                <div v-if="message.recommendedProducts && message.recommendedProducts.length > 0" 
                     class="recommended-products">
                  <h4>推荐产品：</h4>
                  <div class="product-grid">
                    <div 
                      v-for="product in message.recommendedProducts" 
                      :key="product.id"
                      class="product-card"
                      @click="viewProduct(product.id)"
                    >
                      <img :src="product.coverImage || '/placeholder.jpg'" :alt="product.title" />
                      <div class="product-info">
                        <h5>{{ product.title }}</h5>
                        <p class="product-price">¥{{ product.price }}</p>
                        <p class="product-region">{{ product.region }}</p>
                        <div v-if="product.reason" class="product-reason">
                          {{ product.reason }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 反馈按钮 -->
                <div v-if="message.recommendationId" class="feedback-section">
                  <span class="feedback-label">这个推荐对您有帮助吗？</span>
                  <el-button-group>
                    <el-button 
                      size="small" 
                      :type="message.feedback === 1 ? 'success' : 'default'"
                      @click="submitFeedback(message.recommendationId, 1)"
                    >
                      <el-icon><Check /></el-icon> 有帮助
                    </el-button>
                    <el-button 
                      size="small" 
                      :type="message.feedback === 0 ? 'danger' : 'default'"
                      @click="submitFeedback(message.recommendationId, 0)"
                    >
                      <el-icon><Close /></el-icon> 没帮助
                    </el-button>
                  </el-button-group>
                </div>

                <div class="message-time">{{ formatTime(message.time) }}</div>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="loading-message">
            <div class="ai-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              placeholder="请输入您的需求，例如：我想找一个适合家庭游玩的景点"
              @keyup.enter="sendMessage"
              :disabled="loading"
              class="message-input"
            />
            <el-button 
              @click="sendMessage" 
              type="primary" 
              :loading="loading"
              :disabled="!inputMessage.trim()"
              class="send-button"
            >
              发送
            </el-button>
          </div>
          <div class="input-tips">
            <span>您可以询问景点推荐、酒店预订、美食推荐等问题</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, defineOptions, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Check, Close, Plus } from '@element-plus/icons-vue'
import { getAiRecommendation, getAiRecommendationStream, getRecommendationHistory, submitFeedback as submitFeedbackApi } from '@/api/ai'
import { useUserStore } from '@/stores/user'

// 定义组件名称，用于keep-alive缓存
defineOptions({
  name: 'AiChat'
})

const router = useRouter()
const userStore = useUserStore()

// localStorage key
const CHAT_SESSION_KEY = 'ai_chat_session'

// 响应式数据
const inputMessage = ref('')
const loading = ref(false)
const showHistory = ref(false)
const historyLoading = ref(false)
const messagesContainer = ref(null)

const messages = ref([])
const historyList = ref([])

// 保存会话到localStorage
const saveSession = () => {
  try {
    const session = {
      messages: messages.value,
      inputMessage: inputMessage.value,
      timestamp: new Date().getTime()
    }
    localStorage.setItem(CHAT_SESSION_KEY, JSON.stringify(session))
  } catch (error) {
    console.error('保存会话失败:', error)
  }
}

// 从localStorage恢复会话
const restoreSession = () => {
  try {
    const sessionData = localStorage.getItem(CHAT_SESSION_KEY)
    if (sessionData) {
      const session = JSON.parse(sessionData)
      // 检查会话是否在24小时内
      const now = new Date().getTime()
      const sessionAge = now - session.timestamp
      const maxAge = 24 * 60 * 60 * 1000 // 24小时
      
      if (sessionAge < maxAge) {
        messages.value = session.messages || []
        inputMessage.value = session.inputMessage || ''
        console.log('已恢复上次会话')
      } else {
        // 会话过期，清除
        localStorage.removeItem(CHAT_SESSION_KEY)
        console.log('会话已过期，已清除')
      }
    }
  } catch (error) {
    console.error('恢复会话失败:', error)
  }
}

// 监听messages变化，自动保存
watch(messages, () => {
  saveSession()
}, { deep: true })

// 监听inputMessage变化，自动保存
watch(inputMessage, () => {
  saveSession()
})

// 检查登录状态
const checkLoginStatus = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再使用AI推荐功能')
    router.push('/login')
    return false
  }
  return true
}

// 快速问题
const quickQuestions = ref([
  '推荐一些适合家庭游玩的景点',
  '我想找一个价格实惠的酒店',
  '有什么特色的陶瓷体验活动',
  '推荐一些当地美食',
  '适合拍照的景点有哪些'
])

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  // 检查登录状态
  if (!checkLoginStatus()) return

  const userMessage = {
    id: Date.now(),
    type: 'user',
    content: inputMessage.value,
    time: new Date()
  }
  
  messages.value.push(userMessage)
  const query = inputMessage.value
  inputMessage.value = ''
  
  loading.value = true
  scrollToBottom()

  try {
    console.log('发送AI推荐请求:', { query })
    
    // 创建AI消息占位符
    const aiMessageId = Date.now() + 1
    const aiMessage = {
      id: aiMessageId,
      type: 'ai',
      content: '',
      recommendedProducts: [],
      recommendationId: null,
      feedback: null,
      time: new Date(),
      isStreaming: true
    }
    
    messages.value.push(aiMessage)
    scrollToBottom()
    
    // 模拟流式输出
    await simulateStreamingResponse(aiMessage, query)
    
  } catch (error) {
    console.error('AI推荐失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data
    })
    
    ElMessage.error(`AI推荐失败: ${error.response?.data?.message || error.message}`)
    
    const errorMessage = {
      id: Date.now() + 1,
      type: 'ai',
      content: `抱歉，AI推荐失败: ${error.response?.data?.message || error.message}`,
      time: new Date()
    }
    messages.value.push(errorMessage)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 发送快速问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 加载历史记录
const loadHistory = async () => {
  historyLoading.value = true
  try {
    const response = await getRecommendationHistory({
      current: 1,
      size: 20
    })
    historyList.value = response.data.records
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败')
  } finally {
    historyLoading.value = false
  }
}

// 加载历史记录项
const loadHistoryItem = (item) => {
  messages.value = [
    {
      id: item.id,
      type: 'user',
      content: item.query,
      time: new Date(item.createTime)
    },
    {
      id: item.id + 1,
      type: 'ai',
      content: item.response,
      recommendedProducts: item.recommendedProducts,
      recommendationId: item.id,
      feedback: item.feedback,
      time: new Date(item.createTime)
    }
  ]
  showHistory.value = false
  scrollToBottom()
}

// 提交反馈
const submitFeedback = async (recommendationId, feedback) => {
  try {
    await submitFeedbackApi({
      recommendationId,
      feedback
    })
    
    // 更新消息中的反馈状态
    const message = messages.value.find(m => m.recommendationId === recommendationId)
    if (message) {
      message.feedback = feedback
    }
    
    ElMessage.success('反馈提交成功')
  } catch (error) {
    console.error('提交反馈失败:', error)
    ElMessage.error('提交反馈失败')
  }
}

// 查看产品详情
const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

// 格式化AI响应
const formatAiResponse = (content) => {
  return content.replace(/\n/g, '<br>')
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 真正的流式输出（使用SSE）
const simulateStreamingResponse = async (aiMessage, query) => {
  let streamCompleted = false
  let hasReceivedAnyContent = false
  
  try {
    console.log('开始流式AI推荐请求:', query)
    
    // 调用流式API
    await getAiRecommendationStream(
      { query },
      {
        // 接收到内容块
        onContent: (content) => {
          console.log('收到内容块，长度:', content.length, '内容:', content.substring(0, 50))
          if (content && content.length > 0) {
            hasReceivedAnyContent = true
            aiMessage.content += content
            scrollToBottom()
          }
        },
        
        // 接收到推荐产品
        onProducts: (products, productIds) => {
          console.log('收到推荐产品，数量:', products?.length)
          if (products && products.length > 0) {
            aiMessage.recommendedProducts = products
            scrollToBottom()
          }
        },
        
        // 流式完成
        onComplete: (recommendationId) => {
          console.log('流式推荐完成，ID:', recommendationId, '已接收内容:', hasReceivedAnyContent, '内容长度:', aiMessage.content.length)
          streamCompleted = true
          aiMessage.recommendationId = recommendationId || Date.now()
          aiMessage.isStreaming = false
          
          // 只有在真的没有收到任何内容时才使用降级方案
          if (!hasReceivedAnyContent || !aiMessage.content || aiMessage.content.trim() === '') {
            console.warn('流式完成但没有内容，使用降级方案')
            ElMessage.warning('AI服务响应异常，为您推荐热门产品...')
            // 给一个小延迟，让可能延迟到达的内容有机会显示
            setTimeout(() => {
              if (!hasReceivedAnyContent || !aiMessage.content || aiMessage.content.trim() === '') {
                useMockData(aiMessage, query)
              }
            }, 500)
          }
        },
        
        // 发生错误
        onError: (errorMessage) => {
          console.error('流式推荐失败:', errorMessage)
          
          // 停止流式状态
          streamCompleted = true
          aiMessage.isStreaming = false
          
          // 如果没有内容，使用模拟数据
          if (!hasReceivedAnyContent) {
            ElMessage.warning('AI服务暂时不可用，正在为您推荐热门产品...')
            useMockData(aiMessage, query)
          } else {
            ElMessage.error('推荐过程中发生错误: ' + errorMessage)
          }
        }
      }
    )
    
    // 超时保护：如果60秒后还没完成，强制结束
    setTimeout(() => {
      if (!streamCompleted) {
        console.warn('流式推送超时，强制结束')
        aiMessage.isStreaming = false
        if (!hasReceivedAnyContent) {
          ElMessage.warning('AI响应超时，为您推荐热门产品...')
          useMockData(aiMessage, query)
        }
      }
    }, 60000) // 60秒超时
    
  } catch (error) {
    // 如果API调用失败，使用模拟数据
    console.error('流式AI调用失败:', error.message)
    console.error('错误堆栈:', error)
    
    // 停止流式状态
    aiMessage.isStreaming = false
    
    ElMessage.warning('AI服务暂时不可用，正在为您推荐热门产品...')
    useMockData(aiMessage, query)
  }
}

// 使用模拟数据
const useMockData = async (aiMessage, query) => {
  const mockResponse = generateMockResponse(query)
  
  // 设置为流式状态
  aiMessage.isStreaming = true
  aiMessage.content = ''  // 清空之前的内容
  
  await streamText(aiMessage, mockResponse.content)
  
  aiMessage.recommendedProducts = mockResponse.recommendedProducts
  aiMessage.recommendationId = Date.now()
  aiMessage.isStreaming = false
}

// 流式输出文本
const streamText = async (message, text) => {
  // 检查文本是否有效
  if (!text || typeof text !== 'string') {
    console.error('streamText: 无效的文本内容', text)
    message.content = '抱歉，AI返回了无效的内容。'
    return
  }
  
  console.log('开始流式输出，文本长度:', text.length)
  message.content = ''
  
  try {
    // 按字符分割，快速流式显示效果
    const chunkSize = 3 // 每次显示3个字符，速度更快更自然
    for (let i = 0; i < text.length; i += chunkSize) {
      message.content += text.slice(i, i + chunkSize)
      scrollToBottom()
      await new Promise(resolve => setTimeout(resolve, 20)) // 20ms延迟，类似ChatGPT的速度
    }
    
    console.log('流式输出完成，最终内容长度:', message.content.length)
  } catch (error) {
    console.error('流式输出错误:', error)
    // 如果出错，直接显示全部内容
    message.content = text
  }
}

// 生成模拟响应
const generateMockResponse = (query) => {
  const responses = {
    '酒店': {
      content: `根据您的需求"${query}"，我为您推荐以下价格实惠的酒店：

🏨 **景德镇经济型酒店推荐**

1. **陶溪川便捷酒店**
   - 价格：168元/晚
   - 位置：陶溪川文创街区
   - 特色：简约设计，免费停车
   - 评分：4.6分

2. **御窑厂连锁酒店**
   - 价格：198元/晚
   - 位置：御窑厂国家考古公园对面
   - 特色：24小时前台，含早餐
   - 评分：4.7分

3. **人民广场快捷酒店**
   - 价格：188元/晚
   - 位置：市中心人民广场旁
   - 特色：2023年新装修，智能家居
   - 评分：4.5分

这些酒店都位于景德镇的核心区域，交通便利，性价比很高。建议您根据具体需求选择。`,
      recommendedProducts: [
        {
          id: 1,
          title: '陶溪川便捷酒店',
          description: '简约设计风格的经济型酒店',
          coverImage: '/placeholder.jpg',
          price: '168',
          region: '陶溪川',
          address: '陶溪川文创街区',
          rating: 4.6,
          tags: '经济型,便捷,停车',
          reason: '价格实惠，位置优越'
        },
        {
          id: 2,
          title: '御窑厂连锁酒店',
          description: '24小时前台服务的连锁酒店',
          coverImage: '/placeholder.jpg',
          price: '198',
          region: '御窑厂',
          address: '御窑厂国家考古公园对面',
          rating: 4.7,
          tags: '连锁,早餐,服务',
          reason: '服务优质，含早餐'
        }
      ]
    },
    '景点': {
      content: `根据您的需求"${query}"，我为您推荐以下景德镇热门景点：

🏺 **景德镇必游景点推荐**

1. **景德镇古窑民俗博览区**
   - 门票：95元
   - 特色：体验陶瓷制作，观看古窑烧制
   - 推荐理由：了解景德镇陶瓷文化的最佳去处

2. **陶溪川文创街区**
   - 门票：免费
   - 特色：现代陶瓷艺术，创意市集
   - 推荐理由：感受传统与现代的完美结合

3. **御窑厂国家考古公园**
   - 门票：60元
   - 特色：明代御窑遗址，考古发现
   - 推荐理由：探索景德镇的历史底蕴

这些景点展现了景德镇作为瓷都的独特魅力，建议您合理安排时间游览。`,
      recommendedProducts: [
        {
          id: 3,
          title: '景德镇古窑民俗博览区',
          description: '体验陶瓷制作，观看古窑烧制',
          coverImage: '/placeholder.jpg',
          price: '95',
          region: '景德镇',
          address: '景德镇市昌江区',
          rating: 4.8,
          tags: '古窑,陶瓷,体验',
          reason: '了解陶瓷文化的最佳去处'
        },
        {
          id: 4,
          title: '陶溪川文创街区',
          description: '现代陶瓷艺术，创意市集',
          coverImage: '/placeholder.jpg',
          price: '0',
          region: '景德镇',
          address: '景德镇市珠山区',
          rating: 4.6,
          tags: '文创,艺术,免费',
          reason: '传统与现代的完美结合'
        }
      ]
    }
  }
  
  // 根据查询内容选择响应
  if (query.includes('酒店') || query.includes('住宿')) {
    return responses['酒店']
  } else if (query.includes('景点') || query.includes('游玩')) {
    return responses['景点']
  } else {
    return {
      content: `感谢您的查询"${query}"。我理解您想了解景德镇的文旅信息。

作为景德镇AI文旅助手，我可以为您推荐：
- 🏨 经济实惠的酒店住宿
- 🏺 经典的陶瓷文化景点
- 🍜 地道的当地美食
- 🎨 有趣的陶瓷体验活动

请告诉我您更具体的需求，比如：
- "推荐一些价格实惠的酒店"
- "有什么适合家庭游玩的景点"
- "推荐一些当地特色美食"

我会为您提供更精准的推荐！`,
      recommendedProducts: []
    }
  }
}

// 新建会话
const startNewChat = () => {
  messages.value = []
  inputMessage.value = ''
  showHistory.value = false
  // 清除保存的会话
  localStorage.removeItem(CHAT_SESSION_KEY)
  ElMessage.success('已开始新会话')
}

// 组件挂载时检查登录状态并加载历史记录
onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再使用AI推荐功能')
    router.push('/login')
    return
  }
  
  // 恢复上次会话（如果存在）
  restoreSession()
  
  // 加载历史记录
  loadHistory()
})
</script>

<style scoped>
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.chat-header {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.header-info h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.header-info p {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.chat-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.history-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h4 {
  margin: 0;
  color: #303133;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  border: 1px solid transparent;
  margin-bottom: 8px;
}

.history-item:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
}

.history-query {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 12px;
  color: #909399;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-feedback {
  margin-left: 8px;
}

.empty-history {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.welcome-content {
  text-align: center;
  max-width: 500px;
}

.welcome-content h3 {
  margin: 16px 0 8px 0;
  color: #303133;
}

.welcome-content p {
  color: #606266;
  margin-bottom: 24px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  background: #409eff;
  color: white;
}

.message-item {
  margin-bottom: 20px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 12px;
}

.ai-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-content {
  max-width: 70%;
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.user-message .message-content {
  background: #409eff;
  color: white;
}

.message-text {
  line-height: 1.5;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.user-message .message-time {
  color: rgba(255,255,255,0.8);
}

.recommended-products {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.recommended-products h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.product-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.product-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.product-card img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.product-info {
  padding: 12px;
}

.product-info h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #f56c6c;
  font-weight: bold;
  margin: 0 0 4px 0;
  font-size: 14px;
}

.product-region {
  color: #909399;
  font-size: 12px;
  margin: 0 0 8px 0;
}

.product-reason {
  color: #606266;
  font-size: 12px;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  margin: 0;
}

.feedback-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 12px;
}

.feedback-label {
  font-size: 12px;
  color: #606266;
}

.loading-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.typing-cursor {
  animation: blink 1s infinite;
  color: #409eff;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.input-area {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 16px 24px;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-input {
  flex: 1;
  min-height: 40px;
}

.message-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.send-button {
  min-width: 80px;
}

.input-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .history-sidebar {
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }
  
  .history-sidebar.show {
    transform: translateX(0);
  }
  
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>
