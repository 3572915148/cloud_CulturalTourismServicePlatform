import request from '@/utils/request'

export function getCategoryList(params) {
  return request({
    url: '/category/list',
    method: 'get',
    params
  })
}

/**
 * 获取分类统计信息（包含每个分类的产品数量）
 */
export function getCategoryStatistics() {
  return request({
    url: '/category/statistics',
    method: 'get'
  })
}


