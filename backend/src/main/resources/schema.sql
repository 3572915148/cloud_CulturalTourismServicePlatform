-- ===================================
-- 景德镇文旅服务平台数据库设计
-- ===================================

CREATE
DATABASE IF NOT EXISTS jingdezhen_tourism DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
jingdezhen_tourism;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
CREATE TABLE `user`
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username`     VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`     VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `nickname`     VARCHAR(50) COMMENT '昵称',
    `email`        VARCHAR(100) COMMENT '邮箱',
    `phone`        VARCHAR(20) COMMENT '手机号',
    `avatar`       VARCHAR(255) COMMENT '头像URL',
    `gender`       TINYINT  DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday`     DATE COMMENT '生日',
    `introduction` TEXT COMMENT '个人简介',
    `status`       TINYINT  DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常',
    `deleted`      TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX          idx_username (`username`),
    INDEX          idx_phone (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 商户表
-- ----------------------------
CREATE TABLE `merchant`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商户ID',
    `username`          VARCHAR(50)  NOT NULL UNIQUE COMMENT '商户账号',
    `password`          VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `shop_name`         VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `shop_logo`         VARCHAR(255) COMMENT '店铺Logo',
    `shop_introduction` TEXT COMMENT '店铺介绍',
    `contact_person`    VARCHAR(50) COMMENT '联系人',
    `contact_phone`     VARCHAR(20) COMMENT '联系电话',
    `contact_email`     VARCHAR(100) COMMENT '联系邮箱',
    `address`           VARCHAR(255) COMMENT '店铺地址',
    `business_license`  VARCHAR(255) COMMENT '营业执照URL',
    `id_card_front`     VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back`      VARCHAR(255) COMMENT '身份证背面URL',
    `audit_status`      TINYINT  DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `audit_remark`      VARCHAR(255) COMMENT '审核备注',
    `status`            TINYINT  DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常',
    `deleted`           TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX               idx_shop_name (`shop_name`),
    INDEX               idx_audit_status (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

-- ----------------------------
-- 3. 产品分类表
-- ----------------------------
CREATE TABLE `product_category`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id`   BIGINT   DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `sort_order`  INT      DEFAULT 0 COMMENT '排序',
    `icon`        VARCHAR(255) COMMENT '图标',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX         idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- ----------------------------
-- 4. 产品表（包括景点、酒店、餐厅等）
-- ----------------------------
CREATE TABLE `product`
(
    `id`             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    `merchant_id`    BIGINT       NOT NULL COMMENT '商户ID',
    `category_id`    BIGINT       NOT NULL COMMENT '分类ID',
    `title`          VARCHAR(200) NOT NULL COMMENT '产品标题',
    `description`    TEXT COMMENT '产品描述',
    `cover_image`    VARCHAR(255) COMMENT '封面图片',
    `images`         TEXT COMMENT '产品图片（JSON数组）',
    `price`          DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格',
    `original_price` DECIMAL(10, 2) COMMENT '原价',
    `stock`          INT            DEFAULT 0 COMMENT '库存',
    `sales`          INT            DEFAULT 0 COMMENT '销量',
    `region`         VARCHAR(100) COMMENT '所在区域',
    `address`        VARCHAR(255) COMMENT '详细地址',
    `latitude`       DECIMAL(10, 7) COMMENT '纬度',
    `longitude`      DECIMAL(10, 7) COMMENT '经度',
    `rating`         DECIMAL(2, 1)  DEFAULT 5.0 COMMENT '评分',
    `tags`           VARCHAR(255) COMMENT '标签（JSON数组）',
    `features`       TEXT COMMENT '产品特色',
    `notice`         TEXT COMMENT '预订须知',
    `status`         TINYINT        DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `recommend`      TINYINT        DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    `deleted`        TINYINT        DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`    DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX            idx_merchant_id (`merchant_id`),
    INDEX            idx_category_id (`category_id`),
    INDEX            idx_region (`region`),
    INDEX            idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- ----------------------------
-- 5. 订单表
-- ----------------------------
CREATE TABLE `orders`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no`      VARCHAR(32)    NOT NULL UNIQUE COMMENT '订单号',
    `user_id`       BIGINT         NOT NULL COMMENT '用户ID',
    `merchant_id`   BIGINT         NOT NULL COMMENT '商户ID',
    `product_id`    BIGINT         NOT NULL COMMENT '产品ID',
    `product_title` VARCHAR(200) COMMENT '产品标题快照',
    `product_image` VARCHAR(255) COMMENT '产品图片快照',
    `price`         DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `quantity`      INT      DEFAULT 1 COMMENT '数量',
    `total_amount`  DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    `booking_date`  DATE COMMENT '预订日期',
    `booking_time`  VARCHAR(50) COMMENT '预订时间段',
    `contact_name`  VARCHAR(50) COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `remark`        TEXT COMMENT '备注',
    `status`        TINYINT  DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已完成，3-已取消，4-已退款',
    `pay_time`      DATETIME COMMENT '支付时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `cancel_time`   DATETIME COMMENT '取消时间',
    `deleted`       TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX           idx_order_no (`order_no`),
    INDEX           idx_user_id (`user_id`),
    INDEX           idx_merchant_id (`merchant_id`),
    INDEX           idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 6. 评价表
-- ----------------------------
CREATE TABLE `review`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `order_id`      BIGINT  NOT NULL COMMENT '订单ID',
    `user_id`       BIGINT  NOT NULL COMMENT '用户ID',
    `product_id`    BIGINT  NOT NULL COMMENT '产品ID',
    `merchant_id`   BIGINT  NOT NULL COMMENT '商户ID',
    `rating`        TINYINT NOT NULL COMMENT '评分：1-5星',
    `content`       TEXT COMMENT '评价内容',
    `images`        TEXT COMMENT '评价图片（JSON数组）',
    `reply_content` TEXT COMMENT '商户回复内容',
    `reply_time`    DATETIME COMMENT '回复时间',
    `status`        TINYINT  DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
    `deleted`       TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX           idx_order_id (`order_id`),
    INDEX           idx_user_id (`user_id`),
    INDEX           idx_product_id (`product_id`),
    INDEX           idx_merchant_id (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ----------------------------
-- 7. 反馈表
-- ----------------------------
CREATE TABLE `feedback`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    `user_id`     BIGINT COMMENT '用户ID（可为空，允许匿名反馈）',
    `type`        TINYINT  DEFAULT 1 COMMENT '反馈类型：1-功能建议，2-问题反馈，3-投诉',
    `content`     TEXT NOT NULL COMMENT '反馈内容',
    `contact`     VARCHAR(100) COMMENT '联系方式',
    `images`      TEXT COMMENT '反馈图片（JSON数组）',
    `status`      TINYINT  DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理中，2-已处理',
    `reply`       TEXT COMMENT '回复内容',
    `reply_time`  DATETIME COMMENT '回复时间',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX         idx_user_id (`user_id`),
    INDEX         idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反馈表';

-- ----------------------------
-- 8. 陶瓷文化内容表
-- ----------------------------
CREATE TABLE `ceramic_content`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内容ID',
    `category`    VARCHAR(50) COMMENT '分类：历史、工艺、名家、作品等',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `summary`     TEXT COMMENT '摘要',
    `content`     LONGTEXT COMMENT '内容（富文本）',
    `cover_image` VARCHAR(255) COMMENT '封面图',
    `images`      TEXT COMMENT '相关图片（JSON数组）',
    `video_url`   VARCHAR(255) COMMENT '视频链接',
    `author`      VARCHAR(50) COMMENT '作者',
    `views`       INT      DEFAULT 0 COMMENT '浏览量',
    `likes`       INT      DEFAULT 0 COMMENT '点赞数',
    `sort_order`  INT      DEFAULT 0 COMMENT '排序',
    `status`      TINYINT  DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX         idx_category (`category`),
    INDEX         idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='陶瓷文化内容表';

-- ----------------------------
-- 9. AI推荐记录表
-- ----------------------------
CREATE TABLE `ai_recommendation`
(
    `id`                   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id`              BIGINT COMMENT '用户ID',
    `query`                TEXT NOT NULL COMMENT '用户查询内容',
    `context`              TEXT COMMENT '上下文信息',
    `response`             TEXT COMMENT 'AI响应内容',
    `recommended_products` TEXT COMMENT '推荐的产品ID列表（JSON数组）',
    `feedback`             TINYINT COMMENT '用户反馈：1-有帮助，0-没帮助',
    `deleted`              TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX                  idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐记录表';

-- ----------------------------
-- 10. 系统管理员表
-- ----------------------------
CREATE TABLE `admin`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    `username`    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `nickname`    VARCHAR(50) COMMENT '昵称',
    `email`       VARCHAR(100) COMMENT '邮箱',
    `avatar`      VARCHAR(255) COMMENT '头像',
    `role`        VARCHAR(50) DEFAULT 'ADMIN' COMMENT '角色',
    `status`      TINYINT     DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `deleted`     TINYINT     DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX         idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------

-- 插入产品分类
INSERT INTO `product_category` (`name`, `parent_id`, `sort_order`, `icon`)
VALUES ('景点门票', 0, 1, 'scenic'),
       ('酒店住宿', 0, 2, 'hotel'),
       ('特色餐饮', 0, 3, 'restaurant'),
       ('文化体验', 0, 4, 'culture'),
       ('陶瓷购物', 0, 5, 'shopping');

-- ----------------------------
-- 9. 收藏表
-- ----------------------------
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

-- 插入管理员账号（密码：admin123，需要加密）
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN');

