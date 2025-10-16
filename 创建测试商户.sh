#!/bin/bash

# ========================================
# 通过API创建测试商户账号
# ========================================

API_BASE="http://localhost:8080/api"

echo "======================================"
echo "  创建测试商户账号"
echo "======================================"
echo ""

# 商户1：陶瓷旗舰店
echo "创建商户1：景德镇陶瓷旗舰店..."
curl -X POST "${API_BASE}/merchant/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "merchant001",
    "password": "123456",
    "shopName": "景德镇陶瓷旗舰店",
    "shopIntroduction": "专注景德镇传统陶瓷工艺，提供高品质青花瓷、粉彩瓷等精品。三代传承，匠心制作，每一件都是艺术品。",
    "contactPerson": "张大师",
    "contactPhone": "13800138001",
    "contactEmail": "ceramic001@jdz.com",
    "address": "江西省景德镇市珠山区陶瓷大道188号"
  }'
echo -e "\n"

# 商户2：瓷都商务酒店
echo "创建商户2：瓷都商务酒店..."
curl -X POST "${API_BASE}/merchant/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "merchant002",
    "password": "123456",
    "shopName": "瓷都商务酒店",
    "shopIntroduction": "位于景德镇市中心，交通便利。客房宽敞舒适，配备完善设施，是您商务出行和旅游度假的理想选择。",
    "contactPerson": "李经理",
    "contactPhone": "13800138002",
    "contactEmail": "hotel002@jdz.com",
    "address": "江西省景德镇市昌江区广场北路66号"
  }'
echo -e "\n"

# 商户3：瓷都风味餐厅
echo "创建商户3：瓷都风味餐厅..."
curl -X POST "${API_BASE}/merchant/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "merchant003",
    "password": "123456",
    "shopName": "瓷都风味餐厅",
    "shopIntroduction": "地道景德镇特色美食，传承百年烹饪技艺。主打饺子粑、碱水粑、冷粉等本地小吃，让您品味瓷都地道风味。",
    "contactPerson": "王厨师",
    "contactPhone": "13800138003",
    "contactEmail": "food003@jdz.com",
    "address": "江西省景德镇市珠山区中华北路128号"
  }'
echo -e "\n"

# 商户4：景德镇文化旅行社
echo "创建商户4：景德镇文化旅行社..."
curl -X POST "${API_BASE}/merchant/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "merchant004",
    "password": "123456",
    "shopName": "景德镇文化旅行社",
    "shopIntroduction": "专业景德镇文化深度游，提供陶瓷博物馆、古窑遗址、大师工作室等精品线路。专业导游，品质保证。",
    "contactPerson": "赵导",
    "contactPhone": "13800138004",
    "contactEmail": "travel004@jdz.com",
    "address": "江西省景德镇市昌江区瓷都大道298号"
  }'
echo -e "\n"

# 商户5：景瓷艺术体验馆
echo "创建商户5：景瓷艺术体验馆..."
curl -X POST "${API_BASE}/merchant/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "merchant005",
    "password": "123456",
    "shopName": "景瓷艺术体验馆",
    "shopIntroduction": "提供陶瓷制作全流程体验，从拉坯、绘画到烧制，专业老师一对一指导。适合亲子活动、团建、陶艺爱好者。",
    "contactPerson": "陈老师",
    "contactPhone": "13800138005",
    "contactEmail": "art005@jdz.com",
    "address": "江西省景德镇市浮梁县三宝国际陶艺村"
  }'
echo -e "\n"

echo "======================================"
echo "✅ 测试商户账号创建完成！"
echo ""
echo "所有账号密码都是：123456"
echo ""
echo "注意：这些商户的审核状态为待审核(0)"
echo "如需登录使用，请先在数据库中将audit_status设置为1"
echo ""
echo "或执行以下SQL："
echo "UPDATE merchant SET audit_status = 1 WHERE username LIKE 'merchant%';"
echo "======================================"

