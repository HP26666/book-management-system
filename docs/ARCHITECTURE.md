# 系统架构

## 1. 技术栈

| 模块 | 技术选型 | 说明 |
|------|----------|------|
| 后端 | Spring Boot 3.2.5、Spring Security 6、Spring Data JPA、PostgreSQL Driver、Redis、JWT (jjwt 0.12.5)、SpringDoc OpenAPI | RESTful API + RBAC |
| 管理端 | Vue 3、Vite 5、Element Plus、Pinia、Axios、ECharts、vue-echarts | PC 后台管理 |
| 小程序端 | Uni-App Vue 3、Pinia、uView Plus | 微信小程序用户端 |
| 基础设施 | Docker、Docker Compose、Nginx、PostgreSQL 15、Redis 7 | 开发与生产部署 |

## 2. 部署拓扑

```
                    互联网/局域网用户
                         │
                    ┌────┴────┐
                    │  Nginx  │  ← admin 容器：托管 Vue 静态资源 + /api 代理
                    └────┬────┘
           ┌─────────────┼─────────────┐
           │             │             │
    ┌──────┴──────┐ ┌───┴───┐   ┌────┴────┐
    │  Vue Admin  │ │ UniApp│   │ Spring  │
    │  (Docker)   │ │ (微信) │   │ Boot API│
    └─────────────┘ └───────┘   └────┬────┘
                              ┌──────┴──────┐
                              │ PostgreSQL  │
                              │    Redis    │
                              └─────────────┘
```

## 3. 模块职责

| 模块 | 后端 Controller | 管理端页面 | 小程序端页面 |
|------|-----------------|------------|--------------|
| 用户权限 | AuthController, UserController, RoleController, PermissionController | 登录、用户管理、角色权限 | 微信登录 |
| 图书管理 | BookController, CategoryController | 图书列表、分类管理 | 首页、搜索、图书详情 |
| 借阅管理 | BorrowController | 借阅记录、审批、归还登记 | 借阅记录、续借 |
| 预约管理 | ReservationController | 预约记录 | 预约记录 |
| 读者管理 | ReaderController | 读者档案 | 个人中心 |
| 公告管理 | NoticeController | 公告管理 | 首页轮播 |
| 系统管理 | OperationLogController, StatisticsController | Dashboard、操作日志 | — |

## 4. 核心数据流

1. **认证流**：Bearer Token（JWT），登录后返回 `accessToken` + `refreshToken`
2. **权限流**：Spring Security + 方法级 `@PreAuthorize`，角色包括 `admin`、`librarian`、`reader`
3. **借阅闭环**：申请(0) → 审批通过(1) → 取书/借阅中(2) → 归还(3)；逾期(5)
4. **库存控制**：数据库条件更新（`available_stock > 0`）+ 事务，禁止无保护先查后改
5. **文件上传**：后端本地存储（`./uploads`），Docker 中挂载 `upload_data` 数据卷

## 5. 工程约束

- 后端每个模块至少包含 `entity`、`repository`、`service`、`controller`、`dto`
- 请求 DTO 必须带 Bean Validation 注解
- 不允许 Controller 直接访问 Repository
- 所有状态枚举在后端用枚举类表达，前端维护统一字典映射
- 数据库结构变更必须通过 Flyway 新增迁移脚本，禁止修改已发布脚本
