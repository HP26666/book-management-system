# 图书管理系统

基于 Spring Boot 3 + Vue 3 + PostgreSQL + Uni-App 的图书管理系统，支持 PC Web 管理端和微信小程序用户端访问。PC 管理端、后端、PostgreSQL、Redis 均通过 Docker Compose 部署；Uni-App 小程序端按微信开发者工具/HBuilderX 流程单独构建发布。

## 技术栈

| 模块 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.2.5、Spring Security 6、Spring Data JPA、PostgreSQL Driver、Redis、JWT、SpringDoc |
| 管理端 | Vue 3、Vite 5、Element Plus、Pinia、Axios、ECharts |
| 小程序端 | Uni-App Vue 3、Pinia、uView Plus、微信小程序 |
| 基础设施 | Docker、Docker Compose、Nginx、PostgreSQL 15、Redis 7 |

## 项目结构

```text
book_management_system/
├── docs/                          # 项目文档，按编号顺序阅读和维护
├── book-management-backend/        # Spring Boot 后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
├── book-management-admin/          # Vue 3 管理端
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── src/
├── book-management-uniapp/         # Uni-App 小程序端，不纳入 Docker 运行
│   ├── package.json
│   └── src/
├── .env.example                    # Docker Compose 环境变量模板
├── docker-compose.yml              # 本地/测试环境编排
└── README.md
```

## 快速开始

### 1. 准备环境变量

```bash
cp .env.example .env
```

生产或公开测试环境必须修改 `.env` 中的 `POSTGRES_PASSWORD` 和 `JWT_SECRET`。

### 2. Docker 启动后端与管理端

```bash
docker compose up -d --build
```

默认访问地址：

- 管理端：`http://localhost`
- 后端健康检查：`http://localhost:8080/actuator/health`
- PostgreSQL：`localhost:5432`
- Redis：`localhost:6379`

### 3. 本地开发启动

后端：

```bash
cd book-management-backend
mvn spring-boot:run
```

管理端：

```bash
cd book-management-admin
npm install
npm run dev
```

小程序端：

```bash
cd book-management-uniapp
npm install
npm run dev:mp-weixin
```

也可以使用 HBuilderX 导入 `book-management-uniapp`，配置微信小程序 `appid` 后运行到微信开发者工具。

## 文档阅读顺序

| 顺序 | 文档 | 用途 |
|------|------|------|
| 01 | `docs/01-需求规格说明书.md` | 明确业务边界、角色、功能和非功能要求 |
| 02 | `docs/02-概要设计说明书.md` | 明确总体架构、模块边界和运行环境 |
| 03 | `docs/03-详细设计说明书.md` | 指导后端、管理端、小程序端的代码结构和流程实现 |
| 04 | `docs/04-数据库设计说明书.md` | 指导 PostgreSQL 表结构、索引、枚举和迁移脚本 |
| 05 | `docs/05-接口设计文档.md` | 指导前后端 API 契约、鉴权和错误码 |
| 06 | `docs/06-测试计划.md` | 指导单元、接口、集成、性能、安全测试 |
| 07 | `docs/07-部署运维手册.md` | 指导 Docker 部署、环境变量、日志、备份与排障 |
| 08 | `docs/08-用户操作手册.md` | 指导最终用户操作和功能验收体验 |
| 09 | `docs/09-项目验收目标文档.md` | 作为开发完成后的验收清单和通过标准 |

## 后续 Agent 开发约束

1. 每次开发前先阅读相关文档，变更功能时同步更新需求、接口、数据库和测试文档。
2. 后端新增接口必须提供 DTO、参数校验、统一响应、权限控制、单元/接口测试和 OpenAPI 注解。
3. 数据库结构变更必须以迁移脚本方式提交，不允许依赖 `ddl-auto=update` 修改生产库。
4. 管理端和小程序端必须通过统一请求封装访问 `/api`，禁止页面直接拼接分散的后端地址。
5. PC 管理端和后端必须保持 `docker compose up -d --build` 可启动；小程序端必须保持 `npm run dev:mp-weixin` 可构建。

## License

MIT
