# 开发手册

## 1. 环境要求

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 17 LTS | 后端开发 |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+ / 20 LTS | 前端开发 |
| PostgreSQL | 15.x | 主数据库 |
| Redis | 7.x | 缓存、会话 |
| Docker | 24.x+ | 容器化部署 |

## 2. 本地启动

### 2.1 后端

```bash
cd book-management-backend
mvn spring-boot:run
```

默认访问：
- Swagger UI：`http://localhost:8080/api/swagger-ui/index.html`
- 健康检查：`http://localhost:8080/api/actuator/health`

### 2.2 管理端

```bash
cd book-management-admin
npm install
npm run dev
```

Vite 默认端口 `5173`，`/api` 代理到 `http://localhost:8080`。

### 2.3 小程序端

```bash
cd book-management-uniapp
npm install
npm run dev:mp-weixin
```

也可使用 HBuilderX 导入项目后运行到微信开发者工具。

## 3. 数据库与迁移

项目使用 **Flyway** 管理迁移，脚本位于 `book-management-backend/src/main/resources/db/migration/`。

当前脚本：

| 脚本 | 说明 |
|------|------|
| `V1__init_schema.sql` | 初始化全部表结构、索引、视图 |
| `V2__init_data.sql` | 初始化基础数据（角色、权限、管理员账号等） |
| `V3__add_openid_to_sys_user.sql` | 为 `sys_user` 增加 `openid` 字段 |
| `V4__fix_default_passwords.sql` | 修正默认用户密码哈希 |

**约束**：
- 已发布脚本禁止修改，只能追加新版本
- 新增字段、索引、枚举值必须新建 `V{next}__xxx.sql`
- 本地开发若需清库重来，可执行 `docker compose down -v`（仅开发环境）

## 4. 测试

### 4.1 单元测试

```bash
cd book-management-backend
mvn test
```

当前已覆盖：
- `AuthControllerTest`
- `BookControllerTest`

待补齐：核心 Service 层单元测试（目标覆盖率 ≥ 70%）。

### 4.2 接口测试

推荐使用 Postman / Apifox 导入 Swagger 定义：
`http://localhost:8080/api/v3/api-docs`

### 4.3 并发与集成测试重点

- 多用户同时借阅同一本低库存图书
- 审批通过后库存扣减与 `book_stock_log` 一致性
- 归还后库存恢复与预约队列通知

## 5. 代码规范

- **Java**：阿里巴巴 Java 开发手册，优先使用 `var`（局部变量）、Stream API、Optional
- **Vue / Uni-App**：组合式 API (`<script setup>`)，Pinia 管理全局状态，页面内禁止直接写 `axios`
- **数据库**：小写下划线命名，核心业务表必须具备 `created_at`、`updated_at`、`deleted`
- **Git**：主干开发 + 功能分支，commit message 使用中文简明描述

## 6. 调试技巧

| 问题 | 排查方式 |
|------|----------|
| 后端无法启动 | 检查 PostgreSQL/Redis 是否运行、环境变量是否正确、端口 8080 是否被占用 |
| 管理端 `/api` 404 | 检查 Vite proxy 配置或 Nginx `location /api/` 代理路径 |
| 小程序接口失败 | 检查 HTTPS、request 合法域名、后端 CORS 配置、微信开发者工具详情设置 |
| 数据库迁移失败 | 检查 Flyway 历史表 `flyway_schema_history`，禁止手动删改已执行脚本 |
| 登录 401 | 检查 Token 是否过期、Authorization 头格式是否为 `Bearer {token}` |
