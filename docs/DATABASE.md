# 数据库文档

## 1. 环境说明

| 项目 | 内容 |
|------|------|
| 数据库 | PostgreSQL 15.x |
| 数据库名 | `book_management` |
| 字符集 | UTF-8 |
| 时区 | `Asia/Shanghai` |
| 迁移工具 | Flyway（已启用） |
| 迁移路径 | `book-management-backend/src/main/resources/db/migration/` |

## 2. 命名规范

| 对象 | 规则 | 示例 |
|------|------|------|
| 表名 | 小写下划线 | `sys_user`, `book_info` |
| 字段 | 小写下划线 | `created_at`, `user_id` |
| 索引 | `idx_表名_字段名` | `idx_book_info_isbn` |
| 唯一索引 | `uk_表名_字段名` | `uk_sys_user_username` |
| 外键 | `fk_表名_字段名` | `fk_book_info_category_id` |

通用字段：
- `id` — `BIGSERIAL PRIMARY KEY`
- `created_at` — 创建时间
- `updated_at` — 更新时间
- `deleted` — 逻辑删除标记（`BOOLEAN`）

## 3. 核心实体关系

```text
[sys_user] N -- M [sys_role] N -- M [sys_permission]
[sys_user] 1 -- 1 [reader_info]
[sys_user] 1 -- N [borrow_record]
[sys_user] 1 -- N [reservation_record]
[book_category] 1 -- N [book_info]
[book_info] 1 -- N [borrow_record]
[book_info] 1 -- N [reservation_record]
[book_info] 1 -- N [book_stock_log]
```

## 4. 核心表说明

### 4.1 用户权限

| 表名 | 说明 |
|------|------|
| `sys_user` | 系统用户（管理员、图书管理员、读者） |
| `sys_role` | 角色定义（`admin`、`librarian`、`reader`） |
| `sys_permission` | 权限定义（菜单、按钮、接口） |
| `sys_user_role` | 用户-角色关联 |
| `sys_role_permission` | 角色-权限关联 |

### 4.2 图书与分类

| 表名 | 说明 |
|------|------|
| `book_category` | 图书分类（邻接表，支持树形） |
| `book_info` | 图书基本信息、库存（`total_stock` / `available_stock`） |

### 4.3 业务表

| 表名 | 说明 |
|------|------|
| `borrow_record` | 借阅记录（状态：0申请中 / 1已通过 / 2借阅中 / 3已归还 / 4已拒绝 / 5逾期） |
| `reservation_record` | 预约记录（状态：0预约中 / 1已通知 / 2已取消 / 3已过期） |
| `book_stock_log` | 库存变更日志（init / borrow / return / adjust） |
| `reader_info` | 读者档案（借阅证、信用积分、黑名单） |
| `sys_notice` | 公告/资讯 |
| `sys_operation_log` | 操作审计日志 |

### 4.4 视图

- `v_book_borrow_stat` — 图书借阅统计
- `v_user_borrow_summary` — 用户借阅汇总

## 5. 状态字典

| 表/字段 | 值 | 含义 |
|---------|----|------|
| `sys_user.status` | 0 | 禁用 |
| `sys_user.status` | 1 | 启用 |
| `book_info.status` | 0 | 下架 |
| `book_info.status` | 1 | 上架 |
| `borrow_record.status` | 0 | 申请中 |
| `borrow_record.status` | 1 | 已通过/待取书 |
| `borrow_record.status` | 2 | 借阅中 |
| `borrow_record.status` | 3 | 已归还 |
| `borrow_record.status` | 4 | 已拒绝 |
| `borrow_record.status` | 5 | 逾期 |
| `reservation_record.status` | 0 | 预约中 |
| `reservation_record.status` | 1 | 已通知 |
| `reservation_record.status` | 2 | 已取消 |
| `reservation_record.status` | 3 | 已过期 |
| `book_stock_log.change_type` | init / borrow / return / adjust | — |

## 6. 迁移规则

1. **禁止修改已发布脚本**。任何表结构变更必须新建 `V{next}__description.sql`。
2. 新脚本必须与 Entity、DTO、接口文档字段名保持一致。
3. 核心业务表禁止物理删除，使用 `deleted` 逻辑删除。
4. 库存相关更新必须使用数据库条件更新或乐观锁，禁止先查后改。
5. 生产环境执行迁移前必须先备份数据库。
