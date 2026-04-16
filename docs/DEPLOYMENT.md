# 部署运维手册

## 1. 部署边界

| 模块 | Docker 部署 | 说明 |
|------|-------------|------|
| PostgreSQL | 是 | Docker Compose 编排，数据卷持久化 |
| Redis | 是 | Docker Compose 编排，数据卷持久化 |
| Spring Boot 后端 | 是 | `book-management-backend/Dockerfile` |
| Vue 管理端 | 是 | `book-management-admin/Dockerfile`，Nginx 托管 |
| Uni-App 小程序端 | 否 | 使用 HBuilderX / 微信开发者工具单独构建发布 |

## 2. 环境变量

复制模板并修改：

```bash
cp .env.example .env
```

关键变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `POSTGRES_DB` | `book_management` | 数据库名 |
| `POSTGRES_USER` | `bookuser` | 数据库用户 |
| `POSTGRES_PASSWORD` | `change-me` | **测试/生产必须修改** |
| `POSTGRES_PORT` | `5432` | 宿主机端口 |
| `REDIS_PORT` | `6379` | 宿主机端口 |
| `BACKEND_PORT` | `8080` | 后端宿主机端口 |
| `ADMIN_PORT` | `16666` | 管理端宿主机端口 |
| `JWT_SECRET` | `replace-with-at-least-32-random-characters` | **生产必须修改** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost,...` | 根据实际域名调整 |

## 3. Docker Compose 部署

### 3.1 服务组成

| 服务 | 容器名 | 端口映射 | 健康检查 |
|------|--------|----------|----------|
| postgres | `book-postgres` | `${POSTGRES_PORT}:5432` | `pg_isready` |
| redis | `book-redis` | `${REDIS_PORT}:6379` | `redis-cli ping` |
| backend | `book-backend` | `${BACKEND_PORT}:8080` | `/api/actuator/health` |
| admin | `book-admin` | `${ADMIN_PORT:-16666}:80` | 依赖后端健康 |

### 3.2 启动与停止

```bash
# 启动（首次或代码变更后）
docker compose up -d --build

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f backend
docker compose logs -f admin

# 停止
docker compose down

# 清空数据卷（仅开发/测试环境慎用）
docker compose down -v
```

### 3.3 健康检查

```bash
# 直接访问后端
curl http://localhost:8080/api/actuator/health

# 通过 Nginx 代理访问
curl http://localhost:16666/api/actuator/health
```

## 4. Nginx 配置说明

管理端容器内 Nginx 配置位于 `book-management-admin/nginx.conf`：

| 路径 | 代理目标 | 说明 |
|------|----------|------|
| `/` | 本地静态资源 | Vue 管理端，history fallback |
| `/api/` | `http://book-backend:8080/api/` | API 代理 |
| `/uploads/` | `http://book-backend:8080/api/uploads/` | 图书封面等静态资源 |

## 5. 数据库备份与恢复

### 5.1 备份

```bash
docker exec book-postgres pg_dump -U bookuser -d book_management > backup_$(date +%Y%m%d).sql
```

### 5.2 恢复

```bash
docker exec -i book-postgres psql -U bookuser -d book_management < backup_20260416.sql
```

### 5.3 生产要求

- 每日至少一次全量备份
- 备份文件保留 30 天以上，建议加密并异地保存
- 每月至少进行一次恢复演练

## 6. 发布流程

1. 确认代码已合并到发布分支
2. 确认接口文档、数据库文档、测试用例已同步更新
3. 执行后端单元测试、管理端构建、小程序构建
4. 备份目标环境数据库
5. 执行 `docker compose up -d --build`
6. 检查 `docker compose ps` 所有服务健康
7. 执行冒烟用例：登录、图书查询、借阅申请、审批、归还、预约
8. 记录版本号、发布时间、变更内容和回滚方案

## 7. 常见问题排查

| 现象 | 排查方向 |
|------|----------|
| 后端无法启动 | `docker compose logs backend` → 检查数据库连接、环境变量、端口占用 |
| 管理端页面空白 | 浏览器控制台、Nginx 日志、构建产物是否存在 |
| `/api` 访问失败 | 检查 `nginx.conf` 代理、`book-backend` 容器健康状态 |
| 数据库连接失败 | 检查 `POSTGRES_*` 变量、PostgreSQL 健康检查、数据卷权限 |
| 小程序无法请求接口 | 检查 HTTPS、微信合法域名、后端 CORS、接口证书 |
| 借阅并发库存异常 | 检查库存扣减 SQL 是否为条件更新，是否有事务覆盖 |
