INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
    (1, 'admin', '系统管理员', '拥有系统全部权限', 1),
    (2, 'librarian', '图书管理员', '负责图书、借阅、读者等管理', 1),
    (3, 'reader', '普通读者', '普通读者用户', 1);

INSERT INTO sys_permission (id, perm_code, perm_name, type, parent_id, sort_order, path, icon, status) VALUES
    (1, 'menu:dashboard', '仪表板', 'menu', NULL, 1, '/dashboard', 'Odometer', 1),
    (2, 'menu:books', '图书管理', 'menu', NULL, 2, '/books', 'Collection', 1),
    (3, 'menu:categories', '分类管理', 'menu', NULL, 3, '/categories', 'FolderOpened', 1),
    (4, 'menu:users', '用户管理', 'menu', NULL, 4, '/users', 'User', 1),
    (5, 'menu:borrows', '借阅管理', 'menu', NULL, 5, '/borrow', 'DocumentCopy', 1),
    (6, 'menu:reservations', '预约管理', 'menu', NULL, 6, '/reserve', 'Calendar', 1),
    (7, 'menu:readers', '读者管理', 'menu', NULL, 7, '/readers', 'Avatar', 1),
    (8, 'menu:notices', '公告管理', 'menu', NULL, 8, '/notices', 'Bell', 1),
    (9, 'book:create', '新增图书', 'button', 2, 1, NULL, NULL, 1),
    (10, 'book:update', '编辑图书', 'button', 2, 2, NULL, NULL, 1),
    (11, 'book:delete', '删除图书', 'button', 2, 3, NULL, NULL, 1),
    (12, 'book:stock', '调整库存', 'button', 2, 4, NULL, NULL, 1),
    (13, 'category:create', '新增分类', 'button', 3, 1, NULL, NULL, 1),
    (14, 'category:update', '编辑分类', 'button', 3, 2, NULL, NULL, 1),
    (15, 'category:delete', '删除分类', 'button', 3, 3, NULL, NULL, 1),
    (16, 'user:create', '创建用户', 'button', 4, 1, NULL, NULL, 1),
    (17, 'user:update', '编辑用户', 'button', 4, 2, NULL, NULL, 1),
    (18, 'user:delete', '删除用户', 'button', 4, 3, NULL, NULL, 1),
    (19, 'user:assign-role', '分配角色', 'button', 4, 4, NULL, NULL, 1),
    (20, 'borrow:approve', '审批借阅', 'button', 5, 1, NULL, NULL, 1),
    (21, 'borrow:reject', '拒绝借阅', 'button', 5, 2, NULL, NULL, 1),
    (22, 'borrow:return', '归还图书', 'button', 5, 3, NULL, NULL, 1),
    (23, 'borrow:renew', '续借图书', 'button', 5, 4, NULL, NULL, 1),
    (24, 'reader:create', '新增读者', 'button', 7, 1, NULL, NULL, 1),
    (25, 'reader:update', '编辑读者', 'button', 7, 2, NULL, NULL, 1),
    (26, 'reader:card', '办理借阅证', 'button', 7, 3, NULL, NULL, 1),
    (27, 'notice:create', '发布公告', 'button', 8, 1, NULL, NULL, 1),
    (28, 'notice:update', '编辑公告', 'button', 8, 2, NULL, NULL, 1),
    (29, 'notice:delete', '删除公告', 'button', 8, 3, NULL, NULL, 1),
    (30, 'stats:dashboard', '查看仪表板统计', 'api', 1, 9, '/api/stats/dashboard', NULL, 1);

INSERT INTO sys_user (id, username, password, real_name, phone, email, status, deleted)
VALUES (1, 'admin', '$2b$12$U6Q61P1ZA2Uvzep1LW9z.eg2rcD0KDOSTZMwTZGFhqrLM4XZ2jYpG', '系统管理员', '13800000001', 'admin@bookms.local', 1, FALSE);

INSERT INTO sys_user_role (user_id, role_id) VALUES
    (1, 1);

INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, id FROM sys_permission;

INSERT INTO sys_role_permission (role_id, perm_id) VALUES
    (2, 1), (2, 2), (2, 3), (2, 5), (2, 6), (2, 7), (2, 8),
    (2, 9), (2, 10), (2, 11), (2, 12),
    (2, 13), (2, 14), (2, 15),
    (2, 20), (2, 21), (2, 22), (2, 23),
    (2, 24), (2, 25), (2, 26),
    (2, 27), (2, 28), (2, 29),
    (2, 30),
    (3, 1), (3, 2), (3, 5), (3, 6), (3, 8);

INSERT INTO book_category (id, name, parent_id, sort_order, level, deleted) VALUES
    (1, '计算机', NULL, 1, 1, FALSE),
    (2, '文学', NULL, 2, 1, FALSE),
    (3, '历史', NULL, 3, 1, FALSE),
    (4, '科学', NULL, 4, 1, FALSE),
    (5, '经济', NULL, 5, 1, FALSE),
    (6, '哲学', NULL, 6, 1, FALSE),
    (7, '艺术', NULL, 7, 1, FALSE),
    (8, '程序设计', 1, 1, 2, FALSE),
    (9, '数据库', 1, 2, 2, FALSE),
    (10, '现代小说', 2, 1, 2, FALSE),
    (11, '中国史', 3, 1, 2, FALSE),
    (12, '自然科学', 4, 1, 2, FALSE),
    (13, '金融投资', 5, 1, 2, FALSE),
    (14, '绘画', 7, 1, 2, FALSE);

SELECT setval(pg_get_serial_sequence('sys_role', 'id'), (SELECT MAX(id) FROM sys_role), true);
SELECT setval(pg_get_serial_sequence('sys_permission', 'id'), (SELECT MAX(id) FROM sys_permission), true);
SELECT setval(pg_get_serial_sequence('sys_user', 'id'), (SELECT MAX(id) FROM sys_user), true);
SELECT setval(pg_get_serial_sequence('book_category', 'id'), (SELECT MAX(id) FROM book_category), true);