package com.jingdezhen.tourism.agent.tool.impl;

import com.jingdezhen.tourism.agent.tool.AgentTool;
import com.jingdezhen.tourism.agent.tool.ToolResult;
import com.jingdezhen.tourism.entity.Orders;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mapper.OrdersMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 创建订单工具
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderTool implements AgentTool {
    
    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;
    
    @Override
    public String getName() {
        return "create_order";
    }
    
    @Override
    public String getDescription() {
        return "为用户创建订单。需要产品ID、数量、预订日期、联系人信息。创建成功后返回订单号和订单详情。";
    }
    
    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "productId": {
                    "type": "integer",
                    "description": "产品ID"
                },
                "quantity": {
                    "type": "integer",
                    "description": "购买数量",
                    "minimum": 1
                },
                "bookingDate": {
                    "type": "string",
                    "description": "预订日期，格式：YYYY-MM-DD"
                },
                "contactName": {
                    "type": "string",
                    "description": "联系人姓名"
                },
                "contactPhone": {
                    "type": "string",
                    "description": "联系人电话"
                },
                "remarks": {
                    "type": "string",
                    "description": "订单备注（可选）"
                }
            },
            "required": ["productId", "quantity", "bookingDate", "contactName", "contactPhone"]
        }
        """;
    }
    
    @Override
    public String getCategory() {
        return "order";
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ToolResult execute(Map<String, Object> parameters, Long userId) {
        try {
            log.info("🛒 创建订单: userId={}, params={}", userId, parameters);
            
            // 提取参数
            Long productId = ((Number) parameters.get("productId")).longValue();
            Integer quantity = ((Number) parameters.get("quantity")).intValue();
            String contactName = (String) parameters.get("contactName");
            String contactPhone = (String) parameters.get("contactPhone");
            String remarks = (String) parameters.get("remarks");
            
            // 解析预订日期
            String dateStr = (String) parameters.get("bookingDate");
            LocalDate bookingDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            
            // 查询产品信息
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                log.warn("⚠️ 产品不存在或已下架: productId={}", productId);
                return ToolResult.error("产品不存在或已下架", "PRODUCT_NOT_FOUND");
            }
            
            // 检查库存
            if (product.getStock() < quantity) {
                log.warn("⚠️ 库存不足: productId={}, stock={}, quantity={}", 
                    productId, product.getStock(), quantity);
                return ToolResult.error(
                    String.format("库存不足，当前库存：%d，需要：%d", product.getStock(), quantity),
                    "INSUFFICIENT_STOCK"
                );
            }
            
            // 创建订单
            Orders order = new Orders();
            order.setUserId(userId);
            order.setMerchantId(product.getMerchantId());
            order.setProductId(productId);
            
            // 生成订单号
            String orderNumber = generateOrderNumber();
            order.setOrderNo(orderNumber);
            
            // 保存产品快照
            order.setProductTitle(product.getTitle());
            order.setProductImage(product.getCoverImage());
            order.setPrice(product.getPrice());
            
            // 计算总金额
            BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));
            order.setQuantity(quantity);
            order.setTotalAmount(totalAmount);
            
            // 联系人信息
            order.setContactName(contactName);
            order.setContactPhone(contactPhone);
            order.setBookingDate(bookingDate);
            order.setRemark(remarks);
            
            // 订单状态：待支付
            order.setStatus(0);
            order.setCreateTime(LocalDateTime.now());
            
            // 保存订单
            ordersMapper.insert(order);
            
            // 扣减库存
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);
            
            log.info("✅ 订单创建成功: orderNumber={}, orderId={}", orderNumber, order.getId());
            
            // 返回订单信息
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderId", order.getId());
            orderInfo.put("orderNumber", order.getOrderNo());
            orderInfo.put("productTitle", order.getProductTitle());
            orderInfo.put("quantity", order.getQuantity());
            orderInfo.put("totalAmount", order.getTotalAmount());
            orderInfo.put("status", "待支付");
            orderInfo.put("createTime", order.getCreateTime().toString());
            orderInfo.put("bookingDate", order.getBookingDate().toString());
            
            return ToolResult.builder()
                .success(true)
                .data(orderInfo)
                .message(String.format("订单创建成功！订单号：%s，总金额：¥%.2f，请在30分钟内完成支付。", 
                    order.getOrderNo(), order.getTotalAmount()))
                .build();
                
        } catch (Exception e) {
            log.error("❌ 创建订单失败: userId={}, params={}", userId, parameters, e);
            return ToolResult.error("创建订单失败：" + e.getMessage(), "CREATE_ORDER_ERROR");
        }
    }
    
    /**
     * 生成订单号
     * 格式：ORD + 时间戳 + 随机UUID前8位
     */
    private String generateOrderNumber() {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}

