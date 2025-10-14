package com.jingdezhen.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingdezhen.tourism.dto.OrderCreateDTO;
import com.jingdezhen.tourism.entity.Orders;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.exception.BusinessException;
import com.jingdezhen.tourism.mapper.OrdersMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.service.OrdersService;
import com.jingdezhen.tourism.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单Service实现
 */
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateDTO dto, Long userId) {
        // 查询产品信息
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("产品已下架");
        }
        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(product.getMerchantId());
        order.setProductId(product.getId());
        order.setQuantity(dto.getQuantity());
        order.setPrice(product.getPrice());
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(dto.getQuantity())));
        order.setStatus(0); // 待支付
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setRemark(dto.getRemark());

        ordersMapper.insert(order);

        // 扣减库存
        product.setStock(product.getStock() - dto.getQuantity());
        productMapper.updateById(product);

        // 返回订单信息
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setProductTitle(product.getTitle());
        vo.setProductImage(product.getCoverImage());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setCanReview(false);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态错误");
        }

        // 更新订单状态为已支付
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        // 更新产品销量
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setSales((product.getSales() == null ? 0 : product.getSales()) + order.getQuantity());
            productMapper.updateById(product);
        }
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }

        // 查询产品信息
        Product product = productMapper.selectById(order.getProductId());

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductImage(product.getCoverImage());
        }
        vo.setStatusText(getStatusText(order.getStatus()));
        // 已支付且未评价可以评价
        vo.setCanReview(order.getStatus() == 2);

        return vo;
    }

    @Override
    public Page<OrderVO> getUserOrders(Long userId, Long current, Long size, Integer status) {
        Page<Orders> page = new Page<>(current, size);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);

        Page<Orders> ordersPage = ordersMapper.selectPage(page, wrapper);

        // 查询所有订单对应的产品信息
        List<Long> productIds = ordersPage.getRecords().stream()
                .map(Orders::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        }

        // 转换为VO
        Map<Long, Product> finalProductMap = productMap;
        List<OrderVO> voList = ordersPage.getRecords().stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            
            Product product = finalProductMap.get(order.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductImage(product.getCoverImage());
            }
            
            vo.setStatusText(getStatusText(order.getStatus()));
            vo.setCanReview(order.getStatus() == 2);
            
            return vo;
        }).collect(Collectors.toList());

        Page<OrderVO> voPage = new Page<>(ordersPage.getCurrent(), ordersPage.getSize(), ordersPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待支付订单可以取消");
        }

        // 更新订单状态
        order.setStatus(3); // 已取消
        ordersMapper.updateById(order);

        // 恢复库存
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + order.getQuantity());
            productMapper.updateById(product);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOrder(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有已支付订单可以完成");
        }

        // 更新订单状态
        order.setStatus(2); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 格式：yyyyMMddHHmmss + 6位随机数
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%06d", (int) (Math.random() * 1000000));
        return timeStr + randomStr;
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "退款中";
            case 5: return "已退款";
            default: return "未知状态";
        }
    }
}

