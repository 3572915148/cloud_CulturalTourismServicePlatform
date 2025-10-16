import request from '@/utils/request'

/**
 * 获取陶瓷文化内容列表（分页）
 * @param {Object} params - 查询参数
 * @param {Number} params.current - 当前页
 * @param {Number} params.size - 每页大小
 * @param {String} params.category - 分类（可选）
 * @returns {Promise}
 */
export function getCeramicContentList(params) {
  return request({
    url: '/ceramic/list',
    method: 'get',
    params
  })
}

/**
 * 获取陶瓷文化内容详情
 * @param {Number} id - 内容ID
 * @returns {Promise}
 */
export function getCeramicContentById(id) {
  return request({
    url: `/ceramic/${id}`,
    method: 'get'
  })
}

/**
 * 增加陶瓷文化内容浏览量
 * @param {Number} id - 内容ID
 * @returns {Promise}
 */
export function incrementCeramicViews(id) {
  return request({
    url: `/ceramic/${id}/view`,
    method: 'post'
  })
}

/**
 * 获取陶瓷文化分类统计
 * @returns {Promise}
 */
export function getCeramicCategories() {
  return request({
    url: '/ceramic/categories',
    method: 'get'
  })
}

