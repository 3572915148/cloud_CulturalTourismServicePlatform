import request from '@/utils/request'

/**
 * 创建反馈
 */
export function createFeedback(data) {
  return request({
    url: '/feedback/create',
    method: 'post',
    data
  })
}

/**
 * 获取我的反馈列表
 */
export function getMyFeedbacks(params) {
  return request({
    url: '/feedback/my',
    method: 'get',
    params
  })
}

/**
 * 获取反馈详情
 */
export function getFeedbackDetail(feedbackId) {
  return request({
    url: `/feedback/${feedbackId}`,
    method: 'get'
  })
}

/**
 * 删除反馈
 */
export function deleteFeedback(feedbackId) {
  return request({
    url: `/feedback/${feedbackId}`,
    method: 'delete'
  })
}

