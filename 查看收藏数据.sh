#!/bin/bash

# 景德镇文旅平台 - 收藏数据查看脚本

echo "================================"
echo "收藏表实时数据监控"
echo "================================"
echo ""

# 查看所有收藏记录（包括已删除的）
echo "📊 所有收藏记录（包括已删除）："
mysql -u root -p031010Sra -e "
USE jingdezhen_tourism;
SELECT 
    id AS '收藏ID',
    user_id AS '用户ID',
    product_id AS '产品ID',
    CASE deleted 
        WHEN 0 THEN '✅ 已收藏'
        WHEN 1 THEN '❌ 已取消'
    END AS '状态',
    create_time AS '创建时间',
    update_time AS '更新时间'
FROM favorite 
ORDER BY update_time DESC
LIMIT 20;
" 2>/dev/null

echo ""
echo "================================"
echo "📈 统计信息："
echo "================================"

# 统计各状态的数量
mysql -u root -p031010Sra -e "
USE jingdezhen_tourism;
SELECT 
    '✅ 当前收藏中' AS '状态',
    COUNT(*) AS '数量'
FROM favorite 
WHERE deleted = 0

UNION ALL

SELECT 
    '❌ 已取消收藏' AS '状态',
    COUNT(*) AS '数量'
FROM favorite 
WHERE deleted = 1;
" 2>/dev/null

echo ""
echo "================================"
echo "💡 说明："
echo "- deleted=0 表示当前已收藏"
echo "- deleted=1 表示已取消收藏"
echo "- 同一用户对同一产品只有一条记录"
echo "- 收藏/取消会更新这条记录的状态"
echo "================================"

