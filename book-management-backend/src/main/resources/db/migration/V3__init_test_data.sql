INSERT INTO sys_user (id, username, password, real_name, phone, email, status, deleted) VALUES
    (2, 'librarian1', '$2b$12$htRSvC.jihWS1mW4eJjTg.A/9DeEoBgi6fS3RL6i4cHoe6Xgn52BC', '张管理', '13800000002', 'librarian1@bookms.local', 1, FALSE),
    (3, 'reader1', '$2b$12$htRSvC.jihWS1mW4eJjTg.A/9DeEoBgi6fS3RL6i4cHoe6Xgn52BC', '王同学', '13800000003', 'reader1@bookms.local', 1, FALSE),
    (4, 'reader2', '$2b$12$htRSvC.jihWS1mW4eJjTg.A/9DeEoBgi6fS3RL6i4cHoe6Xgn52BC', '李同学', '13800000004', 'reader2@bookms.local', 1, FALSE),
    (5, 'reader3', '$2b$12$htRSvC.jihWS1mW4eJjTg.A/9DeEoBgi6fS3RL6i4cHoe6Xgn52BC', '赵同学', '13800000005', 'reader3@bookms.local', 1, FALSE);

INSERT INTO sys_user_role (user_id, role_id) VALUES
    (2, 2),
    (3, 3),
    (4, 3),
    (5, 3);

INSERT INTO reader_info (id, user_id, reader_card_no, reader_type, max_borrow_count, current_borrow_count, credit_score, is_blacklist, valid_date_start, valid_date_end, deleted) VALUES
    (1, 3, 'RD20260001', 'student', 10, 2, 100, FALSE, DATE '2026-01-01', DATE '2029-12-31', FALSE),
    (2, 4, 'RD20260002', 'student', 10, 1, 100, FALSE, DATE '2026-01-01', DATE '2029-12-31', FALSE),
    (3, 5, 'RD20260003', 'teacher', 20, 0, 100, FALSE, DATE '2026-01-01', DATE '2029-12-31', FALSE);

