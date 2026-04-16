# school-mcp 设计文档

| 项 | 值 |
|---|---|
| 创建日期 | 2026-04-16 |
| 作者 | zhangwh |
| 状态 | 设计已确认，待实现计划 |
| 关联仓库（待建） | https://github.com/zhangwh/school-mcp |

## 1. 背景与目标

### 1.1 背景

`school` 项目是基于 Spring Boot 2.7 + Vue 3 的中文校园管理系统，已实现学生 / 教师 / 班级 / 课程 / 成绩 / 系统用户 / 仪表盘 7 个业务模块的 REST API。当前所有操作只能通过前端页面完成。

### 1.2 目标

构建一个 **MCP（Model Context Protocol）服务**，让 Claude / Claude Code 等 AI 客户端能直接调用 school 后端 API。覆盖以下使用场景：

- **管理操作助手**：用自然语言增删改查（"新增学生张三，三年二班"）
- **数据查询助手**：让 AI 做统计、分析、报表
- **后端调试 / 测试工具**：开发阶段构造测试数据、验证业务逻辑
- **全量 API 暴露**：用户自由组合调用

### 1.3 非目标

- 不替代前端 UI
- 不做业务逻辑（业务全部在后端）
- 不支持多租户/多用户隔离（stdio 单用户场景）
- 不做监控、限流、熔断
- 不做 token 持久化

## 2. 整体架构

### 2.1 架构图

```
┌─────────────────────┐     stdio (JSON-RPC)      ┌──────────────────────────┐
│   Claude Code       │ ─────────────────────────►│   school-mcp (Python)     │
│   (用户客户端)       │ ◄─────────────────────────│   uvx 子进程              │
└─────────────────────┘                            └─────────────┬────────────┘
                                                                 │ HTTPS
                                                                 │ Bearer Token
                                                                 ▼
                                                   ┌──────────────────────────┐
                                                   │   Spring Boot 后端        │
                                                   │   /api/* + /v3/api-docs  │
                                                   └─────────────┬────────────┘
                                                                 │ JDBC
                                                                 ▼
                                                   ┌──────────────────────────┐
                                                   │   MySQL                  │
                                                   └──────────────────────────┘
```

### 2.2 三层职责

| 层 | 职责 | 不做什么 |
|---|---|---|
| **Claude Code 客户端** | 启动 MCP 子进程；调用 tool；展示结果；用户操作确认 | 不直接访问后端 |
| **school-mcp** | tool 定义；HTTP 客户端；JWT 自动登录/刷新；OpenAPI 拉取；错误透传 | 不做任何业务逻辑 |
| **Spring Boot 后端** | 业务逻辑、数据持久化、权限校验、逻辑删除 | 不感知 MCP（普通 HTTP 客户端而已） |

### 2.3 一次完整调用的数据流

正常路径（创建学生）：

```
1. 用户对 Claude 说："新增学生张三，三年二班"
2. Claude 调用：school_student(action="create", payload={...})
3. school-mcp:
   3.1 检查内存 token，无效 → POST /api/auth/login → 拿新 token
   3.2 POST /api/students  with Authorization: Bearer xxx
4. 后端：JWT 校验 → 业务校验 → 写库 → 返回 Result{code:200,data:{...}}
5. school-mcp 解包 data 返回给 Claude
6. Claude 用人话告诉用户
```

token 过期分支：

```
3.2 收到 401 → 强制重新登录 → 重发请求
3.3 仍 401 → 抛 ToolError("认证失败...")
```

业务异常分支：

```
4. 后端返回 Result{code:500, message:"学号已存在"}
5. school-mcp 抛 ToolError("学号已存在")
6. Claude 告知用户
```

## 3. 技术栈与分发

