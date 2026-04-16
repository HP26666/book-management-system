-- 补充 sys_user 缺少的 openid 字段
ALTER TABLE sys_user ADD COLUMN openid VARCHAR(100);
CREATE INDEX idx_sys_user_openid ON sys_user(openid);
