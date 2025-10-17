import request from '@/utils/request'

/**
 * 商户注册
 * @param {Object} data - 注册信息
 * @returns {Promise}
 */
export function merchantRegister(data) {
  return request({
    url: '/merchant/register',
    method: 'post',
    data
  })
}

/**
 * 商户登录
 * @param {Object} data - 登录信息
 * @returns {Promise}
 */
export function merchantLogin(data) {
  return request({
    url: '/merchant/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前商户信息
 * @returns {Promise}
 */
export function getMerchantInfo() {
  return request({
    url: '/merchant/info',
    method: 'get'
  })
}

/**
 * 更新商户信息
 * @param {Object} data - 更新信息
 * @returns {Promise}
 */
export function updateMerchantInfo(data) {
  return request({
    url: '/merchant/info',
    method: 'put',
    data
  })
}

// ====== 商户产品相关 ======
export function listMyProducts(params) {
  return request({
    url: '/merchant/products',
    method: 'get',
    params
  })
}

export function createMyProduct(data) {
  return request({
    url: '/merchant/product',
    method: 'post',
    data
  })
}

export function updateMyProduct(id, data) {
  return request({
    url: `/merchant/product/${id}`,
    method: 'put',
    data
  })
}

export function deleteMyProduct(id) {
  return request({
    url: `/merchant/product/${id}`,
    method: 'delete'
  })
}

// ====== 商户评价相关 ======
export function listMyReviews(params) {
  return request({
    url: '/merchant/reviews',
    method: 'get',
    params
  })
}

export function replyReview(id, content) {
  return request({
    url: `/merchant/review/${id}/reply`,
    method: 'post',
    params: { content }
  })
}