INSERT INTO book_info (id, isbn, title, author, publisher, publish_date, category_id, cover_url, summary, price, total_stock, available_stock, status, location, deleted) VALUES
    (1, '978-7-111-40685-4', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', DATE '2024-01-01', 1, NULL, '计算机系统经典教材。', 128.00, 5, 4, 1, 'A-01-001', FALSE),
    (2, '978-7-115-52241-7', 'JavaScript高级程序设计', 'Matt Frisbie', '人民邮电出版社', DATE '2023-06-01', 1, NULL, '前端开发核心书籍。', 129.00, 3, 0, 1, 'A-01-002', FALSE),
    (3, '978-7-020-00220-1', '红楼梦', '曹雪芹', '人民文学出版社', DATE '2018-01-01', 10, NULL, '中国古典文学名著。', 59.00, 10, 9, 1, 'B-02-001', FALSE),
    (4, '978-7-111-55842-0', '算法导论', 'Thomas H. Cormen', '机械工业出版社', DATE '2022-08-01', 1, NULL, '算法领域经典教材。', 118.00, 4, 4, 1, 'A-01-004', FALSE),
    (5, '978-7-302-44648-5', '数据结构（C语言版）', '严蔚敏', '清华大学出版社', DATE '2021-03-01', 1, NULL, '数据结构基础教材。', 49.80, 6, 5, 1, 'A-01-005', FALSE),
    (6, '978-7-111-55843-7', '计算机网络：自顶向下方法', 'James F. Kurose', '机械工业出版社', DATE '2023-03-01', 1, NULL, '网络课程常用教材。', 89.00, 2, 1, 1, 'A-01-006', FALSE),
    (7, '978-7-121-36918-5', 'PostgreSQL实战', '张文', '电子工业出版社', DATE '2022-09-01', 9, NULL, 'PostgreSQL 数据库开发实践。', 79.00, 4, 4, 1, 'A-02-001', FALSE),
    (8, '978-7-5442-7225-0', '百年孤独', '加西亚·马尔克斯', '南海出版公司', DATE '2019-07-01', 10, NULL, '拉丁美洲魔幻现实主义名作。', 55.00, 5, 5, 1, 'B-02-002', FALSE),
    (9, '978-7-108-07040-2', '史记选读', '司马迁', '中华书局', DATE '2017-05-01', 11, NULL, '中国史学经典选读。', 68.00, 6, 6, 1, 'C-01-001', FALSE),
    (10, '978-7-5357-4482-8', '时间简史', 'Stephen Hawking', '湖南科学技术出版社', DATE '2020-10-01', 12, NULL, '科学普及读物。', 45.00, 7, 7, 1, 'D-01-001', FALSE),
    (11, '978-7-300-12150-6', '经济学原理', 'N. Gregory Mankiw', '中国人民大学出版社', DATE '2021-01-01', 5, NULL, '经济学入门经典。', 88.00, 5, 5, 1, 'E-01-001', FALSE),
    (12, '978-7-02-004249-4', '苏菲的世界', '乔斯坦·贾德', '人民文学出版社', DATE '2016-09-01', 6, NULL, '哲学启蒙读物。', 42.00, 4, 4, 1, 'F-01-001', FALSE),
    (13, '978-7-100-13025-8', '艺术的故事', 'E. H. 贡布里希', '商务印书馆', DATE '2015-11-01', 7, NULL, '艺术史经典导论。', 98.00, 4, 4, 1, 'G-01-001', FALSE),
    (14, '978-7-115-36425-3', '人月神话', 'Frederick P. Brooks Jr.', '人民邮电出版社', DATE '2021-06-01', 8, NULL, '软件工程经典。', 69.00, 3, 3, 1, 'A-03-001', FALSE),
    (15, '978-7-115-28118-5', '代码大全', 'Steve McConnell', '电子工业出版社', DATE '2020-04-01', 8, NULL, '软件构建实践指南。', 128.00, 5, 5, 1, 'A-03-002', FALSE),
    (16, '978-7-115-47718-2', '数据密集型应用系统设计', 'Martin Kleppmann', '电子工业出版社', DATE '2022-02-01', 9, NULL, '分布式系统设计经典。', 99.00, 4, 4, 1, 'A-02-002', FALSE),
    (17, '978-7-5086-6089-9', '人类简史', 'Yuval Noah Harari', '中信出版社', DATE '2018-08-01', 3, NULL, '全球畅销历史读物。', 68.00, 6, 6, 1, 'C-01-002', FALSE),
    (18, '978-7-5086-5823-0', '未来简史', 'Yuval Noah Harari', '中信出版社', DATE '2019-02-01', 3, NULL, '未来社会与科技思考。', 72.00, 5, 5, 1, 'C-01-003', FALSE),
    (19, '978-7-300-24465-6', '国富论', 'Adam Smith', '中国人民大学出版社', DATE '2017-03-01', 13, NULL, '经济学奠基之作。', 76.00, 4, 4, 1, 'E-02-001', FALSE),
    (20, '978-7-100-17046-9', '西方哲学史', '伯特兰·罗素', '商务印书馆', DATE '2018-12-01', 6, NULL, '西方哲学发展脉络。', 86.00, 5, 5, 1, 'F-01-002', FALSE);

INSERT INTO borrow_record (id, user_id, book_id, borrow_no, borrow_date, due_date, return_date, status, renew_count, fine_amount, approve_user_id, approve_time, reject_reason, remark, deleted) VALUES
    (1, 3, 1, 'B202603200001', DATE '2026-03-20', DATE '2026-04-29', NULL, 2, 0, 0, 2, TIMESTAMP '2026-03-20 10:00:00', NULL, '正常借阅', FALSE),
    (2, 3, 5, 'B202604010001', DATE '2026-04-01', DATE '2026-05-01', NULL, 2, 0, 0, 2, TIMESTAMP '2026-04-01 11:00:00', NULL, '正常借阅', FALSE),
    (3, 4, 3, 'B202602010001', DATE '2026-02-01', DATE '2026-03-01', NULL, 5, 0, 0, 2, TIMESTAMP '2026-02-01 09:30:00', NULL, '逾期未还', FALSE),
    (4, 5, 4, 'B202603010001', DATE '2026-03-01', DATE '2026-03-31', DATE '2026-03-28', 3, 0, 0, 2, TIMESTAMP '2026-03-01 08:30:00', NULL, '已归还', FALSE);

INSERT INTO reservation_record (id, user_id, book_id, status, reserve_date, expire_date, notify_at, deleted) VALUES
    (1, 3, 2, 0, DATE '2026-04-20', DATE '2026-04-27', NULL, FALSE);

INSERT INTO sys_notice (id, title, content, type, status, publisher_id, publish_time, deleted) VALUES
    (1, '图书馆开放时间调整通知', '自2026年4月起，图书馆工作日开放时间调整为 08:00-21:00，请合理安排到馆时间。', 1, 1, 1, TIMESTAMP '2026-04-10 09:00:00', FALSE),
    (2, '新书上架通知', '本月新增计算机、历史、经济类图书 20 余册，欢迎读者借阅。', 2, 1, 1, TIMESTAMP '2026-04-12 10:00:00', FALSE),
    (3, '系统维护通知', '定于本周六凌晨 01:00-03:00 进行系统维护，期间部分功能可能短暂不可用。', 3, 1, 1, TIMESTAMP '2026-04-15 15:00:00', FALSE);

INSERT INTO book_stock_log (id, book_id, change_type, change_qty, before_qty, after_qty, operator_id, related_borrow_id, remark) VALUES
    (1, 1, 'init', 4, 0, 4, 1, NULL, '初始化库存'),
    (2, 2, 'init', 0, 0, 0, 1, NULL, '初始化库存'),
    (3, 3, 'init', 9, 0, 9, 1, NULL, '初始化库存'),
    (4, 4, 'init', 4, 0, 4, 1, NULL, '初始化库存'),
    (5, 5, 'init', 5, 0, 5, 1, NULL, '初始化库存');

SELECT setval(pg_get_serial_sequence('sys_user', 'id'), (SELECT MAX(id) FROM sys_user), true);
SELECT setval(pg_get_serial_sequence('reader_info', 'id'), (SELECT MAX(id) FROM reader_info), true);
SELECT setval(pg_get_serial_sequence('book_info', 'id'), (SELECT MAX(id) FROM book_info), true);
SELECT setval(pg_get_serial_sequence('borrow_record', 'id'), (SELECT MAX(id) FROM borrow_record), true);
SELECT setval(pg_get_serial_sequence('reservation_record', 'id'), (SELECT MAX(id) FROM reservation_record), true);
SELECT setval(pg_get_serial_sequence('sys_notice', 'id'), (SELECT MAX(id) FROM sys_notice), true);
SELECT setval(pg_get_serial_sequence('book_stock_log', 'id'), (SELECT MAX(id) FROM book_stock_log), true);