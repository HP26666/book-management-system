# 接口文档

## 1. 通用规范

- **协议**：HTTP/HTTPS
- **数据格式**：JSON (`Content-Type: application/json`)
- **字符编码**：UTF-8
- **认证方式**：Bearer Token
  - 登录后获取 `accessToken`，后续请求在 `Authorization` 头部携带：`Bearer {accessToken}`
  - Token 过期后使用 `refreshToken` 换取新的 `accessToken`

## 2. 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页数据 `data` 结构：

```json
{
  "total": 100,
  "pages": 10,
  "current": 1,
  "size": 10,
  "records": []
}
```

## 3. 全局错误码

| 错误码 | 含义 | 说明 |
|--------|------|------|
| 200 | 成功 | — |
| 400 | 请求参数错误 | 参数校验失败或格式不正确 |
| 401 | 未授权 | Token 无效或已过期 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 请求的接口或数据不存在 |
| 409 | 业务冲突 | 库存不足、重复操作等 |
| 500 | 服务器内部错误 | 系统异常 |

## 4. 接口速查表

### 认证 (Auth)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 登录 | POST | `/api/auth/login` | 用户名密码登录 |
| 注册 | POST | `/api/auth/register` | 新用户注册 |
| 微信登录 | POST | `/api/auth/wechat-login` | 小程序 code 登录 |
| 刷新 Token | POST | `/api/auth/refresh` | refreshToken 换 accessToken |
| 登出 | POST | `/api/auth/logout` | 退出登录 |

### 用户 (User)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 当前用户 | GET | `/api/users/me` | 获取登录用户信息 |
| 用户列表 | GET | `/api/users` | 分页查询 |
| 创建用户 | POST | `/api/users` | 管理员创建 |
| 更新用户 | PUT | `/api/users/{id}` | 更新信息 |
| 删除用户 | DELETE | `/api/users/{id}` | 逻辑删除 |
| 重置密码 | POST | `/api/users/{id}/reset-password` | 管理员重置密码 |

### 图书 (Book)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 图书列表 | GET | `/api/books` | 分页搜索 |
| 图书详情 | GET | `/api/books/{id}` | — |
| ISBN 解析 | GET | `/api/books/isbn/{isbn}` | 根据 ISBN 获取元数据 |
| 创建图书 | POST | `/api/books` | — |
| 更新图书 | PUT | `/api/books/{id}` | — |
| 删除图书 | DELETE | `/api/books/{id}` | 逻辑删除 |
| 调整库存 | POST | `/api/books/{id}/stock-adjust` | 管理员调整库存 |

### 分类 (Category)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 分类树 | GET | `/api/categories/tree` | 树形结构 |
| 分类列表 | GET | `/api/categories` | 分页查询 |
| 创建分类 | POST | `/api/categories` | — |
| 更新分类 | PUT | `/api/categories/{id}` | — |
| 删除分类 | DELETE | `/api/categories/{id}` | 有关联图书时禁止删除 |

### 借阅 (Borrow)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 借阅列表(管理) | GET | `/api/borrows` | 管理员/图书管理员查询 |
| 我的借阅 | GET | `/api/borrows/my` | 当前用户查询 |
| 申请借阅 | POST | `/api/borrows/apply` | 用户提交申请 |
| 审批通过 | POST | `/api/borrows/{id}/approve` | — |
| 审批驳回 | POST | `/api/borrows/{id}/reject` | 需传 `reason` |
| 归还图书 | POST | `/api/borrows/{id}/return` | 读者端发起归还 |
| 确认归还 | POST | `/api/borrows/{id}/confirm-return` | 管理员确认，释放库存 |
| 续借 | POST | `/api/borrows/{id}/renew` | — |

### 预约 (Reservation)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 预约列表(管理) | GET | `/api/reservations` | — |
| 我的预约 | GET | `/api/reservations/my` | — |
| 提交预约 | POST | `/api/reservations` | — |
| 取消预约 | POST | `/api/reservations/{id}/cancel` | — |

### 读者 (Reader)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 读者列表 | GET | `/api/readers` | 分页查询 |
| 读者详情 | GET | `/api/readers/user/{userId}` | 根据 userId 查询 |
| 更新读者 | PUT | `/api/readers/{id}` | — |
| 黑名单开关 | PATCH | `/api/readers/{id}/blacklist` | 传 `blacklist` 参数 |

### 公告 (Notice)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 公告列表 | GET | `/api/notices` | — |
| 最新公告 | GET | `/api/notices/latest` | 小程序首页展示 |
| 创建公告 | POST | `/api/notices` | — |
| 更新公告 | PUT | `/api/notices/{id}` | — |
| 删除公告 | DELETE | `/api/notices/{id}` | — |

### 统计 (Statistics)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| Dashboard | GET | `/api/statistics/dashboard` | 仪表盘数据 |

### 日志 (Log)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 操作日志列表 | GET | `/api/logs/operations` | 分页查询 |

### 文件 (File)

| 接口 | 方法 | URL | 说明 |
|------|------|-----|------|
| 上传图片 | POST | `/api/files/upload` | `multipart/form-data` |
| 删除图片 | DELETE | `/api/files?path={path}` | — |

## 5. 运维接口

| 路径 | 说明 |
|------|------|
| `GET /api/actuator/health` | 健康检查 |
| `GET /api/swagger-ui/index.html` | Swagger UI |
| `GET /api/v3/api-docs` | OpenAPI JSON |
