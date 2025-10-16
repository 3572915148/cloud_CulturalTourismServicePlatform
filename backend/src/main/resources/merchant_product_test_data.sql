-- ===================================
-- 商户和产品测试数据
-- ===================================

-- 设置字符编码为UTF-8，防止中文乱码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE jingdezhen_tourism;

-- ===================================
-- 0. 清空旧数据（防止重复导入）
-- ===================================
DELETE FROM product WHERE merchant_id IN (SELECT id FROM merchant WHERE username LIKE 'merchant%');
DELETE FROM merchant WHERE username LIKE 'merchant%';

-- 重置自增ID
ALTER TABLE merchant AUTO_INCREMENT = 1;
ALTER TABLE product AUTO_INCREMENT = 1;

-- ===================================
-- 1. 插入测试商户数据
-- ===================================
-- 注意：密码都是 "123456" 加密后的值
-- 使用BCrypt加密：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKzfO.J6

INSERT INTO merchant (username, password, shop_name, shop_logo, shop_introduction, contact_person, contact_phone, contact_email, address, audit_status, status)
VALUES 
-- 商户1：陶瓷旗舰店
('merchant001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKzfO.J6', 
 '景德镇陶瓷旗舰店', 
 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=400',
 '专注景德镇传统陶瓷工艺，提供高品质青花瓷、粉彩瓷等精品。三代传承，匠心制作，每一件都是艺术品。',
 '张大师', 
 '13800138001', 
 'ceramic001@jdz.com',
 '江西省景德镇市珠山区陶瓷大道188号',
 1, -- 已审核通过
 1),

-- 商户2：文旅酒店
('merchant002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKzfO.J6',
 '瓷都商务酒店',
 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=400',
 '位于景德镇市中心，交通便利。客房宽敞舒适，配备完善设施，是您商务出行和旅游度假的理想选择。',
 '李经理',
 '13800138002',
 'hotel002@jdz.com',
 '江西省景德镇市昌江区广场北路66号',
 1,
 1),

-- 商户3：特色餐厅
('merchant003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKzfO.J6',
 '瓷都风味餐厅',
 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400',
 '地道景德镇特色美食，传承百年烹饪技艺。主打饺子粑、碱水粑、冷粉等本地小吃，让您品味瓷都地道风味。',
 '王厨师',
 '13800138003',
 'food003@jdz.com',
 '江西省景德镇市珠山区中华北路128号',
 1,
 1),

-- 商户4：旅游公司
('merchant004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKzfO.J6',
 '景德镇文化旅行社',
 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400',
 '专业景德镇文化深度游，提供陶瓷博物馆、古窑遗址、大师工作室等精品线路。专业导游，品质保证。',
 '赵导',
 '13800138004',
 'travel004@jdz.com',
 '江西省景德镇市昌江区瓷都大道298号',
 1,
 1),

-- 商户5：体验工作室
('merchant005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKzfO.J6',
 '景瓷艺术体验馆',
 'https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=400',
 '提供陶瓷制作全流程体验，从拉坯、绘画到烧制，专业老师一对一指导。适合亲子活动、团建、陶艺爱好者。',
 '陈老师',
 '13800138005',
 'art005@jdz.com',
 '江西省景德镇市浮梁县三宝国际陶艺村',
 1,
 1);

-- ===================================
-- 2. 插入产品数据
-- ===================================

-- 商户1的产品（陶瓷旗舰店）
INSERT INTO product (merchant_id, category_id, title, description, cover_image, images, price, original_price, stock, sales, region, address, rating, tags, features, notice, status, recommend)
VALUES 
(1, 1, '青花瓷茶具套装', 
 '精选景德镇优质高岭土，手工拉坯成型，纯手工绘制青花图案。釉色青翠，画工精细，是品茶、收藏的上乘之选。',
 'https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=800',
 '["https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=800", "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800"]',
 388.00, 580.00, 50, 128, '珠山区', '江西省景德镇市珠山区陶瓷大道188号', 4.9,
 '["青花瓷", "茶具", "手工", "收藏级"]',
 '<ul><li>景德镇传统手工工艺</li><li>72道工序精心制作</li><li>釉下彩绘，永不褪色</li><li>配精美礼盒，适合送礼</li></ul>',
 '<p>1. 陶瓷易碎，请轻拿轻放</p><p>2. 首次使用建议用温水清洗</p><p>3. 不可用钢丝球清洁</p>',
 1, 1),

