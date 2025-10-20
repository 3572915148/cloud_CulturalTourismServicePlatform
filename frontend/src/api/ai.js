import request from '@/utils/request'

/**
 * AI推荐相关API
 */

// 获取AI推荐
export function getAiRecommendation(data) {
  return request({
    url: '/ai/recommend',
    method: 'post',
    data
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
