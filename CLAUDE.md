# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 架构概览

中文校园管理系统，前后端分离单体架构。

```
school/
├── school-backend/     # Spring Boot 2.7 + Java 21
└── school-frontend/    # Vue 3 + Vite + Element Plus (JavaScript, 非 TypeScript)
```

## 后端关键信息

### 技术栈
- **Spring Boot 2.7.18** + Java 21 + Maven
- **MyBatis-Plus 3.5.5** — 分页、逻辑删除、自动填充
- **Spring Security + JWT (jjwt 0.12.5)** — 无状态认证
- **MySQL 8.0** — 数据库名 `school`，端口 3306，默认用户 root/root
- **Lombok** — 全量使用 `@Data`、`@RequiredArgsConstructor`

### 包结构（按业务域划分，非按层次）

```
com.school
├── common/       # BaseEntity, Result, PageResult, BusinessException, GlobalExceptionHandler
├── config/       # MyBatisPlusConfig, CorsConfig
├── security/     # SecurityConfig, JwtUtils, JwtAuthenticationFilter, AuthController, LoginUser
├── student/      # controller/service/impl/mapper/entity/dto
├── teacher/      # 同上
├── clazz/        # ⚠️ 注意：包名是 clazz 不是 class
├── course/        # 同上
├── grade/         # 同上
└── system/        # SysUser, SysRole, SysUserRole
```

**每个业务模块内部分层**：`controller → service/impl → convert → mapper → entity/dto`

### 核心约定

| 约定 | 说明 |
|------|------|
| 统一响应 | 所有 Controller 返回 `Result<T>`，格式 `{ code, message, data }` |
| 分页响应 | 返回 `PageResult<T>`，字段 `records, total, size, current` |
| 业务异常 | 抛 `BusinessException("消息")` 或 `BusinessException(code, "消息")` |
| 实体基类 | 继承 `BaseEntity`，自带 `id`、`createTime`(INSERT 填充)、`updateTime`(INSERT_UPDATE 填充) |
| 逻辑删除 | 全局配置 `deleted` 字段（1=已删除，0=未删除），实体类无需声明该字段 |
| 主键策略 | `@TableId(type = IdType.AUTO)` 自增（BaseEntity 已声明） |
| 权限注解 | `@PreAuthorize("hasRole('ADMIN')")` 或 `hasAnyRole('ADMIN','TEACHER')` |
| 参数校验 | Controller 入参 DTO 使用 `@Validated @RequestBody` |
| Mapper 扫描 | `@MapperScan("com.school.**.mapper")` 自动扫描所有模块下 mapper 包 |

### 数据库

- Schema 初始化脚本：`src/main/resources/db/schema.sql`
- 无 Flyway/Liquibase，手动管理 schema
- 下划线转驼峰已启用（`map-underscore-to-camel-case: true`）
- 枚举值约定：性别 `1=男 2=女`；状态 `1=启用 0=禁用`
- 班级表名是 `clazz`（非 class）

### 启动与运行

```bash
# 后端（在 school-backend/ 目录下）
mvn spring-boot:run               # 启动后端，端口 8080

# 数据库初始化
mysql -u root -p < src/main/resources/db/schema.sql
```

## 前端关键信息

### 技术栈
- **Vue 3** + Vite 5 + **JavaScript**（非 TypeScript）
- **Element Plus**（中文 locale `zhCn`）+ @element-plus/icons-vue
- **Pinia** 组合式 Store（`defineStore` + setup 函数）
- **Axios** 封装于 `src/utils/request.js`
- **Vue Router 4**，hash-free history 模式

### 目录结构

```
src/
├── api/          # 按模块拆分（auth.js, student.js, ...），每个函数一个导出
├── assets/       # 静态资源
├── components/   # 公共组件
├── layout/       # MainLayout.vue — 侧边栏 + 顶栏布局
├── router/       # index.js — 路由定义 + beforeEach 守卫
├── stores/       # user.js — Pinia store（token, userInfo, roles）
├── utils/        # request.js — axios 实例（/api 前缀，JWT 拦截器）
└── views/        # 按模块目录：student/StudentList.vue 等
```

### 前端核心约定

| 约定 | 说明 |
|------|------|
| 组件风格 | `<script setup>` 组合式 API，**不使用选项式 API** |
| API 前缀 | axios baseURL 为 `/api`，前端请求路径如 `/students` 对应后端 `/api/students` |
| 代理配置 | Vite 开发将 `/api` 代理到 `http://localhost:8080` |
| JWT | 存于 `localStorage`，请求拦截器自动附加 `Authorization: Bearer {token}` |
| 响应解包 | 响应拦截器自动检查 `res.code !== 200`，业务错误 `ElMessage.error`，401 自动 logout |
| 权限 | 路由 `meta.roles` 数组控制页面访问；侧边栏菜单根据 `userStore.roles` 动态过滤 |

### 启动与运行

```bash
# 前端（在 school-frontend/ 目录下）
npm install      # 安装依赖
npm run dev      # 开发服务器，端口 5173
npm run build    # 生产构建
```

## 开发流程

1. **新增业务模块**：参照 `student/` 模块结构，创建 `controller/service/impl/mapper/entity/dto` 五件套
2. **Entity**：继承 `BaseEntity`，加 `@TableName`，逻辑删除字段无需声明
3. **Mapper**：继承 `BaseMapper<Xxx>`，加 `@Mapper`
4. **Service**：接口继承 `IService<Xxx>`，实现类继承 `ServiceImpl<XxxMapper, Xxx>`
5. **Controller**：注入 Service，返回 `Result<T>`，分页用 `PageResult<T>`
6. **前端页面**：参照 `StudentList.vue` 模式 — 搜索表单 + 表格 + 分页 + 弹窗增删改
7. **前端 API**：参照 `student.js` — 每个操作一个导出函数
