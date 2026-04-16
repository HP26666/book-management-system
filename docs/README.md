# 项目文档

本文档目录面向开发、运维及最终用户，按实际工作场景组织。

| 文档 | 读者 | 内容说明 |
|------|------|----------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | 开发 / 运维 | 系统架构、技术栈、模块边界、部署拓扑 |
| [DEVELOPMENT.md](./DEVELOPMENT.md) | 开发 | 本地环境搭建、构建、测试、代码规范、调试技巧 |
| [API.md](./API.md) | 前端 / 测试 / 运维 | 前后端接口规范、认证方式、各模块接口速查 |
| [DATABASE.md](./DATABASE.md) | 后端 / DBA | 数据库设计、表结构、索引、Flyway 迁移规则 |
| [DEPLOYMENT.md](./DEPLOYMENT.md) | 运维 / 开发 | Docker Compose 生产部署、环境变量、备份、排障 |
| [USER_GUIDE.md](./USER_GUIDE.md) | 最终用户 | PC 管理端与微信小程序操作说明 |
| [CHANGELOG.md](./CHANGELOG.md) | 全员 | 版本变更记录、已知问题、兼容性说明 |
| [archive/](./archive/) | 全员 | 前期规划文档（需求规格、概要设计、详细设计、验收目标等） |

---

## 快速导航

- **第一次接触项目**：先读根目录 `README.md` → `ARCHITECTURE.md` → `DEVELOPMENT.md`
- **需要调用接口**：直接查看 `API.md` 第 3 节接口速查表
- **需要排查线上问题**：查看 `DEPLOYMENT.md` 日志与常见问题章节
- **需要改数据库**：查看 `DATABASE.md` 迁移约束与表字典