| 维度 | 选择 |
|---|---|
| 语言 | Python (>=3.10) |
| MCP SDK | 官方 `mcp` Python SDK |
| HTTP 客户端 | `httpx`（async） |
| 包管理 | `uv` / `uvx` |
| 传输模式 | stdio |
| 仓库 | 个人 GitHub 公开仓库 |
| 版本管理 | 主分支 main 即"最新"，不打 tag |
| 用户启动方式 | `uvx --refresh --from git+https://...@main school-mcp` |
| 更新机制 | 用户重启 Claude Code 客户端或 reconnect MCP |

### 3.1 分发模型说明

- 用户机器只需装 `uv`（一次性，自带 `uvx`，自动管理 Python）
- 公开仓库无需 git 认证
- `--refresh` 让 uvx 每次启动都解析 git ref 并拉新 commit
- `@main` 跟随主分支
- **MCP 协议本质限制**：进程一旦启动不会感知代码变化；用户必须重启客户端 / reconnect 才能用上新版

### 3.2 后端要求

后端必须部署且 MCP 进程能通过 HTTP 访问到 `SCHOOL_API_BASE`。

## 4. 项目结构

```
school-mcp/
├── pyproject.toml            # 包元数据 + 依赖 + 脚本入口
├── README.md                 # 用户配置说明
├── .gitignore
├── .python-version           # 锁定 Python 版本
├── src/
│   └── school_mcp/
│       ├── __init__.py
│       ├── main.py           # MCP server 启动入口
│       ├── config.py         # env 读取与校验
│       ├── auth.py           # JWT 登录与刷新
│       ├── client.py         # HTTP 客户端封装
│       ├── errors.py         # 自定义异常
│       └── tools/
│           ├── __init__.py
│           ├── student.py
│           ├── teacher.py
│           ├── clazz.py
│           ├── course.py
│           ├── grade.py
│           ├── system.py
│           ├── dashboard.py
│           └── raw.py        # school_list_apis + school_call
└── tests/
    ├── conftest.py
    ├── test_config.py
    ├── test_auth.py
    ├── test_client.py
    ├── test_tools_student.py
    └── test_raw.py
```

### 4.1 模块职责（单一职责）

| 模块 | 职责 | 不做什么 |
|---|---|---|
| `main.py` | 实例化 MCP server，注册 tools，启动 stdio 循环 | 不写业务/HTTP |
| `config.py` | 从 env 读配置，校验必填项 | 不读默认密码、不持久化 |
| `auth.py` | 维护 token、判断过期、登录、刷新；asyncio.Lock 并发控制 | 不做业务请求 |
| `client.py` | `SchoolClient.request(method, path, **kwargs)` 封装：注入 token、解包 Result、401 重试、OpenAPI 缓存 | 不知道有哪些 tool |
| `errors.py` | 定义异常类（AuthError / BusinessError / HttpError） | 不做日志 |
| `tools/<resource>.py` | tool 函数 + pydantic 参数 model；通过 client 调后端 | 不写 HTTP / 认证 |
| `tools/raw.py` | school_list_apis + school_call | — |

### 4.2 模块依赖关系

```
main.py
  ├── 引入所有 tools/*.py，注册到 MCP server
  └── 创建全局 SchoolClient 单例

tools/*.py ──► client.py ──► auth.py ──► config.py
                  ▲              │
                  └──────────────┘
                  (401 时通知 auth 强制刷新)

errors.py ──► 被所有模块 import
```

**关键约束**：
- `tools/*.py` **不直接 import auth.py**，认证细节由 client 隐藏
- `client.py` **不知道 tool 的业务语义**，只是一个加 token 的 HTTP 客户端

### 4.3 依赖

```toml
[project]
name = "school-mcp"
version = "0.1.0"
requires-python = ">=3.10"
dependencies = [
    "mcp>=1.0.0",
    "httpx>=0.27.0",
    "pydantic>=2.0",
    "pyjwt>=2.8.0",
]

[project.scripts]
school-mcp = "school_mcp.main:main"
```

## 5. 工具设计

### 5.1 工具总览（共 9 个）

