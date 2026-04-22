# 校园管理系统设计文档

## 一、项目概述

基础校园管理系统，包含前后端，提供学生管理、教师管理、班级管理、课程管理、成绩管理和系统管理等核心功能。

**用户角色**：管理员、教师、学生（三种角色）

## 二、技术选型

| 层面 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.x |
| 安全框架 | Spring Security + JWT | - |
| ORM | MyBatis-Plus | 3.5.x |
| 数据库 | MySQL（本地，数据库名：school） | 8.0 |
| 构建工具 | Maven | 3.8+ |
| 前端框架 | Vue | 3.x |
| UI 组件库 | Element Plus | 最新版 |
| 状态管理 | Pinia | 最新版 |
| HTTP 客户端 | Axios | 最新版 |
| 前端构建工具 | Vite | 最新版 |
| Java 版本 | JDK | 21 |
| Node 版本 | Node.js | 18+ |

## 三、整体架构

```
┌─────────────────────────────┐
│   前端 Vue 3 + Element Plus  │
│   端口: 5173 (开发环境)       │
└──────────┬──────────────────┘
           │ HTTP REST API (JSON)
┌──────────▼──────────────────┐
│   后端 Spring Boot 2.7+      │
│   ├── Controller 层 (接口)    │
│   ├── Service 层 (业务逻辑)   │
│   ├── Mapper 层 (MyBatis-Plus)│
│   ├── Security (JWT 认证鉴权) │
│   端口: 8080                  │
└──────────┬──────────────────┘
           │ JDBC
┌──────────▼──────────────────┐
│       MySQL 数据库            │
│       school                 │
└─────────────────────────────┘
```

## 四、后端包结构

```
com.school
├── common/          # 通用工具、响应封装、异常处理
├── config/          # 配置类 (Security, CORS, MyBatis-Plus)
├── security/        # JWT工具、过滤器、认证逻辑
├── student/         # 学生管理 (controller/service/mapper/entity)
├── teacher/         # 教师管理
├── clazz/           # 班级管理
├── course/          # 课程管理
├── grade/           # 成绩管理
└── system/          # 系统管理 (用户、角色、权限)
```

每个业务模块内部结构：
```
模块/
├── controller/      # REST 控制器
├── service/         # 业务逻辑接口与实现
├── mapper/          # MyBatis-Plus Mapper 接口
├── entity/          # 实体类
└── dto/             # 数据传输对象
```

## 五、数据库设计

数据库名：`school`

### 5.1 用户表 sys_user

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| username | VARCHAR(50), 唯一 | 用户名 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| avatar | VARCHAR(255) | 头像URL |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| status | TINYINT, 默认1 | 状态（1启用 0禁用） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

> 提示：用户角色通过 `sys_user_role` 关联表管理，不在 `sys_user` 表中冗余存储。

### 5.2 角色表 sys_role

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| role_name | VARCHAR(50) | 角色名称 |
| role_key | VARCHAR(50), 唯一 | 角色标识（ADMIN/TEACHER/STUDENT） |
| status | TINYINT, 默认1 | 状态 |
| create_time | DATETIME | 创建时间 |

### 5.3 用户角色关联表 sys_user_role

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| user_id | BIGINT, FK | 用户ID |
| role_id | BIGINT, FK | 角色ID |

### 5.4 学生表 student

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| user_id | BIGINT, FK | 关联用户ID |
| student_no | VARCHAR(30), 唯一 | 学号 |
| name | VARCHAR(50) | 姓名 |
| gender | TINYINT | 性别（1男 2女） |
| age | INT | 年龄 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| class_id | BIGINT, FK | 班级ID |
| enrollment_date | DATE | 入学日期 |
| status | TINYINT, 默认1 | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 5.5 教师表 teacher

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| user_id | BIGINT, FK | 关联用户ID |
| teacher_no | VARCHAR(30), 唯一 | 工号 |
| name | VARCHAR(50) | 姓名 |
| gender | TINYINT | 性别（1男 2女） |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| title | VARCHAR(50) | 职称 |
| status | TINYINT, 默认1 | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 5.6 班级表 clazz

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| class_name | VARCHAR(50) | 班级名称 |
| grade_level | VARCHAR(20) | 年级 |
| teacher_id | BIGINT, FK | 班主任ID |
| student_count | INT, 默认0 | 学生人数 |
| status | TINYINT, 默认1 | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 5.7 课程表 course

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| course_name | VARCHAR(100) | 课程名称 |
| course_code | VARCHAR(30), 唯一 | 课程编号 |
| credit | DECIMAL(3,1) | 学分 |
| teacher_id | BIGINT, FK | 任课教师ID |
| class_id | BIGINT, FK | 班级ID |
| schedule | VARCHAR(100) | 上课时间 |
| status | TINYINT, 默认1 | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 5.8 成绩表 grade

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT, PK, 自增 | 主键 |
| student_id | BIGINT, FK | 学生ID |
| course_id | BIGINT, FK | 课程ID |
| score | DECIMAL(5,2) | 成绩 |
| exam_type | VARCHAR(20) | 考试类型（期中/期末/平时） |
| semester | VARCHAR(20) | 学期（如：2025-2026-1） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 六、API 接口设计