(1, 4, '粉彩花瓶摆件',
 '粉彩装饰技法，色彩柔和淡雅。器型端庄大方，绘有牡丹花卉图案，寓意富贵吉祥。适合家居装饰，彰显品味。',
 'https://images.unsplash.com/photo-1610725664285-7c57e6eeac3f?w=800',
 '["https://images.unsplash.com/photo-1610725664285-7c57e6eeac3f?w=800"]',
 268.00, 398.00, 30, 89, '珠山区', '江西省景德镇市珠山区陶瓷大道188号', 4.8,
 '["粉彩", "花瓶", "摆件", "装饰"]',
 '<ul><li>釉上彩绘工艺</li><li>色彩丰富细腻</li><li>高温二次烧制</li><li>大师监制作品</li></ul>',
 '<p>1. 摆放在平稳处，避免碰撞</p><p>2. 定期用软布擦拭</p>',
 1, 1);

-- 商户2的产品（瓷都商务酒店）
INSERT INTO product (merchant_id, category_id, title, description, cover_image, images, price, original_price, stock, sales, region, address, rating, tags, features, notice, status, recommend)
VALUES 
(2, 2, '豪华商务套房（双床）',
 '45平米豪华套房，双床配置，适合商务出差、家庭出游。配备独立办公区、舒适沙发、55寸智能电视，免费高速WiFi。',
 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800',
 '["https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800", "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800"]',
 458.00, 680.00, 10, 245, '昌江区', '江西省景德镇市昌江区广场北路66号', 4.7,
 '["酒店", "豪华套房", "双床", "商务"]',
 '<ul><li>免费早餐</li><li>免费停车位</li><li>健身房、游泳池</li><li>24小时热水</li><li>近市中心，交通便利</li></ul>',
 '<p>1. 入住时间：14:00后，退房时间：12:00前</p><p>2. 需提前1天预订</p><p>3. 不可带宠物</p>',
 1, 1),

(2, 2, '标准大床房',
 '28平米标准大床房，1.8米大床，干净整洁，性价比高。适合单人或情侣入住，配备基础设施齐全。',
 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800',
 '["https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800"]',
 228.00, 328.00, 20, 456, '昌江区', '江西省景德镇市昌江区广场北路66号', 4.6,
 '["酒店", "标准间", "大床", "经济"]',
 '<ul><li>免费WiFi</li><li>独立卫浴</li><li>24小时热水</li><li>每日清洁服务</li></ul>',
 '<p>1. 入住时间：14:00后</p><p>2. 当天预订需电话确认</p>',
 1, 0);

-- 商户3的产品（瓷都风味餐厅）
INSERT INTO product (merchant_id, category_id, title, description, cover_image, images, price, original_price, stock, sales, region, address, rating, tags, features, notice, status, recommend)
VALUES 
(3, 3, '景德镇特色套餐（2-3人）',
 '包含饺子粑、碱水粑、冷粉、瓷泥煨鸡、景德板鸡等8道地道景德镇美食。分量充足，适合2-3人享用。',
 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800',
 '["https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"]',
 168.00, 238.00, 999, 1245, '珠山区', '江西省景德镇市珠山区中华北路128号', 4.9,
 '["美食", "套餐", "特色", "地道"]',
 '<ul><li>精选本地食材</li><li>传统烹饪技艺</li><li>分量充足</li><li>包含主食、凉菜、热菜、汤</li></ul>',
 '<p>1. 建议提前2小时预订</p><p>2. 节假日需提前1天预订</p><p>3. 用餐时间2小时</p>',
 1, 1),

(3, 3, '景德镇小吃拼盘',
 '汇集景德镇最具特色的小吃：饺子粑、油条包麻糍、碱水粑、冷粉等。小份装，可以一次品尝多种美味。',
 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800',
 '["https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800"]',
 58.00, 88.00, 999, 876, '珠山区', '江西省景德镇市珠山区中华北路128号', 4.8,
 '["小吃", "拼盘", "特色", "地道"]',
 '<ul><li>8种特色小吃</li><li>现做现卖</li><li>适合下午茶</li></ul>',
 '<p>1. 堂食或打包</p><p>2. 营业时间：9:00-21:00</p>',
 1, 1);

-- 商户4的产品（景德镇文化旅行社）
INSERT INTO product (merchant_id, category_id, title, description, cover_image, images, price, original_price, stock, sales, region, address, rating, tags, features, notice, status, recommend)
VALUES 
(4, 1, '景德镇陶瓷文化一日游',
 '深度游览景德镇陶瓷文化。参观景德镇陶瓷博物馆、古窑民俗博览区、陶溪川文创街区。专业导游讲解，含中餐和门票。',
 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800',
 '["https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800"]',
 268.00, 388.00, 50, 567, '全市', '江西省景德镇市昌江区瓷都大道298号', 4.9,
 '["一日游", "陶瓷文化", "导游", "含餐"]',
 '<ul><li>专业导游全程陪同</li><li>含景区门票</li><li>含特色中餐</li><li>空调旅游大巴</li><li>赠送精美瓷器小礼品</li></ul>',
 '<p>1. 每天8:30出发，18:00返回</p><p>2. 需提前1天预订</p><p>3. 10人成团</p><p>4. 雨天照常出团</p>',
 1, 1),

