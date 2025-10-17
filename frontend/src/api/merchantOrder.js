import request from '@/utils/request'

/**
 * 获取商户订单列表
 */
export function getMerchantOrders(params) {
  return request({
    url: '/merchant/orders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(id) {
  return request({
    url: `/merchant/orders/${id}`,
    method: 'get'
  })
}

/**
 * 确认订单
 */
export function confirmOrder(id) {
  return request({
    url: `/merchant/orders/${id}/confirm`,
    method: 'post'
  })
}

/**
 * 完成订单
 */
export function completeOrder(id) {
  return request({
    url: `/merchant/orders/${id}/complete`,
    method: 'post'
  })
}

/**
 * 取消订单
 */
export function cancelOrder(id, reason) {
  return request({
    url: `/merchant/orders/${id}/cancel`,
    method: 'post',
    data: reason
  })
}

/**
 * 退款订单
 */
export function refundOrder(id) {
  return request({
    url: `/merchant/orders/${id}/refund`,
    method: 'post'
  })
}