| Tool 名 | 用途 | 关联后端路径 |
|---|---|---|
| `school_student` | 学生 CRUD + 分页 | `/api/students` |
| `school_teacher` | 教师 CRUD + 分页 | `/api/teachers` |
| `school_clazz` | 班级 CRUD + 分页 | `/api/clazz` |
| `school_course` | 课程 CRUD + 分页 | `/api/courses` |
| `school_grade` | 成绩 CRUD + 分页 | `/api/grades` |
| `school_user` | 系统用户管理 | `/api/system/users` |
| `school_dashboard` | 首页统计 | `/api/dashboard/stats` |
| `school_list_apis` | 拉取后端 OpenAPI 文档 | `/v3/api-docs` |
| `school_call` | 兜底通用 HTTP 调用 | 任意 `/api/*` |

### 5.2 资源 tool 统一签名（B 模式）

```python
@mcp.tool()
async def school_student(
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    keyword: str | None = None,
    class_id: int | None = None,
    payload: dict | None = None,
) -> dict:
    """
    学生管理。
    
    actions:
      - page: 分页查询。可选 keyword(模糊), class_id(按班级筛选)
      - get: 按 ID 查询。必填 id
      - create: 新增。必填 payload，字段参考 StudentDTO
      - update: 更新。必填 id 和 payload
      - delete: 删除（逻辑）。必填 id
    
    权限：create/update/delete 需要 ADMIN；page/get 需要 ADMIN 或 TEACHER。
    """
```

**关键决策**：
- 用 `payload: dict` 而非平铺字段，让 MCP 不必跟随后端 DTO 字段变化
- 用 `action` 参数分发（B 模式），减少 tool 数量
- 资源 tool 列表的查询参数（`keyword` 等）按各自 controller 实际参数定义

### 5.3 兜底 tool

`school_list_apis(keyword, detail=False)`：拉 OpenAPI 文档，输出裁剪：
- 默认只返回 `[{path, method, summary}]` 列表
- `detail=True` 返回完整参数定义
- `keyword` 按路径或 summary 关键字过滤

`school_call(method, path, query, body)`：通用 HTTP 代理，path 必须以 `/api/` 开头。

### 5.4 返回值约定

| 后端响应 | tool 返回 |
|---|---|
| `Result{code:200, data:{...}}` | `data` 部分 |
| `Result{code:200, data:null}` | `{"success": true}` |
| `Result{code:200, data:PageResult}` | `{records, total, size, current}` |
| `Result{code:500, message:"xx"}` | 抛 `ToolError("xx")` |
| HTTP 401（重试后仍 401） | 抛 `ToolError("认证失败...")` |
| HTTP 5xx | 抛 `ToolError("后端错误...")` |

## 6. 认证模块详细设计

### 6.1 AuthManager 实现

```python
class AuthManager:
    REFRESH_THRESHOLD = 60  # 距过期 60 秒主动刷新

    def __init__(self, config: Config, http: httpx.AsyncClient):
        self._config = config
        self._http = http
        self._token: str | None = None
        self._exp: float = 0.0
        self._lock = asyncio.Lock()

    async def get_token(self) -> str:
        """主动检查 + 刷新。"""
        if self._is_valid():
            return self._token
        async with self._lock:
            if self._is_valid():  # 双检锁
                return self._token
            await self._login()
        return self._token

    async def force_refresh(self) -> str:
        """被动场景（401）。"""
        async with self._lock:
            await self._login()
        return self._token

    def _is_valid(self) -> bool:
        return self._token is not None and (self._exp - time.time()) > self.REFRESH_THRESHOLD

    async def _login(self) -> None:
        # 调 POST /api/auth/login，解析 JWT exp 存到 self._exp
        # 失败抛 AuthError
        ...
```

### 6.2 client 调用模式

```python
async def request(self, method, path, **kwargs):
    token = await self._auth.get_token()
    resp = await self._send(method, path, token, **kwargs)
    if resp.status_code == 401:
        token = await self._auth.force_refresh()
        resp = await self._send(method, path, token, **kwargs)
    return self._unwrap(resp)
```

