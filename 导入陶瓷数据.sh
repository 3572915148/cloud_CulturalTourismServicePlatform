#!/bin/bash

# 导入陶瓷文化测试数据
echo "正在导入陶瓷文化测试数据..."

mysql -u root -p031010Sra --default-character-set=utf8mb4 jingdezhen_tourism < backend/src/main/resources/ceramic_content_test_data.sql

if [ $? -eq 0 ]; then
    echo "✅ 数据导入成功！"
    echo ""
    echo "验证数据："
    mysql -u root -p031010Sra --default-character-set=utf8mb4 -e "USE jingdezhen_tourism; SELECT id, category, title, author, views FROM ceramic_content WHERE status=1 ORDER BY sort_order LIMIT 5;"
else
    echo "❌ 数据导入失败，请检查数据库连接"
fi

