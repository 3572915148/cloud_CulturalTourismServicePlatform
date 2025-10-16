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

