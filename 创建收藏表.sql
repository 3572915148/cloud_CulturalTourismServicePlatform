-- 创建收藏表
USE jingdezhen_tourism;

-- 如果表已存在则先删除
DROP TABLE IF EXISTS `favorite`;

-- 创建收藏表
CREATE TABLE `favorite`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT       NOT NULL COMMENT '产品ID',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    INDEX         idx_user_id (`user_id`),
    INDEX         idx_product_id (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

SELECT '收藏表创建成功！' AS message;

