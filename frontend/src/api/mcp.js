/**
 * MCP Server API
 * 景德镇旅游MCP Server的前端调用封装
 */
import request from '@/utils/request'

/**
 * MCP请求ID计数器
 */
let mcpRequestId = 0

/**
 * 调用MCP工具
 * @param {string} toolName - 工具名称
 * @param {object} args - 工具参数
 * @returns {Promise} 工具执行结果
 */
async function callMcpTool(toolName, args = {}) {
  mcpRequestId++
  
  const mcpRequest = {
    jsonrpc: '2.0',
    id: mcpRequestId.toString(),
    method: 'tools/call',
    params: {
      name: toolName,
      arguments: args
    }
  }
  
  try {
    // 直接使用axios发送请求，不经过统一响应拦截器
    // 因为MCP响应格式与标准API不同
    const response = await request.post('/mcp/message', mcpRequest, {
      transformResponse: [(data) => {
        // 不做任何转换，直接返回MCP响应
        return typeof data === 'string' ? JSON.parse(data) : data
      }]
    })
    
    // 检查MCP错误
    if (response.error) {
      throw new Error(response.error.message || 'MCP工具调用失败')
    }
    
    // 从MCP响应中提取实际数据
    if (response.result && response.result.content) {
      const content = response.result.content[0].text
      return JSON.parse(content)
    }
    
    return response.result
  } catch (error) {
    console.error(`MCP工具调用失败: ${toolName}`, error)
    throw error
  }
}

/**
 * 搜索景点
 * @param {object} params - 搜索参数
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.region - 所在区域
 * @param {number} params.minPrice - 最低价格
 * @param {number} params.maxPrice - 最高价格
 * @param {number} params.minRating - 最低评分
 * @param {string} params.sortBy - 排序方式
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise<object>} 景点列表
 */
export function searchAttractions(params) {
  return callMcpTool('search_attractions', params)
}

/**
 * 生成每日旅游行程
 * @param {object} params - 行程参数
 * @param {number} params.days - 旅游天数（必填）
 * @param {number} params.budget - 总预算
 * @param {string} params.interests - 兴趣偏好（逗号分隔）
 * @param {string} params.pace - 旅游节奏 (relaxed/moderate/intense)
 * @param {string} params.startDate - 开始日期 (YYYY-MM-DD)
 * @returns {Promise<object>} 每日行程计划
 */
export function recommendDailyPlan(params) {
  return callMcpTool('recommend_daily_plan', params)
}

/**
 * 查找住宿
 * @param {object} params - 住宿搜索参数
 * @param {string} params.region - 所在区域
 * @param {string} params.checkInDate - 入住日期 (YYYY-MM-DD)
 * @param {string} params.checkOutDate - 退房日期 (YYYY-MM-DD)
 * @param {number} params.minPrice - 最低价格/晚
 * @param {number} params.maxPrice - 最高价格/晚
 * @param {number} params.minRating - 最低评分
 * @param {string} params.facilities - 设施要求（逗号分隔）
 * @param {string} params.hotelType - 酒店类型
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise<object>} 住宿列表
 */
export function findAccommodations(params) {
  return callMcpTool('find_accommodations', params)
}

/**
 * 计算旅行预算
 * @param {object} params - 预算计算参数
 * @param {number} params.days - 旅游天数（必填）
 * @param {number} params.people - 旅游人数
 * @param {string} params.accommodationLevel - 住宿标准 (budget/standard/comfort/luxury)
 * @param {string} params.mealLevel - 用餐标准 (budget/standard/premium)
 * @param {boolean} params.includeTransport - 是否包含交通
 * @param {string} params.transportType - 交通方式 (train/plane/car/bus)
 * @param {string} params.departureCity - 出发城市
 * @param {number} params.shoppingBudget - 购物预算/人
 * @param {boolean} params.includeShopping - 是否包含购物
 * @returns {Promise<object>} 详细预算清单
 */
export function getTravelBudget(params) {
  return callMcpTool('get_travel_budget', params)
}

/**
 * 获取MCP服务器信息
 * @returns {Promise<object>} 服务器信息
 */
export function getMcpServerInfo() {
  return request.get('/mcp/info')
}

/**
 * 获取所有可用的MCP工具列表
 * @returns {Promise<object>} 工具列表
 */
export function getMcpTools() {
  return request.get('/mcp/tools')
}

/**
 * MCP健康检查
 * @returns {Promise<object>} 健康状态
 */
export function checkMcpHealth() {
  return request.get('/mcp/health')
}

export default {
  searchAttractions,
  recommendDailyPlan,
  findAccommodations,
  getTravelBudget,
  getMcpServerInfo,
  getMcpTools,
  checkMcpHealth
}