### 6.3 关键决策

1. **token 只存内存**：进程退出即清空，多实例天然隔离
2. **双检锁防并发登录**：并发请求时登录接口最多被打 1 次
3. **REFRESH_THRESHOLD = 60s**：避免"判断时还有效，发请求时刚过期"
4. **JWT 解析失败降级**：当作 5 分钟有效，靠 401 兜底
5. **共享 httpx 实例**：复用连接池

### 6.4 测试要点

| 用例 | 验证 |
|---|---|
| 首次调用触发 login | 调 mock 后端 1 次 |
| 连续调用 token 有效 → 不重复登录 | 1 次 |
| 距过期 < 60s → 自动刷新 | 触发 login |
| 并发 10 请求 token 过期 | login 仅 1 次 |
| 401 → 强制刷新 + 重试 | login 2 次，业务 2 次 |
| 重试后仍 401 → 抛 AuthError | 不无限重试 |
| login code != 200 → 抛 AuthError | message 透传 |

## 7. 后端 OpenAPI 集成

### 7.1 后端改动（共 3 处）

**改动 1：`pom.xml` 加依赖**

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

（适配 Spring Boot 2.7；3.x 需要换 `springdoc-openapi-starter-webmvc-ui` 2.x）

**改动 2：`application.yml` 配置**

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  packages-to-scan: com.school
```

**改动 3：`SecurityConfig` 放行 OpenAPI**

```java
.antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
```

### 7.2 MCP 内存缓存策略

```python
self._openapi_cache: dict | None = None
self._openapi_cached_at: float = 0
self.OPENAPI_TTL = 300  # 5 分钟

async def fetch_openapi(self) -> dict:
    if cache 有效: 返回
    GET /v3/api-docs → 缓存 → 返回
```

### 7.3 list_apis 输出裁剪

- 默认只返回 `{path, method, summary}` 列表
- `detail=True` 返回 `parameters / requestBody / responses`
- `keyword` 大小写不敏感过滤

### 7.4 验证后端集成

```bash
curl http://localhost:8080/v3/api-docs | jq .info
curl http://localhost:8080/v3/api-docs | jq '.paths | keys'
# 浏览器：http://localhost:8080/swagger-ui.html
```

## 8. 错误处理

### 8.1 异常类型

```python
class SchoolMcpError(Exception): pass
class AuthError(SchoolMcpError): pass
class BusinessError(SchoolMcpError): pass
class HttpError(SchoolMcpError): pass
```

### 8.2 错误对外措辞

| 内部异常 | 对外（ToolError 消息） |
|---|---|
| BusinessError | 透传后端 message |
| AuthError | "认证失败，请检查 SCHOOL_USERNAME/SCHOOL_PASSWORD" |
| HttpError 5xx | "后端服务异常，请稍后重试（HTTP {code}）" |
| TimeoutException | "请求超时，请检查 SCHOOL_API_BASE 是否可达" |
| ConnectError | "无法连接后端 {API_BASE}，请检查网络" |
| pydantic 校验 | MCP SDK 自动转 |

### 8.3 不做的事

- 不重试业务错误（只重试 401）
- 不吞错误
- 不把 stack trace 给 Claude

## 9. 日志策略

### 9.1 输出位置

**全部 stderr**（stdout 是 MCP 协议通道，不能污染）

```python
logging.basicConfig(
    level=os.getenv("SCHOOL_LOG_LEVEL", "INFO"),
    format="[%(asctime)s] %(levelname)s %(name)s: %(message)s",
    stream=sys.stderr,
)
```

### 9.2 级别使用

| 级别 | 内容 |
|---|---|
| INFO | 启动信息、登录成功、tool 调用 |
| WARNING | 401 触发刷新 |
| ERROR | 业务/认证/网络错误 |
| DEBUG | HTTP 请求/响应（脱敏） |

### 9.3 不写

- 密码、token 明文（即使 DEBUG 也不写）
- 完整请求/响应 body（只写 path + status）
- 日志文件（用户要持久化自己 `2>> log.txt`）

## 10. 配置（env 完整清单）

| 变量 | 必填 | 默认 | 说明 |
|---|---|---|---|
| `SCHOOL_API_BASE` | ✅ | — | 后端地址，例 `http://localhost:8080` |
| `SCHOOL_USERNAME` | ✅ | — | 登录账号 |
| `SCHOOL_PASSWORD` | ✅ | — | 登录密码 |
| `SCHOOL_TIMEOUT` | ❌ | `30` | HTTP 超时（秒） |
| `SCHOOL_LOG_LEVEL` | ❌ | `INFO` | DEBUG/INFO/WARNING/ERROR |
| `SCHOOL_OPENAPI_TTL` | ❌ | `300` | OpenAPI 缓存秒数 |