统一前缀：`/api`

统一响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 6.1 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录，返回 JWT Token |
| POST | /api/auth/logout | 退出登录 |
| GET | /api/auth/info | 获取当前用户信息 |

### 6.2 学生管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/students | 分页查询学生列表 |
| GET | /api/students/{id} | 查询学生详情 |
| POST | /api/students | 新增学生 |
| PUT | /api/students/{id} | 修改学生信息 |
| DELETE | /api/students/{id} | 删除学生 |

### 6.3 教师管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/teachers | 分页查询教师列表 |
| GET | /api/teachers/{id} | 查询教师详情 |
| POST | /api/teachers | 新增教师 |
| PUT | /api/teachers/{id} | 修改教师信息 |
| DELETE | /api/teachers/{id} | 删除教师 |

### 6.4 班级管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/classes | 分页查询班级列表 |
| GET | /api/classes/{id} | 班级详情（含学生列表） |
| POST | /api/classes | 新增班级 |
| PUT | /api/classes/{id} | 修改班级 |
| DELETE | /api/classes/{id} | 删除班级 |
| POST | /api/classes/{id}/students | 分配学生到班级 |

### 6.5 课程管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/courses | 分页查询课程列表 |
| GET | /api/courses/{id} | 查询课程详情 |
| POST | /api/courses | 新增课程 |
| PUT | /api/courses/{id} | 修改课程 |
| DELETE | /api/courses/{id} | 删除课程 |

### 6.6 成绩管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/grades | 查询成绩（支持按学生/课程/学期筛选） |
| POST | /api/grades | 录入成绩 |
| PUT | /api/grades/{id} | 修改成绩 |
| GET | /api/grades/statistics | 成绩统计 |

### 6.7 系统管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/users | 分页查询用户列表 |
| POST | /api/system/users | 新增用户 |
| PUT | /api/system/users/{id} | 修改用户 |
| DELETE | /api/system/users/{id} | 删除用户 |
| PUT | /api/system/users/{id}/reset-password | 重置密码 |

## 七、权限控制

| 角色 | 权限范围 |
|------|---------|
| 管理员（ADMIN） | 所有接口的完整访问权限 |
| 教师（TEACHER） | 查看学生信息；管理自己课程的成绩；查看班级信息；修改个人信息 |
| 学生（STUDENT） | 查看自己的信息、课程和成绩；修改个人信息 |

## 八、前端页面设计

**整体布局**：顶部导航栏 + 左侧菜单 + 右侧内容区

| 页面 | 路径 | 说明 | 权限 |
|------|------|------|------|
| 登录页 | /login | 用户名密码登录 | 公开 |
| 首页/仪表盘 | /dashboard | 统计概览（学生数、教师数、班级数等） | 全部角色 |
| 学生列表 | /students | 表格展示，支持搜索、分页、增删改 | 管理员、教师（只读） |
| 教师列表 | /teachers | 表格展示，支持搜索、分页、增删改 | 管理员 |
| 班级列表 | /classes | 表格展示，可查看班级下学生，分配学生 | 管理员、教师（只读） |
| 课程列表 | /courses | 表格展示，关联教师和班级 | 管理员、教师（自己的课程） |
| 成绩管理 | /grades | 成绩录入和查询，按班级/课程/学期筛选 | 管理员、教师（自己的课程）、学生（只读自己的） |
| 用户管理 | /system/users | 账号管理 | 管理员 |
| 个人中心 | /profile | 修改个人信息、密码 | 全部角色 |

**前端技术要点**：
- Vue Router 路由管理 + 路由守卫（权限拦截）
- Pinia 状态管理（用户信息、Token 存储）
- Axios 封装（统一请求拦截、JWT 自动携带、错误处理）
- 根据角色动态渲染左侧菜单

## 九、项目目录结构

```
school/
├── school-backend/              # 后端项目
│   ├── src/main/java/com/school/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── mapper/
│   └── pom.xml
└── school-frontend/             # 前端项目
    ├── src/
    │   ├── api/                 # 接口请求封装
    │   ├── assets/              # 静态资源
    │   ├── components/          # 公共组件
    │   ├── layout/              # 布局组件
    │   ├── router/              # 路由配置
    │   ├── stores/              # Pinia 状态管理
    │   ├── utils/               # 工具函数
    │   ├── views/               # 页面视图
    │   ├── App.vue
    │   └── main.js
    ├── index.html
    ├── package.json
    └── vite.config.js
```
