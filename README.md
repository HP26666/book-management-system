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
│       ├── java/com/bookms/       # controller / service / repository / entity / dto / security 等
│       └── resources/
│           ├── application.yml
│           └── db/migration/      # Flyway 迁移脚本
├── book-management-admin/          # Vue 3 管理端
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── src/
│       ├── api/                   # 按模块封装的 Axios 接口
│       ├── layout/                # 后台布局组件
│       ├── router/                # Vue Router 配置
│       ├── stores/                # Pinia 状态管理
│       ├── utils/                 # 请求封装、工具函数
│       ├── views/                 # 页面视图（登录、Dashboard、图书、借阅等）
│       ├── styles/
│       ├── App.vue
│       └── main.js
├── book-management-uniapp/         # Uni-App 小程序端，不纳入 Docker 运行
│   ├── package.json
│   └── src/
│       ├── api/                   # 接口封装
│       ├── pages/                 # 页面（首页、搜索、详情、借阅、预约、个人中心）
│       ├── stores/                # Pinia 状态管理
│       ├── utils/                 # 请求封装
│       ├── App.vue
│       └── main.js
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

- 管理端：`http://localhost:16666`
- 后端 Swagger/OpenAPI：`http://localhost:8080/api/swagger-ui/index.html`
- 后端健康检查：`http://localhost:8080/api/actuator/health`
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

## 文档目录

| 文档 | 读者 | 用途 |
|------|------|------|
| [`docs/README.md`](./docs/README.md) | 全员 | 文档导航与快速入口 |
| [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) | 开发 / 运维 | 系统架构、模块边界、部署拓扑 |
| [`docs/DEVELOPMENT.md`](./docs/DEVELOPMENT.md) | 开发 | 本地环境搭建、构建、测试、调试 |
| [`docs/API.md`](./docs/API.md) | 前端 / 测试 / 运维 | 接口规范、认证方式、接口速查 |
| [`docs/DATABASE.md`](./docs/DATABASE.md) | 后端 / DBA | 数据库设计、Flyway 迁移规则 |
| [`docs/DEPLOYMENT.md`](./docs/DEPLOYMENT.md) | 运维 / 开发 | Docker 部署、备份、排障 |
| [`docs/USER_GUIDE.md`](./docs/USER_GUIDE.md) | 最终用户 | PC 管理端与小程序操作说明 |
| [`docs/CHANGELOG.md`](./docs/CHANGELOG.md) | 全员 | 版本变更记录、已知问题 |
| [`docs/archive/`](./docs/archive/) | 全员 | 前期规划文档（01-09）归档 |

> 前期规划文档（需求规格、概要设计、详细设计、验收目标等）已归档至 `docs/archive/`。

## 后续 Agent 开发约束

1. 每次开发前先阅读相关文档，变更功能时同步更新需求、接口、数据库和测试文档。
2. 后端新增接口必须提供 DTO、参数校验、统一响应、权限控制、单元/接口测试和 OpenAPI 注解。
3. 数据库结构变更必须以迁移脚本方式提交，不允许依赖 `ddl-auto=update` 修改生产库。
4. 管理端和小程序端必须通过统一请求封装访问 `/api`，禁止页面直接拼接分散的后端地址。
5. PC 管理端和后端必须保持 `docker compose up -d --build` 可启动；小程序端必须保持 `npm run dev:mp-weixin` 可构建。

## License

MIT
