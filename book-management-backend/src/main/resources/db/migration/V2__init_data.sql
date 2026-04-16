-- =====================================================
-- 图书管理系统 - 初始数据
-- V2__init_data.sql
-- =====================================================

-- =====================================================
-- 1. 角色数据
-- =====================================================
INSERT INTO sys_role (role_code, role_name, description) VALUES
('admin',     '系统管理员', '系统最高权限，可管理用户、角色、系统配置'),
('librarian', '图书管理员', '管理图书、分类、借阅审批、归还登记'),
('reader',    '普通读者',   '查询图书、提交借阅、查看记录');

-- =====================================================
-- 2. 权限数据(菜单 + 按钮 + API)
-- =====================================================
-- 一级菜单
INSERT INTO sys_permission (id, perm_code, perm_name, type, parent_id, sort_order, path, icon) VALUES
(1,  'dashboard',       'Dashboard',    'menu', 0, 1,  '/dashboard',       'Odometer'),
(2,  'book_mgmt',       '图书管理',     'menu', 0, 2,  '',                 'Reading'),
(3,  'borrow_mgmt',     '借阅管理',     'menu', 0, 3,  '',                 'Document'),
(4,  'reader_mgmt',     '读者管理',     'menu', 0, 4,  '/reader',          'User'),
(5,  'notice_mgmt',     '公告管理',     'menu', 0, 5,  '/notice',          'Bell'),
(6,  'system_mgmt',     '系统管理',     'menu', 0, 6,  '',                 'Setting'),
(7,  'statistics',      '数据统计',     'menu', 0, 7,  '/statistics',      'DataAnalysis');

-- 二级菜单
INSERT INTO sys_permission (id, perm_code, perm_name, type, parent_id, sort_order, path, icon) VALUES
(10, 'book_list',       '图书列表',     'menu', 2, 1,  '/book',            ''),
(11, 'category_list',   '分类管理',     'menu', 2, 2,  '/category',        ''),
(12, 'borrow_list',     '借阅列表',     'menu', 3, 1,  '/borrow',          ''),
(13, 'reservation_list','预约列表',     'menu', 3, 2,  '/reservation',     ''),
(20, 'user_list',       '用户管理',     'menu', 6, 1,  '/user',            ''),
(21, 'role_list',       '角色管理',     'menu', 6, 2,  '/role',            ''),
(22, 'log_list',        '操作日志',     'menu', 6, 3,  '/log',             '');

-- 按钮/API权限
INSERT INTO sys_permission (id, perm_code, perm_name, type, parent_id, sort_order) VALUES
(100, 'book:create',    '新增图书',     'button', 10, 1),
(101, 'book:update',    '编辑图书',     'button', 10, 2),
(102, 'book:delete',    '删除图书',     'button', 10, 3),
(103, 'book:stock',     '调整库存',     'button', 10, 4),
(110, 'category:create','新增分类',     'button', 11, 1),
(111, 'category:update','编辑分类',     'button', 11, 2),
(112, 'category:delete','删除分类',     'button', 11, 3),
(120, 'borrow:approve', '审批借阅',     'button', 12, 1),
(121, 'borrow:reject',  '拒绝借阅',     'button', 12, 2),
(122, 'borrow:return',  '登记归还',     'button', 12, 3),
(130, 'user:create',    '新增用户',     'button', 20, 1),
(131, 'user:update',    '编辑用户',     'button', 20, 2),
(132, 'user:delete',    '删除用户',     'button', 20, 3),
(133, 'user:assign_role','分配角色',    'button', 20, 4),
(140, 'notice:create',  '发布公告',     'button', 5,  1),
(141, 'notice:update',  '编辑公告',     'button', 5,  2),
(142, 'notice:delete',  '删除公告',     'button', 5,  3);

-- 重置序列
SELECT setval('sys_permission_id_seq', 200);

-- =====================================================
-- 3. 角色-权限关联
-- =====================================================
-- admin 拥有所有权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, id FROM sys_permission;

-- librarian 拥有图书、借阅、读者、公告、统计相关权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 2, id FROM sys_permission WHERE perm_code IN (
    'dashboard', 'book_mgmt', 'borrow_mgmt', 'reader_mgmt', 'notice_mgmt', 'statistics',
    'book_list', 'category_list', 'borrow_list', 'reservation_list',
    'book:create', 'book:update', 'book:delete', 'book:stock',
    'category:create', 'category:update', 'category:delete',
    'borrow:approve', 'borrow:reject', 'borrow:return',
    'notice:create', 'notice:update', 'notice:delete'
);

-- reader 拥有 dashboard 和统计(受限)
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 3, id FROM sys_permission WHERE perm_code IN ('dashboard');

-- =====================================================
-- 4. 用户数据 (密码使用BCrypt加密)
-- =====================================================
-- admin/admin123
INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES
(1, 'admin', '$2b$10$2UxeWg80ZFa.s6xNGRTNt.cLmOeSFXROCc55pc3/HHrIb7Tivd/rC', '系统管理员', '13800000001', 'admin@bookms.com', 1);
-- librarian/lib123
INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES
(2, 'librarian', '$2b$10$pgkn9X3T6Ni2xlcrIE6/EerCKE3Idy3IeSiMwdO2he/8KmTtppGTy', '图书管理员', '13800000002', 'lib@bookms.com', 1);
-- reader01/reader123
INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES
(3, 'reader01', '$2b$10$XSLJZiSmBtpdq74yPlbcBeeUS3.D/lVHz9YcjTZp52ZsDd/grupfW', '测试读者', '13800000003', 'reader@bookms.com', 1);

