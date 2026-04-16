-- =====================================================
-- 图书管理系统 - 初始数据库结构
-- V1__init_schema.sql
-- =====================================================

-- 启用 pg_trgm 扩展(模糊搜索)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =====================================================
-- 1. 用户权限相关表
-- =====================================================

-- 用户表
CREATE TABLE sys_user (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(200) NOT NULL,
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    avatar_url  VARCHAR(500),
    status      SMALLINT     NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_sys_user_username ON sys_user(username) WHERE deleted = FALSE;
CREATE INDEX idx_sys_user_phone ON sys_user(phone);
CREATE INDEX idx_sys_user_status ON sys_user(status);

-- 角色表
CREATE TABLE sys_role (
    id          BIGSERIAL PRIMARY KEY,
    role_code   VARCHAR(50)  NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_role_code ON sys_role(role_code) WHERE deleted = FALSE;

-- 权限表
CREATE TABLE sys_permission (
    id          BIGSERIAL PRIMARY KEY,
    perm_code   VARCHAR(100) NOT NULL,
    perm_name   VARCHAR(50)  NOT NULL,
    type        VARCHAR(20)  NOT NULL DEFAULT 'menu',
    parent_id   BIGINT       DEFAULT 0,
    sort_order  INT          NOT NULL DEFAULT 0,
    path        VARCHAR(200),
    icon        VARCHAR(100),
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_perm_code ON sys_permission(perm_code) WHERE deleted = FALSE;

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE(user_id, role_id)
);

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    id      BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    UNIQUE(role_id, perm_id)
);

-- =====================================================
-- 2. 图书相关表
-- =====================================================

-- 图书分类表
CREATE TABLE book_category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    parent_id   BIGINT       DEFAULT 0,
    sort_order  INT          NOT NULL DEFAULT 0,
    level       INT          NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_book_category_parent_id ON book_category(parent_id);

-- 图书信息表
CREATE TABLE book_info (
    id              BIGSERIAL PRIMARY KEY,
    isbn            VARCHAR(20),
    title           VARCHAR(200) NOT NULL,
    author          VARCHAR(100),
    publisher       VARCHAR(100),
    publish_date    DATE,
    category_id     BIGINT,
    cover_url       VARCHAR(500),
    summary         TEXT,
    price           NUMERIC(10,2),
    total_stock     INT          NOT NULL DEFAULT 0,
    available_stock INT          NOT NULL DEFAULT 0,
    status          SMALLINT     NOT NULL DEFAULT 1,
    location        VARCHAR(100),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_total_stock CHECK (total_stock >= 0),
    CONSTRAINT chk_available_stock CHECK (available_stock >= 0),
    CONSTRAINT chk_stock_relation CHECK (available_stock <= total_stock)
);
CREATE UNIQUE INDEX uk_book_isbn ON book_info(isbn) WHERE deleted = FALSE AND isbn IS NOT NULL;
CREATE INDEX idx_book_category_id ON book_info(category_id);
CREATE INDEX idx_book_status ON book_info(status);
CREATE INDEX idx_book_info_title_trgm ON book_info USING gin (title gin_trgm_ops);
CREATE INDEX idx_book_info_author_trgm ON book_info USING gin (author gin_trgm_ops);

-- =====================================================
-- 3. 借阅与预约相关表
-- =====================================================

-- 借阅记录表
CREATE TABLE borrow_record (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    book_id         BIGINT       NOT NULL,
    borrow_no       VARCHAR(20)  NOT NULL,
    borrow_date     DATE,
    due_date        DATE,
    return_date     DATE,
    status          SMALLINT     NOT NULL DEFAULT 0,
    renew_count     INT          NOT NULL DEFAULT 0,
    fine_amount     NUMERIC(10,2) NOT NULL DEFAULT 0,
    approve_user_id BIGINT,
    approve_time    TIMESTAMP,
    reject_reason   VARCHAR(500),
    remark          VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_borrow_no ON borrow_record(borrow_no);
CREATE INDEX idx_borrow_user_id ON borrow_record(user_id);
CREATE INDEX idx_borrow_book_id ON borrow_record(book_id);
CREATE INDEX idx_borrow_status ON borrow_record(status);
CREATE INDEX idx_borrow_due_date ON borrow_record(due_date);

-- 预约记录表
CREATE TABLE reservation_record (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    book_id         BIGINT       NOT NULL,
    status          SMALLINT     NOT NULL DEFAULT 0,
    reserve_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    expire_date     DATE,
    notify_at       TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_reservation_user_id ON reservation_record(user_id);
CREATE INDEX idx_reservation_book_id ON reservation_record(book_id);
CREATE INDEX idx_reservation_status ON reservation_record(status);
CREATE INDEX idx_reservation_queue ON reservation_record(book_id, status, reserve_date);

-- 库存变更日志表
CREATE TABLE book_stock_log (
    id                      BIGSERIAL PRIMARY KEY,
    book_id                 BIGINT       NOT NULL,
    change_type             VARCHAR(20)  NOT NULL,
    change_qty              INT          NOT NULL,
    before_qty              INT          NOT NULL,
    after_qty               INT          NOT NULL,
    operator_id             BIGINT,
    related_borrow_id       BIGINT,
    related_reservation_id  BIGINT,
    remark                  VARCHAR(500),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_stock_log_book_id ON book_stock_log(book_id);
CREATE INDEX idx_stock_log_created_at ON book_stock_log(created_at);

-- =====================================================
-- 4. 读者、公告、日志表
-- =====================================================

-- 读者信息表
CREATE TABLE reader_info (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    reader_card_no       VARCHAR(20),
    reader_type          VARCHAR(20)  NOT NULL DEFAULT 'general',
    max_borrow_count     INT          NOT NULL DEFAULT 5,
    current_borrow_count INT          NOT NULL DEFAULT 0,
    credit_score         INT          NOT NULL DEFAULT 100,
    is_blacklist         BOOLEAN      NOT NULL DEFAULT FALSE,
    valid_date_start     DATE,
    valid_date_end       DATE,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX uk_reader_user_id ON reader_info(user_id) WHERE deleted = FALSE;
CREATE UNIQUE INDEX uk_reader_card_no ON reader_info(reader_card_no) WHERE deleted = FALSE AND reader_card_no IS NOT NULL;

-- 公告表
CREATE TABLE sys_notice (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    content      TEXT,
    type         VARCHAR(20)  DEFAULT 'notice',
    status       SMALLINT     NOT NULL DEFAULT 1,
    publisher_id BIGINT,
    publish_time TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_notice_status_publish ON sys_notice(status, publish_time);

-- 操作日志表
CREATE TABLE sys_operation_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(50),
    operation   VARCHAR(200),
    method      VARCHAR(200),
    request_uri VARCHAR(500),
    params      TEXT,
    ip          VARCHAR(50),
    duration_ms BIGINT,
    status      SMALLINT     NOT NULL DEFAULT 1,
    error_msg   TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_op_log_user_id ON sys_operation_log(user_id);
CREATE INDEX idx_op_log_created_at ON sys_operation_log(created_at);

-- =====================================================
-- 5. 视图
-- =====================================================

-- 图书借阅统计视图
CREATE OR REPLACE VIEW v_book_borrow_stat AS
SELECT
    b.id AS book_id,
    b.title,
    b.isbn,
    COUNT(br.id) FILTER (WHERE br.status IN (2, 3, 5) AND br.deleted = FALSE) AS total_borrow_count,
    COUNT(br.id) FILTER (WHERE br.status = 2 AND br.deleted = FALSE) AS current_borrow_count
FROM book_info b
LEFT JOIN borrow_record br ON b.id = br.book_id
WHERE b.deleted = FALSE
GROUP BY b.id, b.title, b.isbn;

-- 用户借阅汇总视图
CREATE OR REPLACE VIEW v_user_borrow_summary AS
SELECT
    u.id AS user_id,
    u.username,
    u.real_name,
    COUNT(br.id) FILTER (WHERE br.deleted = FALSE) AS total_borrows,
    COUNT(br.id) FILTER (WHERE br.status = 2 AND br.deleted = FALSE) AS active_borrows,
    COUNT(br.id) FILTER (WHERE br.status = 5 AND br.deleted = FALSE) AS overdue_borrows
FROM sys_user u
LEFT JOIN borrow_record br ON u.id = br.user_id
WHERE u.deleted = FALSE
GROUP BY u.id, u.username, u.real_name;
