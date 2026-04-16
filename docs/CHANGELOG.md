# 变更日志

## [Unreleased]

### 新增
- 后端：用户认证（JWT + Spring Security）、RBAC 权限管理、图书/分类/借阅/预约/读者/公告/统计/日志全模块接口
- 管理端：登录页、Dashboard、图书管理、分类管理、借阅管理、用户/角色/权限管理、读者管理、公告管理、操作日志
- 小程序端：微信登录、首页、搜索、图书详情、借阅记录、预约记录、个人中心
- 基础设施：Docker Compose 编排（PostgreSQL + Redis + 后端 + Nginx 管理端）、Flyway 数据库迁移

### 变更
- 文档体系重构：将前期规划文档（01-09）归档至 `docs/archive/`，新增面向开发/生产阶段的 `ARCHITECTURE.md`、`DEVELOPMENT.md`、`API.md`、`DATABASE.md`、`DEPLOYMENT.md`、`USER_GUIDE.md`
- `docker-compose.yml`：admin 默认端口从 `80` 调整为 `16666`；后端健康检查路径修正为 `/api/actuator/health`
- `README.md`：更新项目结构、访问地址、技术栈说明

### 修复
- 后端健康检查路径与 Nginx 代理路径对齐（增加 `/api` 前缀）
- 管理端 `App.vue` 从骨架页替换为 `<router-view />`，支持完整路由渲染
- 小程序端 `App.vue` 补充全局样式、空状态、状态标签等通用 CSS

### 已知问题
- Service 层单元测试覆盖率尚未达到 70% 目标
- 小程序端 ISBN 扫码查书功能待补充
- 性能测试与安全渗透测试待执行

---

## 归档历史

前期规划文档（需求规格、概要设计、详细设计、验收目标等）已迁移至 `docs/archive/` 目录保留。
