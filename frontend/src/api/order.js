import request from '@/utils/request'

/**
 * 创建订单
 */
export function createOrder(data) {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

/**
 * 支付订单
 */
export function payOrder(orderId) {
  return request({
    url: `/order/pay/${orderId}`,
    method: 'post'
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderId) {
  return request({
    url: `/order/${orderId}`,
    method: 'get'
  })
}

/**
 * 获取我的订单列表
 */
export function getMyOrders(params) {
  return request({
    url: '/order/my',
    method: 'get',
    params
  })
}

/**
 * 取消订单
 */
export function cancelOrder(orderId) {
  return request({
    url: `/order/cancel/${orderId}`,
    method: 'post'
  })
}

/**
 * 完成订单
 */
export function finishOrder(orderId) {
  return request({
    url: `/order/finish/${orderId}`,
    method: 'post'
  })
}

