#!/bin/bash

echo "🧪 测试收藏切换功能"
echo "========================"

# 测试用的产品ID和用户token（需要替换为实际值）
PRODUCT_ID=4
USER_TOKEN="Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzMwNjYzNjQ4LCJleHAiOjE3MzA3NTAwNDh9.test"

echo "📋 测试步骤："
echo "1. 检查当前收藏状态"
echo "2. 执行收藏切换"
echo "3. 再次检查收藏状态"
echo "4. 再次执行收藏切换"
echo "5. 最后检查收藏状态"
echo ""

echo "=== 步骤1: 检查当前收藏状态 ==="
curl -X GET "http://localhost:8080/api/favorite/check?productId=$PRODUCT_ID" \
  -H "Authorization: $USER_TOKEN" \
  -H "Content-Type: application/json" 2>/dev/null | jq '.'

echo ""
echo "=== 步骤2: 执行收藏切换 ==="
curl -X POST "http://localhost:8080/api/favorite/toggle?productId=$PRODUCT_ID" \
  -H "Authorization: $USER_TOKEN" \
  -H "Content-Type: application/json" 2>/dev/null | jq '.'

echo ""
echo "=== 步骤3: 再次检查收藏状态 ==="
curl -X GET "http://localhost:8080/api/favorite/check?productId=$PRODUCT_ID" \
  -H "Authorization: $USER_TOKEN" \
  -H "Content-Type: application/json" 2>/dev/null | jq '.'

echo ""
echo "=== 步骤4: 再次执行收藏切换 ==="
curl -X POST "http://localhost:8080/api/favorite/toggle?productId=$PRODUCT_ID" \
  -H "Authorization: $USER_TOKEN" \
  -H "Content-Type: application/json" 2>/dev/null | jq '.'

echo ""
echo "=== 步骤5: 最后检查收藏状态 ==="
curl -X GET "http://localhost:8080/api/favorite/check?productId=$PRODUCT_ID" \
  -H "Authorization: $USER_TOKEN" \
  -H "Content-Type: application/json" 2>/dev/null | jq '.'

echo ""
echo "🔍 同时监控数据库变化："
mysql -u root -p031010Sra -e "
USE jingdezhen_tourism;
SELECT 
  id, 
  user_id, 
  product_id, 
  CASE deleted WHEN 0 THEN '✅收藏' WHEN 1 THEN '❌取消' END AS status,
  DATE_FORMAT(update_time, '%H:%i:%s') AS update_time
FROM favorite 
WHERE user_id = 1 AND product_id = $PRODUCT_ID
ORDER BY update_time DESC;
" 2>/dev/null

echo ""
echo "✅ 测试完成！"
echo "💡 如果看到状态在 ✅收藏 和 ❌取消 之间切换，说明功能正常"
