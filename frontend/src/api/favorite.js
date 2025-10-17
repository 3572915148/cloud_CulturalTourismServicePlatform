import request from '@/utils/request'

/**
 * 添加收藏
 */
export function addFavorite(productId) {
  return request({
    url: '/favorite/add',
    method: 'post',
    params: { productId }
  })
}

/**
 * 取消收藏
 */
export function removeFavorite(productId) {
  return request({
    url: '/favorite/remove',
    method: 'delete',
    params: { productId }
  })
}

/**
 * 检查是否已收藏
 */
export function checkFavorite(productId) {
  return request({
    url: '/favorite/check',
    method: 'get',
    params: { productId }
  })
}

/**
 * 获取我的收藏列表
 */
export function getMyFavorites(params) {
  return request({
    url: '/favorite/my',
    method: 'get',
    params
  })
}

/**
 * 切换收藏状态（智能收藏/取消收藏）
 */
export function toggleFavorite(productId) {
  return request({
    url: '/favorite/toggle',
    method: 'post',
    params: { productId }
  })
}

/**
 * 获取收藏数量
 */
export function getFavoriteCount() {
  return request({
    url: '/favorite/count',
    method: 'get'
  })
}