(4, 1, '浮梁古县衙+瑶里古镇二日游',
 '探访千年古县城浮梁，游览保存完好的古县衙。第二天前往瑶里古镇，感受原生态的古村落风情。含住宿和餐食。',
 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800',
 '["https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"]',
 588.00, 788.00, 30, 234, '浮梁县', '江西省景德镇市昌江区瓷都大道298号', 4.8,
 '["二日游", "古镇", "文化", "含住宿"]',
 '<ul><li>含1晚三星酒店住宿</li><li>含2早餐、2正餐</li><li>含景区门票</li><li>专业导游</li></ul>',
 '<p>1. 周末团，周六8:00出发</p><p>2. 需提前3天预订</p><p>3. 15人成团</p>',
 1, 1);

-- 商户5的产品（景瓷艺术体验馆）
INSERT INTO product (merchant_id, category_id, title, description, cover_image, images, price, original_price, stock, sales, region, address, rating, tags, features, notice, status, recommend)
VALUES 
(5, 4, '陶瓷拉坯体验课程（2小时）',
 '专业陶艺老师一对一指导，体验传统拉坯技艺。从练泥、拉坯到修坯，全程体验。作品可烧制后邮寄（邮费自理）。',
 'https://images.unsplash.com/photo-1493106641515-6b5631de4bb9?w=800',
 '["https://images.unsplash.com/photo-1493106641515-6b5631de4bb9?w=800", "https://images.unsplash.com/photo-1452860606245-08befc0ff44b?w=800"]',
 188.00, 268.00, 20, 345, '浮梁县', '江西省景德镇市浮梁县三宝国际陶艺村', 5.0,
 '["体验", "拉坯", "陶艺", "亲子"]',
 '<ul><li>专业老师一对一指导</li><li>提供围裙等工具</li><li>可带走一件作品</li><li>提供拍照服务</li><li>适合亲子、情侣、团建</li></ul>',
 '<p>1. 需提前1天预订</p><p>2. 建议穿着轻便服装</p><p>3. 作品烧制需等待15天</p><p>4. 4岁以上儿童可参加</p>',
 1, 1),

(5, 4, '青花绘画体验课程（1.5小时）',
 '学习传统青花瓷绘画技法，在白坯盘子、碗或杯子上绘制自己喜欢的图案。老师提供图案参考和技法指导。',
 'https://images.unsplash.com/photo-1513519245088-0e12902e35ca?w=800',
 '["https://images.unsplash.com/photo-1513519245088-0e12902e35ca?w=800"]',
 158.00, 218.00, 30, 289, '浮梁县', '江西省景德镇市浮梁县三宝国际陶艺村', 4.9,
 '["体验", "青花", "绘画", "创意"]',
 '<ul><li>提供白坯和绘画工具</li><li>老师示范和指导</li><li>作品烧制后邮寄</li><li>零基础可学</li></ul>',
 '<p>1. 需提前预订</p><p>2. 作品烧制需10-15天</p><p>3. 8岁以上可参加</p>',
 1, 1);

-- 为其他商户再添加几个产品
INSERT INTO product (merchant_id, category_id, title, description, cover_image, price, stock, sales, region, address, rating, tags, status, recommend)
VALUES 
(1, 4, '景德镇手工陶瓷杯',
 '纯手工制作的陶瓷马克杯，釉色温润，手感舒适。适合日常使用，也可作为小礼品。',
 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=800',
 68.00, 100, 567, '珠山区', '江西省景德镇市珠山区陶瓷大道188号', 4.7,
 '["陶瓷杯", "手工", "实用", "礼品"]',
 1, 0),

(2, 2, '家庭亲子房',
 '家庭房配置，一张大床+一张儿童床，适合带小孩的家庭入住。提供儿童洗漱用品。',
 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800',
 328.00, 15, 189, '昌江区', '江西省景德镇市昌江区广场北路66号', 4.8,
 '["酒店", "家庭房", "亲子", "儿童"]',
 1, 0),

(3, 3, '瓷泥煨鸡（招牌菜）',
 '景德镇特色名菜，用陶瓷泥包裹土鸡，文火慢煨。肉质鲜嫩，香味浓郁。',
 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6?w=800',
 98.00, 999, 678, '珠山区', '江西省景德镇市珠山区中华北路128号', 4.9,
 '["招牌菜", "特色", "土鸡", "瓷泥煨"]',
 1, 1);

-- 验证插入的数据
SELECT 
    m.id AS merchant_id,
    m.shop_name,
    COUNT(p.id) AS product_count
FROM merchant m
LEFT JOIN product p ON m.id = p.merchant_id
GROUP BY m.id, m.shop_name
ORDER BY m.id;

