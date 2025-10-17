#!/bin/bash

# 实时监控收藏数据变化

echo "🔍 开始实时监控收藏数据变化..."
echo "请在浏览器中操作收藏功能，我会每3秒刷新一次数据"
echo "按 Ctrl+C 停止监控"
echo ""

while true; do
    clear
    echo "================================"
    echo "📊 收藏数据实时监控 - $(date '+%H:%M:%S')"
    echo "================================"
    
    mysql -u root -p031010Sra -e "
    USE jingdezhen_tourism;
    SELECT 
        id AS 'ID',
        user_id AS '用户',
        product_id AS '产品',
        CASE deleted 
            WHEN 0 THEN '✅收藏'
            WHEN 1 THEN '❌取消'
        END AS '状态',
        DATE_FORMAT(create_time, '%H:%i:%s') AS '创建',
        DATE_FORMAT(update_time, '%H:%i:%s') AS '更新'
    FROM favorite 
    ORDER BY update_time DESC
    LIMIT 10;
    " 2>/dev/null
    
    echo ""
    echo "💡 提示："
    echo "- 收藏操作会显示 ✅收藏"
    echo "- 取消操作会显示 ❌取消" 
    echo "- 更新时间会变化"
    echo ""
    echo "⏰ 下次刷新: 3秒后..."
    
    sleep 3
done