SELECT setval('sys_user_id_seq', 100);

-- =====================================================
-- 5. 用户-角色关联
-- =====================================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> 系统管理员
(2, 2),  -- librarian -> 图书管理员
(3, 3);  -- reader01 -> 普通读者

-- =====================================================
-- 6. 读者信息
-- =====================================================
INSERT INTO reader_info (user_id, reader_card_no, reader_type, max_borrow_count, current_borrow_count, credit_score, valid_date_start, valid_date_end) VALUES
(3, 'RD20260001', 'general', 5, 0, 100, '2026-01-01', '2029-12-31');

-- =====================================================
-- 7. 图书分类
-- =====================================================
INSERT INTO book_category (id, name, parent_id, sort_order, level) VALUES
(1, '计算机科学',  0, 1, 1),
(2, '文学',        0, 2, 1),
(3, '历史',        0, 3, 1),
(4, '经济管理',    0, 4, 1),
(5, '自然科学',    0, 5, 1),
(6, '编程语言',    1, 1, 2),
(7, '数据库',      1, 2, 2),
(8, '人工智能',    1, 3, 2),
(9, '中国文学',    2, 1, 2),
(10,'外国文学',    2, 2, 2);

SELECT setval('book_category_id_seq', 100);

-- =====================================================
-- 8. 测试图书数据
-- =====================================================
INSERT INTO book_info (isbn, title, author, publisher, publish_date, category_id, summary, price, total_stock, available_stock, status, location) VALUES
('978-7-111-42776-3', 'Java核心技术 卷I',      'Cay S. Horstmann',   '机械工业出版社', '2023-01-01', 6, 'Java编程经典教材，全面讲解Java核心语言特性和标准库。', 149.00, 10, 10, 1, 'A区-01架-01层'),
('978-7-111-57788-0', '深入理解Java虚拟机',     '周志明',             '机械工业出版社', '2022-06-01', 6, '全面讲解JVM原理、内存管理、并发编程。', 129.00, 5, 5, 1, 'A区-01架-02层'),
('978-7-115-52815-5', 'Python编程：从入门到实践', 'Eric Matthes',      '人民邮电出版社', '2023-03-01', 6, 'Python入门经典教材，项目驱动学习。', 89.90, 8, 8, 1, 'A区-02架-01层'),
('978-7-111-63627-2', 'Spring Boot实战',         'Craig Walls',       '机械工业出版社', '2023-05-01', 6, 'Spring Boot权威指南，快速构建微服务。', 79.00, 3, 3, 1, 'A区-02架-02层'),
('978-7-115-51368-7', 'Vue.js设计与实现',        '霍春阳',            '人民邮电出版社', '2023-02-01', 6, '深入解析Vue.js 3框架设计原理。', 109.00, 6, 6, 1, 'A区-03架-01层'),
('978-7-111-40701-7', '数据库系统概论',          '王珊',               '高等教育出版社', '2022-01-01', 7, '数据库经典教材，全面介绍数据库理论与应用。', 45.00, 7, 7, 1, 'B区-01架-01层'),
('978-7-302-46289-7', '机器学习',                '周志华',            '清华大学出版社', '2022-01-01', 8, '西瓜书，机器学习入门经典。', 88.00, 4, 4, 1, 'B区-02架-01层'),
('978-7-020-00220-1', '红楼梦',                  '曹雪芹',            '人民文学出版社', '2020-01-01', 9, '中国古典文学四大名著之一。', 59.70, 5, 5, 1, 'C区-01架-01层'),
('978-7-020-00221-8', '三国演义',                '罗贯中',            '人民文学出版社', '2020-01-01', 9, '中国古典文学四大名著之一。', 39.50, 4, 4, 1, 'C区-01架-02层'),
('978-7-544-27051-0', '百年孤独',                '加西亚·马尔克斯',    '南海出版公司',   '2021-06-01', 10,'魔幻现实主义文学的代表作。', 55.00, 3, 3, 1, 'C区-02架-01层'),
('978-7-111-99999-0', '高并发系统设计',          '李运华',            '机械工业出版社', '2023-09-01', 6, '讲解高并发系统架构设计核心技术。', 99.00, 1, 0, 1, 'A区-04架-01层'),
('978-7-111-99998-3', '算法导论',                'Thomas H. Cormen',  '机械工业出版社', '2022-12-01', 6, '算法领域经典教材。', 128.00, 2, 2, 0, 'A区-05架-01层');

-- =====================================================
-- 9. 公告数据
-- =====================================================
INSERT INTO sys_notice (title, content, type, status, publisher_id, publish_time) VALUES
('欢迎使用图书管理系统', '图书管理系统已上线，欢迎广大读者使用。请通过微信小程序搜索图书、提交借阅申请。', 'notice', 1, 1, NOW()),
('五一假期借阅通知', '五一假期期间（5月1日-5月5日）借阅服务暂停，4月30日前请归还到期图书。', 'notice', 1, 1, NOW()),
('新书推荐：2026年计算机类热门书单', '本月新到一批计算机类技术书籍，包括AI、云原生、微服务等方向，欢迎借阅。', 'recommend', 1, 1, NOW());
