#!/bin/bash

# 测试前端AI推荐功能
echo "=== 测试前端AI推荐功能 ==="

# 设置API基础URL
API_BASE="http://localhost:3000"

# 1. 用户登录
echo "1. 用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 提取token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "获取到Token: $TOKEN"

if [ -z "$TOKEN" ]; then
    echo "登录失败，请先创建测试用户"
    exit 1
fi

# 2. 测试AI推荐功能
echo ""
echo "2. 测试AI推荐功能..."

AI_RESPONSE=$(curl -s -X POST "$API_BASE/api/ai/recommend" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "我想买陶瓷茶具",
    "preferences": "青花瓷",
    "budget": "500元以内",
    "peopleCount": 2
  }')

echo "AI推荐响应: $AI_RESPONSE"

# 3. 检查响应格式
echo ""
echo "3. 检查响应格式..."
if echo "$AI_RESPONSE" | grep -q '"code":200'; then
    echo "✅ API调用成功"
    
    # 检查是否有推荐产品
    if echo "$AI_RESPONSE" | grep -q '"recommendedProducts"'; then
        echo "✅ 包含推荐产品"
        
        # 提取产品数量
        PRODUCT_COUNT=$(echo "$AI_RESPONSE" | grep -o '"recommendedProducts":\[[^]]*\]' | grep -o '{"id":' | wc -l)
        echo "推荐产品数量: $PRODUCT_COUNT"
        
        if [ "$PRODUCT_COUNT" -gt 0 ]; then
            echo "✅ 推荐产品数据正常"
        else
            echo "❌ 推荐产品数据为空"
        fi
    else
        echo "❌ 缺少推荐产品数据"
    fi
else
    echo "❌ API调用失败"
    echo "错误信息: $AI_RESPONSE"
fi

echo ""
echo "=== 测试完成 ==="
