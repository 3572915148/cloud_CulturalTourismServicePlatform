#!/bin/bash

# ===================================
# 导入商户和产品测试数据脚本
# ===================================

echo "======================================"
echo "  景德镇文化旅游平台 - 数据导入工具"
echo "======================================"
echo ""

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="jingdezhen_tourism"
DB_USER="root"
DB_PASSWORD="031010Sra"

# SQL文件路径
SQL_FILE="backend/src/main/resources/merchant_product_test_data.sql"

echo "开始导入商户和产品测试数据..."
echo "数据库: $DB_NAME"
echo "SQL文件: $SQL_FILE"
echo ""

# 执行导入（使用UTF-8编码）
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 数据导入成功！"
    echo ""
    echo "已导入："
    echo "  - 5个测试商户（密码均为: 123456）"
    echo "  - 15个测试产品"
    echo ""
    echo "商户账号："
    echo "  merchant001 - 景德镇陶瓷旗舰店"
    echo "  merchant002 - 瓷都商务酒店"
    echo "  merchant003 - 瓷都风味餐厅"
    echo "  merchant004 - 景德镇文化旅行社"
    echo "  merchant005 - 景瓷艺术体验馆"
    echo ""
    echo "可以使用这些账号登录商户端进行测试！"
else
    echo ""
    echo "❌ 数据导入失败，请检查："
    echo "  1. MySQL是否正在运行"
    echo "  2. 数据库连接信息是否正确"
    echo "  3. SQL文件路径是否正确"
fi

echo ""
echo "======================================"

