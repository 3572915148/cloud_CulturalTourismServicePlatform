-- 测试收藏恢复功能
-- 模拟toggleFavorite API中的restoreFavorite操作

USE jingdezhen_tourism;

-- 查看当前状态
SELECT '当前状态' AS step, id, user_id, product_id, deleted, update_time FROM favorite WHERE user_id = 1 AND product_id = 6;

-- 执行恢复操作（模拟restoreFavorite方法）
UPDATE favorite SET deleted = 0, update_time = NOW() WHERE id = 4;

-- 查看恢复后的状态
SELECT '恢复后状态' AS step, id, user_id, product_id, deleted, update_time FROM favorite WHERE user_id = 1 AND product_id = 6;

-- 再次执行删除操作（模拟deleteById方法）
UPDATE favorite SET deleted = 1, update_time = NOW() WHERE id = 4;

-- 查看删除后的状态
SELECT '删除后状态' AS step, id, user_id, product_id, deleted, update_time FROM favorite WHERE user_id = 1 AND product_id = 6;
