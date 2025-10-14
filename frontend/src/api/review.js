import request from '@/utils/request'

/**
 * 创建评价
 */
export function createReview(data) {
  return request({
    url: '/review/create',
    method: 'post',
    data
  })
}

/**
 * 获取产品评价列表
 */
export function getProductReviews(productId, params) {
  return request({
    url: `/review/product/${productId}`,
    method: 'get',
    params
  })
}

/**
 * 获取我的评价列表
 */
export function getMyReviews(params) {
  return request({
    url: '/review/my',
    method: 'get',
    params
  })
}

/**
 * 删除评价
 */
export function deleteReview(reviewId) {
  return request({
    url: `/review/${reviewId}`,
    method: 'delete'
  })
}

