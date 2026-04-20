CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_sys_user_username ON sys_user(username) WHERE deleted = FALSE;
CREATE INDEX idx_sys_user_phone ON sys_user(phone);
CREATE INDEX idx_sys_user_status ON sys_user(status);

CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_sys_user_role_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_sys_user_role_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    parent_id BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    path VARCHAR(255),
    icon VARCHAR(100),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, perm_id),
    CONSTRAINT fk_role_perm_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_role_perm_perm_id FOREIGN KEY (perm_id) REFERENCES sys_permission(id)
);

CREATE TABLE book_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_book_category_parent_id FOREIGN KEY (parent_id) REFERENCES book_category(id)
);

CREATE INDEX idx_book_category_parent_id ON book_category(parent_id);

CREATE TABLE book_info (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20),
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    publisher VARCHAR(100),
    publish_date DATE,
    category_id BIGINT,
    cover_url VARCHAR(500),
    summary TEXT,
    price NUMERIC(10,2),
    total_stock INT NOT NULL DEFAULT 0,
    available_stock INT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    location VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_book_info_category_id FOREIGN KEY (category_id) REFERENCES book_category(id),
    CONSTRAINT ck_book_stock_non_negative CHECK (total_stock >= 0 AND available_stock >= 0),
    CONSTRAINT ck_book_available_not_exceed_total CHECK (available_stock <= total_stock)
);

CREATE UNIQUE INDEX uk_book_info_isbn ON book_info(isbn) WHERE isbn IS NOT NULL AND deleted = FALSE;
CREATE INDEX idx_book_info_category_id ON book_info(category_id);
CREATE INDEX idx_book_info_status ON book_info(status);
CREATE INDEX idx_book_info_title_trgm ON book_info USING gin(title gin_trgm_ops);
CREATE INDEX idx_book_info_author_trgm ON book_info USING gin(author gin_trgm_ops);

CREATE TABLE borrow_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_no VARCHAR(32) NOT NULL UNIQUE,
    borrow_date DATE,
    due_date DATE,
    return_date DATE,
    status SMALLINT NOT NULL DEFAULT 0,
    renew_count INT NOT NULL DEFAULT 0,
    fine_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    approve_user_id BIGINT,
    approve_time TIMESTAMP,
    reject_reason VARCHAR(500),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_borrow_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_borrow_book_id FOREIGN KEY (book_id) REFERENCES book_info(id),
    CONSTRAINT fk_borrow_approve_user_id FOREIGN KEY (approve_user_id) REFERENCES sys_user(id)
);

CREATE INDEX idx_borrow_user_id ON borrow_record(user_id);
CREATE INDEX idx_borrow_book_id ON borrow_record(book_id);
CREATE INDEX idx_borrow_status ON borrow_record(status);
CREATE INDEX idx_borrow_due_date ON borrow_record(due_date);

CREATE TABLE reservation_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    reserve_date DATE NOT NULL,
    expire_date DATE NOT NULL,
    notify_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reservation_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_reservation_book_id FOREIGN KEY (book_id) REFERENCES book_info(id)
);

CREATE INDEX idx_reservation_user_id ON reservation_record(user_id);
CREATE INDEX idx_reservation_book_id ON reservation_record(book_id);
CREATE INDEX idx_reservation_status ON reservation_record(status);
CREATE INDEX idx_reservation_queue ON reservation_record(book_id, status, reserve_date);

CREATE TABLE book_stock_log (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    change_qty INT NOT NULL,
    before_qty INT NOT NULL,
    after_qty INT NOT NULL,
    operator_id BIGINT,
    related_borrow_id BIGINT,
    related_reservation_id BIGINT,
    remark VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_log_book_id FOREIGN KEY (book_id) REFERENCES book_info(id),
    CONSTRAINT fk_stock_log_operator_id FOREIGN KEY (operator_id) REFERENCES sys_user(id)
);

CREATE INDEX idx_stock_log_book_id ON book_stock_log(book_id);
CREATE INDEX idx_stock_log_created_at ON book_stock_log(created_at);

CREATE TABLE reader_info (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    reader_card_no VARCHAR(32) UNIQUE,
    reader_type VARCHAR(20) NOT NULL DEFAULT 'general',
    max_borrow_count INT NOT NULL DEFAULT 5,
    current_borrow_count INT NOT NULL DEFAULT 0,
    credit_score INT NOT NULL DEFAULT 100,
    is_blacklist BOOLEAN NOT NULL DEFAULT FALSE,
    valid_date_start DATE,
    valid_date_end DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reader_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE INDEX idx_reader_card_no ON reader_info(reader_card_no);

CREATE TABLE sys_notice (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    type SMALLINT NOT NULL DEFAULT 1,
    status SMALLINT NOT NULL DEFAULT 1,
    publisher_id BIGINT,
    publish_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notice_publisher_id FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
);

CREATE INDEX idx_notice_status_publish_time ON sys_notice(status, publish_time);

CREATE TABLE sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(255),
    request_uri VARCHAR(255),
    params TEXT,
    ip VARCHAR(50),
    duration_ms BIGINT,
    status SMALLINT NOT NULL DEFAULT 1,
    error_msg TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_operation_log_user_id ON sys_operation_log(user_id);
CREATE INDEX idx_operation_log_created_at ON sys_operation_log(created_at);

CREATE VIEW v_book_borrow_stat AS
SELECT
    b.id AS book_id,
    b.title,
    b.isbn,
    COUNT(br.id) AS total_borrow_count,
    SUM(CASE WHEN br.status = 2 THEN 1 ELSE 0 END) AS current_borrow_count
FROM book_info b
LEFT JOIN borrow_record br ON b.id = br.book_id AND br.deleted = FALSE
WHERE b.deleted = FALSE
GROUP BY b.id, b.title, b.isbn;

CREATE VIEW v_user_borrow_summary AS
SELECT
    u.id AS user_id,
    u.username,
    u.real_name,
    COUNT(br.id) AS total_borrows,
    SUM(CASE WHEN br.status = 2 THEN 1 ELSE 0 END) AS active_borrows,
    SUM(CASE WHEN br.status = 5 THEN 1 ELSE 0 END) AS overdue_borrows
FROM sys_user u
LEFT JOIN borrow_record br ON u.id = br.user_id AND br.deleted = FALSE
WHERE u.deleted = FALSE
GROUP BY u.id, u.username, u.real_name;