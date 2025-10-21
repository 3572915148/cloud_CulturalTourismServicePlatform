#!/bin/bash

# 测试AI推荐功能脚本
echo "=== 测试AI推荐功能 ==="

# 设置API基础URL
API_BASE="http://localhost:8080"

# 测试用户登录
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

# 测试AI推荐功能
echo ""
echo "2. 测试AI推荐功能..."

# 测试查询：陶瓷相关
echo "测试查询：陶瓷茶具"
AI_RESPONSE=$(curl -s -X POST "$API_BASE/api/ai/recommendation" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "我想买陶瓷茶具",
    "preferences": "青花瓷",
    "budget": "500元以内",
    "peopleCount": 2
  }')

echo "AI推荐响应: $AI_RESPONSE"

# 测试查询：酒店相关
echo ""
echo "测试查询：酒店住宿"
AI_RESPONSE2=$(curl -s -X POST "$API_BASE/api/ai/recommendation" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "推荐一家酒店",
    "preferences": "商务出行",
    "budget": "300-500元",
    "peopleCount": 1
  }')

echo "AI推荐响应: $AI_RESPONSE2"

# 测试查询：美食相关
echo ""
echo "测试查询：景德镇美食"
AI_RESPONSE3=$(curl -s -X POST "$API_BASE/api/ai/recommendation" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "景德镇特色美食",
    "preferences": "地道小吃",
    "budget": "100元以内",
    "peopleCount": 3
  }')

echo "AI推荐响应: $AI_RESPONSE3"

echo ""
echo "=== 测试完成 ==="
echo "请检查推荐的产品是否都是数据库中的真实产品"