启动时缺必填变量直接 `SystemExit`。

## 11. 用户配置示例（README 给出）

```json
{
  "mcpServers": {
    "school": {
      "command": "uvx",
      "args": [
        "--refresh",
        "--from",
        "git+https://github.com/zhangwh/school-mcp.git@main",
        "school-mcp"
      ],
      "env": {
        "SCHOOL_API_BASE": "http://your-backend:8080",
        "SCHOOL_USERNAME": "admin",
        "SCHOOL_PASSWORD": "xxx"
      }
    }
  }
}
```

## 12. 测试策略

```
tests/
├── conftest.py            # respx mock httpx fixture
├── test_config.py         # env 缺失/异常
├── test_auth.py           # token 主动/被动刷新、并发锁
├── test_client.py         # 401 重试、Result 解包、错误转换
├── test_tools_student.py  # 完整 CRUD 路径
└── test_raw.py            # list_apis 裁剪、school_call 路径校验
```

原则：
- 用 `respx` mock httpx，不启动真实后端
- 一个 tool 文件一个测试文件，不强求每个 action 一个用例
- CI 跑 `pytest`，不强求覆盖率（胶水层项目）

## 13. 不做（YAGNI 清单）

| 不做 | 理由 |
|---|---|
| 多用户/会话隔离 | stdio 天然单用户 |
| token 持久化 | 重启 100ms 重新登录 |
| 限流/熔断 | 调用频率低 |
| 接口录制/回放 | DEBUG 日志够 |
| 配置热重载 | 重启进程即可 |
| 监控/metrics | 个人项目无意义 |
| git tag 版本管理 | main 即版本 |
| 内置版本 tool | 用户已确认不需要 |
| 危险操作二次确认 | Claude Code 客户端有兜底 |
| 文档 i18n | 中文够 |

## 14. 风险与缓解

| 风险 | 缓解 |
|---|---|
| 用户 push 坏代码到 main，所有用户重启即失败 | CI 跑基础冒烟测试；养成 feature 分支开发习惯 |
| 后端 DTO 字段变化，Claude 用 `payload` 时拼错字段 | OpenAPI 缓存 TTL 5 分钟，Claude 调 list_apis 能学到新结构 |
| OpenAPI 公开暴露所有接口形状 | 仅内网部署的项目可接受；公网部署应改回鉴权访问 |
| Python 进程启动慢（首次 uvx 拉依赖） | 仅首次或 `--refresh` 时慢；之后秒启 |
| token 过期 + 后端凭据已修改 → 自动重登失败 | 抛清晰错误，提示用户更新 env |

## 15. 开放问题

设计阶段无未决问题。实现阶段可能遇到的边界（实现时确认）：
- 各资源 controller 的精确查询参数（`teacher` / `clazz` / `course` / `grade` / `system` 的 keyword 之外参数）
- `school_user` 是否需要密码字段处理（创建用户时密码加密）
- Dashboard 是否有更多 endpoint（当前只看到 stats）

这些不影响整体设计，按"实现时对照真实 controller 定义"原则处理即可。
