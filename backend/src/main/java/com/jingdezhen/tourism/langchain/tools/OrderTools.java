package com.jingdezhen.tourism.langchain.tools;

import com.alibaba.fastjson2.JSON;
import com.jingdezhen.tourism.entity.Orders;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mapper.OrdersMapper;
import com.jingdezhen.tourism.mapper.ProductMapper;
import com.jingdezhen.tourism.service.StockService;
import com.jingdezhen.tourism.utils.RedisLockUtil;
import dev.langchain4j.agent.tool.Tool;
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
 * 订单相关工具
 * 使用 LangChain4j 的 @Tool 注解定义工具
 * 
 * @author AI Assistant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTools {
    
    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;
    private final StockService stockService;
    private final RedisLockUtil redisLockUtil;
    
    /**
     * 创建订单工具
     * 注意：userId 需要通过其他方式传入（如上下文）
     */
    @Tool("为用户创建订单。需要产品ID、数量、预订日期、联系人信息。创建成功后返回订单号和订单详情")
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(
            Long userId,            // 用户ID（必需）
            Long productId,         // 产品ID（必需）
            Integer quantity,       // 购买数量（必需）
            String bookingDate,     // 预订日期 YYYY-MM-DD（必需）
            String contactName,     // 联系人姓名（必需）
            String contactPhone,    // 联系人电话（必需）
            String remarks) {       // 订单备注（可选）
        
        try {
            log.info("🛒 [LangChain4j] 创建订单: userId={}, productId={}, quantity={}", 
                    userId, productId, quantity);
            
            // 参数验证
            if (userId == null || productId == null || quantity == null || quantity <= 0) {
                return JSON.toJSONString(Map.of(
                        "success", false,
                        "message", "参数错误：用户ID、产品ID和数量不能为空",
                        "errorCode", "INVALID_PARAMETERS"
                ));
            }
            
            if (contactName == null || contactName.trim().isEmpty() || 
                contactPhone == null || contactPhone.trim().isEmpty()) {
                return JSON.toJSONString(Map.of(
                        "success", false,
                        "message", "联系人姓名和电话不能为空",
                        "errorCode", "INVALID_CONTACT_INFO"
                ));
            }
            
            // 解析预订日期
            LocalDate bookingDateParsed;
            try {
                bookingDateParsed = LocalDate.parse(bookingDate, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                return JSON.toJSONString(Map.of(
                        "success", false,
                        "message", "预订日期格式错误，请使用 YYYY-MM-DD 格式",
                        "errorCode", "INVALID_DATE_FORMAT"
                ));
            }
            
            // 使用分布式锁，防止重复下单
            String lockKey = "order:create:langchain:" + userId + ":" + productId;
            return redisLockUtil.executeWithLock(lockKey, 3, 10, () -> {
                // 查询产品信息
                Product product = productMapper.selectById(productId);
                if (product == null || product.getStatus() != 1) {
                    return JSON.toJSONString(Map.of(
                            "success", false,
                            "message", "产品不存在或已下架",
                            "errorCode", "PRODUCT_NOT_FOUND"
                    ));
                }
                
                // 检查库存
                Integer currentStock = stockService.getStock(productId);
                if (currentStock == null || currentStock < quantity) {
                    return JSON.toJSONString(Map.of(
                            "success", false,
                            "message", String.format("库存不足，当前库存：%d，需要：%d", 
                                    currentStock != null ? currentStock : 0, quantity),
                            "errorCode", "INSUFFICIENT_STOCK"
                    ));
                }
                
                // 扣减库存
                boolean stockDecreased = stockService.decreaseStock(productId, quantity);
                if (!stockDecreased) {
                    return JSON.toJSONString(Map.of(
                            "success", false,
                            "message", "库存不足，请稍后重试",
                            "errorCode", "INSUFFICIENT_STOCK"
                    ));
                }
                
                try {
                    // 创建订单
                    Orders order = new Orders();
                    order.setUserId(userId);
                    order.setMerchantId(product.getMerchantId());
                    order.setProductId(productId);
                    order.setOrderNo(generateOrderNumber());
                    order.setProductTitle(product.getTitle());
                    order.setProductImage(product.getCoverImage());
                    order.setPrice(product.getPrice());
                    
                    BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));
                    order.setQuantity(quantity);
                    order.setTotalAmount(totalAmount);
                    order.setContactName(contactName);
                    order.setContactPhone(contactPhone);
                    order.setBookingDate(bookingDateParsed);
                    order.setRemark(remarks);
                    order.setStatus(0);  // 待支付
                    order.setCreateTime(LocalDateTime.now());
                    
                    ordersMapper.insert(order);
                    
                    log.info("✅ 订单创建成功: orderNo={}", order.getOrderNo());
                    
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
                    
                    return JSON.toJSONString(Map.of(
                            "success", true,
                            "data", orderInfo,
                            "message", String.format("订单创建成功！订单号：%s，总金额：¥%.2f，请在30分钟内完成支付", 
                                    order.getOrderNo(), order.getTotalAmount())
                    ));
                    
                } catch (Exception e) {
                    // 恢复库存
                    log.error("❌ 订单创建失败，恢复库存", e);
                    stockService.increaseStock(productId, quantity);
                    throw e;
                }
            });
            
        } catch (Exception e) {
            log.error("❌ 创建订单失败", e);
            return JSON.toJSONString(Map.of(
                    "success", false,
                    "message", "创建订单失败：" + e.getMessage(),
                    "errorCode", "CREATE_ORDER_ERROR"
            ));
        }
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}

