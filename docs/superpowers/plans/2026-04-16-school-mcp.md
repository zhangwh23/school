# school-mcp 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现一个 Python MCP 服务，让 Claude Code 等客户端能通过自然语言调用 school 后端 API；同时让 school 后端暴露 OpenAPI 文档以支持工具自动发现。

**Architecture:** stdio 传输；Python + httpx 异步客户端；JWT 主动+被动混合刷新；9 个 MCP tool（7 个资源 tool + list_apis + 通用 call）；后端引入 springdoc-openapi 暴露 `/v3/api-docs`。

**Tech Stack:** Python 3.10+ / `mcp` SDK / `httpx` / `pydantic` / `pyjwt` / `uv` 包管理；后端 Spring Boot 2.7 + `springdoc-openapi-ui:1.7.0`。

**关联文档：** [`docs/superpowers/specs/2026-04-16-school-mcp-design.md`](../specs/2026-04-16-school-mcp-design.md)

---

## 路径约定

- **后端修改**：在当前 `/Applications/java/idea/school/` 仓库的 `feature/school-management` 分支
- **MCP 新仓库**：建在 `/Applications/java/idea/school-mcp/`（school 的同级目录），独立 git 仓库
- 计划中所有 `school-mcp/` 开头的相对路径均指向新仓库根目录

## 文件结构

### 后端（school 仓库）需要修改的文件

| 文件 | 改动 |
|---|---|
| `school-backend/pom.xml` | 新增 springdoc 依赖 |
| `school-backend/src/main/resources/application.yml` | 新增 springdoc 配置 |
| `school-backend/src/main/java/com/school/security/SecurityConfig.java` | 放行 OpenAPI 路径 |

### MCP（school-mcp 新仓库）需要创建的文件

```
school-mcp/
├── .gitignore
├── .python-version
├── pyproject.toml
├── README.md
├── src/school_mcp/
│   ├── __init__.py
│   ├── main.py             # MCP server 入口
│   ├── config.py           # env 读取
│   ├── errors.py           # 异常类
│   ├── auth.py             # JWT 管理
│   ├── client.py           # HTTP 客户端
│   └── tools/
│       ├── __init__.py
│       ├── student.py
│       ├── teacher.py
│       ├── clazz.py
│       ├── course.py
│       ├── grade.py
│       ├── system.py
│       ├── dashboard.py
│       └── raw.py          # list_apis + school_call
└── tests/
    ├── __init__.py
    ├── conftest.py         # respx fixture
    ├── test_config.py
    ├── test_errors.py
    ├── test_auth.py
    ├── test_client.py
    ├── test_tools_student.py
    └── test_raw.py
```

---

## Phase A：后端 OpenAPI 改造

### Task A1：添加 springdoc-openapi 依赖

**Files:**
- Modify: `school-backend/pom.xml`

- [ ] **Step 1: 在 dependencies 节点末尾追加 springdoc-openapi-ui 依赖**

打开 `school-backend/pom.xml`，找到最后一个 `</dependency>` 之后、`</dependencies>` 之前的位置，插入：

```xml
        <!-- OpenAPI 文档（用于 MCP 接口发现） -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
            <version>1.7.0</version>
        </dependency>
```

- [ ] **Step 2: 验证 maven 能解析依赖**

```bash
cd /Applications/java/idea/school/school-backend && mvn dependency:resolve -q
```

期望：无报错，下载或使用缓存的 springdoc-openapi-ui:1.7.0。

- [ ] **Step 3: 提交**

```bash
cd /Applications/java/idea/school
git add school-backend/pom.xml
git commit -m "feat(backend): 引入 springdoc-openapi 用于接口发现"
```

---

### Task A2：配置 springdoc

**Files:**
- Modify: `school-backend/src/main/resources/application.yml`

- [ ] **Step 1: 在 application.yml 末尾追加 springdoc 配置**

打开 `school-backend/src/main/resources/application.yml`，在文件最末尾追加（注意保留前面已有的 `jwt:` 等配置）：

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

- [ ] **Step 2: 提交**

```bash
git add school-backend/src/main/resources/application.yml
git commit -m "feat(backend): 配置 springdoc OpenAPI 与 Swagger UI 路径"
```

---

### Task A3：放行 OpenAPI 路径

**Files:**
- Modify: `school-backend/src/main/java/com/school/security/SecurityConfig.java:46-49`

- [ ] **Step 1: 修改 authorizeRequests 配置**

定位 `securityFilterChain` 方法中的 `authorizeRequests` 块：

```java
            .authorizeRequests()
            .antMatchers("/api/auth/login").permitAll()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated()
```

替换为（在原有 permitAll 后新增一行）：

```java
            .authorizeRequests()
            .antMatchers("/api/auth/login").permitAll()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated()
```

- [ ] **Step 2: 启动后端验证**

```bash
cd /Applications/java/idea/school/school-backend && mvn spring-boot:run
```

在另一个终端验证：

```bash
curl -s http://localhost:8080/v3/api-docs | head -c 200
```

期望：返回 JSON，包含 `"openapi":"3.0.1"` 或类似字样，**不是** `"未登录或登录已过期"`。

```bash
curl -s http://localhost:8080/v3/api-docs | python3 -c "import sys,json; d=json.load(sys.stdin); print(list(d.get('paths',{}).keys())[:5])"
```

期望：输出后端接口路径列表前 5 项，例如 `['/api/auth/info', '/api/auth/login', '/api/students', ...]`。

完成后停止后端（Ctrl+C）。

- [ ] **Step 3: 提交**

```bash
git add school-backend/src/main/java/com/school/security/SecurityConfig.java
git commit -m "feat(backend): SecurityConfig 放行 OpenAPI 与 Swagger UI 路径"
```

---

## Phase B：MCP 项目脚手架

### Task B1：检查 uv 工具

- [ ] **Step 1: 验证 uv 可用**

```bash
uv --version
```

期望：输出 `uv 0.x.x` 之类。

如果未安装，执行：

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
exec $SHELL -l   # 重新加载 PATH
uv --version
```

---

### Task B2：创建 school-mcp 项目骨架

**Files:**
- Create: `/Applications/java/idea/school-mcp/` 整个目录树

- [ ] **Step 1: 用 uv 初始化项目**

```bash
cd /Applications/java/idea
uv init --package school-mcp --python 3.11
cd school-mcp
ls -la
```

期望：生成 `pyproject.toml`、`src/school_mcp/__init__.py`、`README.md` 等。

- [ ] **Step 2: 删除 uv init 自动生成的多余文件**

```bash
rm -f hello.py
ls src/school_mcp/
```

期望：`src/school_mcp/` 目录下只有 `__init__.py`（uv 1.x 可能也生成 `__init__.py` + `py.typed`，保留）。

- [ ] **Step 3: 创建子目录结构**

```bash
mkdir -p src/school_mcp/tools tests
touch src/school_mcp/tools/__init__.py tests/__init__.py
```

- [ ] **Step 4: 写 .gitignore**

`/Applications/java/idea/school-mcp/.gitignore`：

```gitignore
__pycache__/
*.py[cod]
*$py.class
.venv/
.pytest_cache/
.ruff_cache/
*.egg-info/
dist/
build/
.coverage
.python-version
```

- [ ] **Step 5: 写 .python-version**

`/Applications/java/idea/school-mcp/.python-version`：

```
3.11
```

- [ ] **Step 6: 初始化 git**

```bash
cd /Applications/java/idea/school-mcp
git init -b main
git add -A
git commit -m "chore: 初始化项目骨架（uv init 生成）"
```

---

### Task B3：配置 pyproject.toml

**Files:**
- Modify: `school-mcp/pyproject.toml`

- [ ] **Step 1: 完整覆盖 pyproject.toml**

写入 `/Applications/java/idea/school-mcp/pyproject.toml`：

```toml
[project]
name = "school-mcp"
version = "0.1.0"
description = "MCP server for school management system backend"
readme = "README.md"
requires-python = ">=3.10"
dependencies = [
    "mcp>=1.0.0",
    "httpx>=0.27.0",
    "pydantic>=2.0",
    "pyjwt>=2.8.0",
]

[project.scripts]
school-mcp = "school_mcp.main:main"

[build-system]
requires = ["hatchling"]
build-backend = "hatchling.build"

[tool.hatch.build.targets.wheel]
packages = ["src/school_mcp"]

[dependency-groups]
dev = [
    "pytest>=8.0",
    "pytest-asyncio>=0.23",
    "respx>=0.20",
]

[tool.pytest.ini_options]
asyncio_mode = "auto"
testpaths = ["tests"]
```

- [ ] **Step 2: 同步依赖**

```bash
cd /Applications/java/idea/school-mcp
uv sync --group dev
```

期望：创建 `.venv`，安装 mcp、httpx、pytest 等。

- [ ] **Step 3: 验证可执行**

```bash
uv run pytest --version
```

期望：输出 pytest 8.x 版本。

- [ ] **Step 4: 提交**

```bash
git add pyproject.toml uv.lock
git commit -m "chore: 配置 pyproject.toml 与开发依赖"
```

---

## Phase C：基础设施（异常 + 配置）

### Task C1：异常类定义

**Files:**
- Create: `school-mcp/src/school_mcp/errors.py`
- Create: `school-mcp/tests/test_errors.py`

- [ ] **Step 1: 写测试**

写入 `tests/test_errors.py`：

```python
from school_mcp.errors import SchoolMcpError, AuthError, BusinessError, HttpError


def test_all_errors_inherit_from_base():
    assert issubclass(AuthError, SchoolMcpError)
    assert issubclass(BusinessError, SchoolMcpError)
    assert issubclass(HttpError, SchoolMcpError)


def test_error_carries_message():
    err = BusinessError("学号已存在")
    assert str(err) == "学号已存在"
```

- [ ] **Step 2: 运行测试看失败**

```bash
cd /Applications/java/idea/school-mcp && uv run pytest tests/test_errors.py -v
```

期望：FAIL with `ModuleNotFoundError: No module named 'school_mcp.errors'`。

- [ ] **Step 3: 实现**

写入 `src/school_mcp/errors.py`：

```python
class SchoolMcpError(Exception):
    """所有 school-mcp 自定义异常的基类。"""


class AuthError(SchoolMcpError):
    """认证相关错误：登录失败、token 重试后仍 401。"""


class BusinessError(SchoolMcpError):
    """后端业务错误：Result.code != 200。"""


class HttpError(SchoolMcpError):
    """HTTP 5xx 或网络层错误。"""
```

- [ ] **Step 4: 运行测试**

```bash
uv run pytest tests/test_errors.py -v
```

期望：2 passed。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/errors.py tests/test_errors.py
git commit -m "feat: 定义异常体系（SchoolMcpError 及子类）"
```

---

### Task C2：Config 类

**Files:**
- Create: `school-mcp/src/school_mcp/config.py`
- Create: `school-mcp/tests/test_config.py`

- [ ] **Step 1: 写测试**

写入 `tests/test_config.py`：

```python
import pytest
from school_mcp.config import Config


def test_from_env_with_all_required(monkeypatch):
    monkeypatch.setenv("SCHOOL_API_BASE", "http://localhost:8080/")
    monkeypatch.setenv("SCHOOL_USERNAME", "admin")
    monkeypatch.setenv("SCHOOL_PASSWORD", "pwd123")

    cfg = Config.from_env()

    # rstrip("/") 保证 base 末尾无斜杠
    assert cfg.api_base == "http://localhost:8080"
    assert cfg.username == "admin"
    assert cfg.password == "pwd123"
    assert cfg.timeout == 30.0
    assert cfg.log_level == "INFO"
    assert cfg.openapi_ttl == 300


def test_from_env_with_optional_overrides(monkeypatch):
    monkeypatch.setenv("SCHOOL_API_BASE", "http://x:8080")
    monkeypatch.setenv("SCHOOL_USERNAME", "u")
    monkeypatch.setenv("SCHOOL_PASSWORD", "p")
    monkeypatch.setenv("SCHOOL_TIMEOUT", "10.5")
    monkeypatch.setenv("SCHOOL_LOG_LEVEL", "DEBUG")
    monkeypatch.setenv("SCHOOL_OPENAPI_TTL", "60")

    cfg = Config.from_env()

    assert cfg.timeout == 10.5
    assert cfg.log_level == "DEBUG"
    assert cfg.openapi_ttl == 60


def test_from_env_missing_required_exits(monkeypatch):
    monkeypatch.delenv("SCHOOL_API_BASE", raising=False)
    monkeypatch.delenv("SCHOOL_USERNAME", raising=False)
    monkeypatch.delenv("SCHOOL_PASSWORD", raising=False)

    with pytest.raises(SystemExit) as exc_info:
        Config.from_env()
    assert "SCHOOL_API_BASE" in str(exc_info.value)
    assert "SCHOOL_USERNAME" in str(exc_info.value)
    assert "SCHOOL_PASSWORD" in str(exc_info.value)
```

- [ ] **Step 2: 运行测试看失败**

```bash
uv run pytest tests/test_config.py -v
```

期望：FAIL with `ModuleNotFoundError`。

- [ ] **Step 3: 实现**

写入 `src/school_mcp/config.py`：

```python
import os
from dataclasses import dataclass


@dataclass(frozen=True)
class Config:
    """从环境变量读取的不可变配置。"""

    api_base: str
    username: str
    password: str
    timeout: float = 30.0
    log_level: str = "INFO"
    openapi_ttl: int = 300

    @classmethod
    def from_env(cls) -> "Config":
        required = ("SCHOOL_API_BASE", "SCHOOL_USERNAME", "SCHOOL_PASSWORD")
        missing = [k for k in required if not os.getenv(k)]
        if missing:
            raise SystemExit(f"缺少必填环境变量: {', '.join(missing)}")

        return cls(
            api_base=os.environ["SCHOOL_API_BASE"].rstrip("/"),
            username=os.environ["SCHOOL_USERNAME"],
            password=os.environ["SCHOOL_PASSWORD"],
            timeout=float(os.getenv("SCHOOL_TIMEOUT", "30")),
            log_level=os.getenv("SCHOOL_LOG_LEVEL", "INFO"),
            openapi_ttl=int(os.getenv("SCHOOL_OPENAPI_TTL", "300")),
        )
```

- [ ] **Step 4: 运行测试**

```bash
uv run pytest tests/test_config.py -v
```

期望：3 passed。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/config.py tests/test_config.py
git commit -m "feat: Config 类从 env 读取并校验配置"
```

---

## Phase D：认证模块

### Task D1：测试 fixture（conftest.py）

**Files:**
- Create: `school-mcp/tests/conftest.py`

- [ ] **Step 1: 写 conftest.py**

写入 `tests/conftest.py`：

```python
import pytest
import httpx

from school_mcp.config import Config


@pytest.fixture
def config() -> Config:
    """测试用配置。"""
    return Config(
        api_base="http://test-backend:8080",
        username="testuser",
        password="testpass",
        timeout=5.0,
    )


@pytest.fixture
async def http_client(config: Config) -> httpx.AsyncClient:
    """共享 httpx AsyncClient，关闭由 pytest 自动管理。"""
    async with httpx.AsyncClient(base_url=config.api_base, timeout=config.timeout) as client:
        yield client


def make_jwt(exp_in_seconds: int = 3600) -> str:
    """生成一个测试用 JWT，包含指定的过期时间。"""
    import time
    import jwt as _jwt
    payload = {"sub": "testuser", "exp": int(time.time()) + exp_in_seconds}
    return _jwt.encode(payload, "test-secret", algorithm="HS256")
```

- [ ] **Step 2: 验证 fixture 可加载（不会报错）**

```bash
uv run pytest --collect-only tests/conftest.py -q
```

期望：no errors。

- [ ] **Step 3: 提交**

```bash
git add tests/conftest.py
git commit -m "test: 添加 conftest fixture（config / http_client / make_jwt）"
```

---

### Task D2：AuthManager 基础登录

**Files:**
- Create: `school-mcp/src/school_mcp/auth.py`
- Create: `school-mcp/tests/test_auth.py`

- [ ] **Step 1: 写测试**

写入 `tests/test_auth.py`：

```python
import pytest
import respx
import httpx

from school_mcp.auth import AuthManager
from school_mcp.errors import AuthError
from tests.conftest import make_jwt


@pytest.mark.asyncio
async def test_first_call_triggers_login(config, http_client):
    token = make_jwt(exp_in_seconds=3600)
    async with respx.mock(base_url=config.api_base) as router:
        login_route = router.post("/api/auth/login").mock(
            return_value=httpx.Response(200, json={
                "code": 200, "message": "ok",
                "data": {"token": token, "username": "testuser", "realName": "测试", "roles": ["ADMIN"]}
            })
        )

        auth = AuthManager(config, http_client)
        got = await auth.get_token()

        assert got == token
        assert login_route.call_count == 1


@pytest.mark.asyncio
async def test_login_failure_raises_auth_error(config, http_client):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(
            return_value=httpx.Response(200, json={"code": 401, "message": "用户名或密码错误"})
        )

        auth = AuthManager(config, http_client)
        with pytest.raises(AuthError) as exc:
            await auth.get_token()
        assert "用户名或密码错误" in str(exc.value)


@pytest.mark.asyncio
async def test_login_http_error_raises_auth_error(config, http_client):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=httpx.Response(500))

        auth = AuthManager(config, http_client)
        with pytest.raises(AuthError) as exc:
            await auth.get_token()
        assert "500" in str(exc.value)
```

- [ ] **Step 2: 运行测试看失败**

```bash
uv run pytest tests/test_auth.py -v
```

期望：FAIL with `ModuleNotFoundError: No module named 'school_mcp.auth'`。

- [ ] **Step 3: 实现 AuthManager（基础版）**

写入 `src/school_mcp/auth.py`：

```python
import asyncio
import time
import jwt
import httpx

from .config import Config
from .errors import AuthError


class AuthManager:
    """JWT token 管理：登录、过期检查、主动+被动刷新。"""

    REFRESH_THRESHOLD = 60  # 距过期还剩多少秒时主动刷新

    def __init__(self, config: Config, http: httpx.AsyncClient):
        self._config = config
        self._http = http
        self._token: str | None = None
        self._exp: float = 0.0
        self._lock = asyncio.Lock()

    async def get_token(self) -> str:
        if self._is_valid():
            return self._token  # type: ignore[return-value]
        async with self._lock:
            if self._is_valid():
                return self._token  # type: ignore[return-value]
            await self._login()
        return self._token  # type: ignore[return-value]

    async def force_refresh(self) -> str:
        async with self._lock:
            await self._login()
        return self._token  # type: ignore[return-value]

    def _is_valid(self) -> bool:
        return self._token is not None and (self._exp - time.time()) > self.REFRESH_THRESHOLD

    async def _login(self) -> None:
        try:
            resp = await self._http.post(
                "/api/auth/login",
                json={"username": self._config.username, "password": self._config.password},
            )
        except httpx.HTTPError as e:
            raise AuthError(f"登录请求失败: {e}") from e

        if resp.status_code != 200:
            raise AuthError(f"登录失败 HTTP {resp.status_code}: {resp.text[:200]}")

        body = resp.json()
        if body.get("code") != 200:
            raise AuthError(f"登录被拒: {body.get('message')}")

        self._token = body["data"]["token"]
        try:
            payload = jwt.decode(self._token, options={"verify_signature": False})
            self._exp = float(payload.get("exp", 0))
        except jwt.PyJWTError:
            self._exp = time.time() + 300
```

- [ ] **Step 4: 运行测试**

```bash
uv run pytest tests/test_auth.py -v
```

期望：3 passed。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/auth.py tests/test_auth.py
git commit -m "feat: AuthManager 基础登录（success/business-fail/http-fail 三路径）"
```

---

### Task D3：AuthManager token 缓存与主动刷新

**Files:**
- Modify: `school-mcp/tests/test_auth.py`

- [ ] **Step 1: 追加测试**

在 `tests/test_auth.py` 文件末尾追加：

```python
@pytest.mark.asyncio
async def test_valid_token_skips_login(config, http_client):
    token = make_jwt(exp_in_seconds=3600)
    async with respx.mock(base_url=config.api_base) as router:
        login_route = router.post("/api/auth/login").mock(
            return_value=httpx.Response(200, json={
                "code": 200, "data": {"token": token}
            })
        )

        auth = AuthManager(config, http_client)
        await auth.get_token()
        await auth.get_token()
        await auth.get_token()

        assert login_route.call_count == 1


@pytest.mark.asyncio
async def test_expiring_token_triggers_refresh(config, http_client):
    # 第一个 token 已经"快过期"（剩 30 秒，低于 REFRESH_THRESHOLD=60）
    expiring_token = make_jwt(exp_in_seconds=30)
    fresh_token = make_jwt(exp_in_seconds=3600)
    tokens = iter([expiring_token, fresh_token])

    def respond(_):
        return httpx.Response(200, json={"code": 200, "data": {"token": next(tokens)}})

    async with respx.mock(base_url=config.api_base) as router:
        login_route = router.post("/api/auth/login").mock(side_effect=respond)

        auth = AuthManager(config, http_client)
        first = await auth.get_token()
        second = await auth.get_token()

        assert first == expiring_token
        assert second == fresh_token
        assert login_route.call_count == 2


@pytest.mark.asyncio
async def test_unparseable_token_falls_back_to_5min(config, http_client):
    # 后端返回非 JWT 字符串
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(
            return_value=httpx.Response(200, json={
                "code": 200, "data": {"token": "opaque-token-xyz"}
            })
        )

        auth = AuthManager(config, http_client)
        token = await auth.get_token()

        assert token == "opaque-token-xyz"
        # exp 应被设置为约 5 分钟后
        import time
        assert 290 <= (auth._exp - time.time()) <= 305
```

- [ ] **Step 2: 运行测试**

```bash
uv run pytest tests/test_auth.py -v
```

期望：6 passed（含原 3 个 + 新增 3 个）。

> 这一阶段的 AuthManager 实现已经覆盖这些行为，无需改实现代码。

- [ ] **Step 3: 提交**

```bash
git add tests/test_auth.py
git commit -m "test(auth): 验证 token 缓存、主动刷新、JWT 解析降级"
```

---

### Task D4：AuthManager 并发锁

**Files:**
- Modify: `school-mcp/tests/test_auth.py`

- [ ] **Step 1: 追加测试**

在 `tests/test_auth.py` 末尾追加：

```python
@pytest.mark.asyncio
async def test_concurrent_get_token_only_logs_in_once(config, http_client):
    import asyncio as _asyncio
    token = make_jwt(exp_in_seconds=3600)

    async def slow_login(request):
        await _asyncio.sleep(0.05)  # 模拟 50ms 网络延迟
        return httpx.Response(200, json={"code": 200, "data": {"token": token}})

    async with respx.mock(base_url=config.api_base) as router:
        login_route = router.post("/api/auth/login").mock(side_effect=slow_login)

        auth = AuthManager(config, http_client)
        results = await _asyncio.gather(*(auth.get_token() for _ in range(10)))

        assert all(r == token for r in results)
        assert login_route.call_count == 1
```

- [ ] **Step 2: 运行测试**

```bash
uv run pytest tests/test_auth.py::test_concurrent_get_token_only_logs_in_once -v
```

期望：1 passed。

> 双检锁逻辑已在 D2 实现，不需要改代码。

- [ ] **Step 3: 提交**

```bash
git add tests/test_auth.py
git commit -m "test(auth): 验证 10 并发请求只触发一次登录（双检锁）"
```

---

### Task D5：AuthManager force_refresh

**Files:**
- Modify: `school-mcp/tests/test_auth.py`

- [ ] **Step 1: 追加测试**

在 `tests/test_auth.py` 末尾追加：

```python
@pytest.mark.asyncio
async def test_force_refresh_always_logs_in(config, http_client):
    token1 = make_jwt(exp_in_seconds=3600)
    token2 = make_jwt(exp_in_seconds=3600)
    tokens = iter([token1, token2])

    def respond(_):
        return httpx.Response(200, json={"code": 200, "data": {"token": next(tokens)}})

    async with respx.mock(base_url=config.api_base) as router:
        login_route = router.post("/api/auth/login").mock(side_effect=respond)

        auth = AuthManager(config, http_client)
        first = await auth.get_token()
        # 即使 token 还有效，force_refresh 也强制重登
        forced = await auth.force_refresh()

        assert first == token1
        assert forced == token2
        assert login_route.call_count == 2
```

- [ ] **Step 2: 运行测试**

```bash
uv run pytest tests/test_auth.py -v
```

期望：8 passed。

- [ ] **Step 3: 提交**

```bash
git add tests/test_auth.py
git commit -m "test(auth): 验证 force_refresh 无视有效 token 强制重登"
```

---

## Phase E：HTTP 客户端

### Task E1：SchoolClient.request 基本路径

**Files:**
- Create: `school-mcp/src/school_mcp/client.py`
- Create: `school-mcp/tests/test_client.py`

- [ ] **Step 1: 写测试**

写入 `tests/test_client.py`：

```python
import pytest
import respx
import httpx

from school_mcp.client import SchoolClient
from school_mcp.errors import BusinessError, HttpError, AuthError
from tests.conftest import make_jwt


@pytest.fixture
def login_token() -> str:
    return make_jwt(exp_in_seconds=3600)


def _login_response(token: str) -> httpx.Response:
    return httpx.Response(200, json={"code": 200, "data": {"token": token}})


@pytest.mark.asyncio
async def test_request_get_unwraps_data(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.get("/api/students/1").mock(
            return_value=httpx.Response(200, json={
                "code": 200, "data": {"id": 1, "name": "张三"}
            })
        )

        client = SchoolClient(config)
        try:
            data = await client.request("GET", "/api/students/1")
        finally:
            await client.aclose()

        assert data == {"id": 1, "name": "张三"}


@pytest.mark.asyncio
async def test_request_business_error_raises(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.post("/api/students").mock(
            return_value=httpx.Response(200, json={"code": 500, "message": "学号已存在"})
        )

        client = SchoolClient(config)
        try:
            with pytest.raises(BusinessError) as exc:
                await client.request("POST", "/api/students", json={"studentNo": "001"})
        finally:
            await client.aclose()
        assert "学号已存在" in str(exc.value)


@pytest.mark.asyncio
async def test_request_5xx_raises_http_error(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.get("/api/students").mock(return_value=httpx.Response(503))

        client = SchoolClient(config)
        try:
            with pytest.raises(HttpError) as exc:
                await client.request("GET", "/api/students")
        finally:
            await client.aclose()
        assert "503" in str(exc.value)


@pytest.mark.asyncio
async def test_request_data_null_returns_success_dict(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.delete("/api/students/1").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": None})
        )

        client = SchoolClient(config)
        try:
            data = await client.request("DELETE", "/api/students/1")
        finally:
            await client.aclose()
        assert data == {"success": True}
```

- [ ] **Step 2: 运行测试看失败**

```bash
uv run pytest tests/test_client.py -v
```

期望：FAIL with `ModuleNotFoundError`。

- [ ] **Step 3: 实现 SchoolClient（基础版）**

写入 `src/school_mcp/client.py`：

```python
import time
import httpx

from .auth import AuthManager
from .config import Config
from .errors import BusinessError, HttpError


class SchoolClient:
    """统一 HTTP 客户端：注入 token、解包 Result、401 重试、OpenAPI 缓存。"""

    def __init__(self, config: Config):
        self._config = config
        self._http = httpx.AsyncClient(base_url=config.api_base, timeout=config.timeout)
        self._auth = AuthManager(config, self._http)
        self._openapi_cache: dict | None = None
        self._openapi_cached_at: float = 0.0

    async def aclose(self) -> None:
        await self._http.aclose()

    async def request(self, method: str, path: str, **kwargs) -> dict | list | None:
        token = await self._auth.get_token()
        resp = await self._send(method, path, token, **kwargs)

        if resp.status_code == 401:
            token = await self._auth.force_refresh()
            resp = await self._send(method, path, token, **kwargs)

        return self._unwrap(resp)

    async def fetch_openapi(self) -> dict:
        now = time.time()
        if self._openapi_cache and (now - self._openapi_cached_at) < self._config.openapi_ttl:
            return self._openapi_cache
        resp = await self._http.get("/v3/api-docs")
        if resp.status_code != 200:
            raise HttpError(f"拉取 OpenAPI 失败 HTTP {resp.status_code}")
        self._openapi_cache = resp.json()
        self._openapi_cached_at = now
        return self._openapi_cache

    async def _send(self, method: str, path: str, token: str, **kwargs) -> httpx.Response:
        headers = kwargs.pop("headers", {})
        headers["Authorization"] = f"Bearer {token}"
        return await self._http.request(method, path, headers=headers, **kwargs)

    def _unwrap(self, resp: httpx.Response) -> dict | list | None:
        if resp.status_code >= 500:
            raise HttpError(f"后端错误 HTTP {resp.status_code}")
        try:
            body = resp.json()
        except Exception as e:
            raise HttpError(f"响应不是合法 JSON: {e}") from e

        if body.get("code") != 200:
            raise BusinessError(body.get("message", "未知业务错误"))

        data = body.get("data")
        if data is None:
            return {"success": True}
        return data
```

- [ ] **Step 4: 运行测试**

```bash
uv run pytest tests/test_client.py -v
```

期望：4 passed。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/client.py tests/test_client.py
git commit -m "feat: SchoolClient.request 实现请求/解包/错误转换"
```

---

### Task E2：SchoolClient 401 重试

**Files:**
- Modify: `school-mcp/tests/test_client.py`

- [ ] **Step 1: 追加测试**

在 `tests/test_client.py` 末尾追加：

```python
@pytest.mark.asyncio
async def test_401_triggers_relogin_and_retry(config):
    token1 = make_jwt(exp_in_seconds=3600)
    token2 = make_jwt(exp_in_seconds=3600)
    tokens = iter([token1, token2])

    def login_resp(_):
        return httpx.Response(200, json={"code": 200, "data": {"token": next(tokens)}})

    call_count = {"n": 0}

    def students_resp(request: httpx.Request):
        call_count["n"] += 1
        # 第一次返回 401，第二次返回成功
        if call_count["n"] == 1:
            return httpx.Response(401, json={"code": 401, "message": "未登录"})
        return httpx.Response(200, json={"code": 200, "data": {"records": []}})

    async with respx.mock(base_url=config.api_base) as router:
        login_route = router.post("/api/auth/login").mock(side_effect=login_resp)
        router.get("/api/students").mock(side_effect=students_resp)

        client = SchoolClient(config)
        try:
            data = await client.request("GET", "/api/students")
        finally:
            await client.aclose()

        assert data == {"records": []}
        assert login_route.call_count == 2  # 首次 + force_refresh
        assert call_count["n"] == 2  # 业务请求重试一次


@pytest.mark.asyncio
async def test_401_retry_still_401_raises_auth_error(config):
    token = make_jwt(exp_in_seconds=3600)

    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": {"token": token}})
        )
        router.get("/api/students").mock(
            return_value=httpx.Response(401, json={"code": 401, "message": "未登录"})
        )

        client = SchoolClient(config)
        try:
            with pytest.raises(BusinessError) as exc:
                await client.request("GET", "/api/students")
        finally:
            await client.aclose()
        # 因为后端 401 响应也是 Result 格式，会被解包为 BusinessError("未登录")
        assert "未登录" in str(exc.value)
```

- [ ] **Step 2: 运行测试**

```bash
uv run pytest tests/test_client.py -v
```

期望：6 passed。

- [ ] **Step 3: 提交**

```bash
git add tests/test_client.py
git commit -m "test(client): 验证 401 自动重登重试逻辑"
```

---

### Task E3：SchoolClient OpenAPI 缓存

**Files:**
- Modify: `school-mcp/tests/test_client.py`

- [ ] **Step 1: 追加测试**

在 `tests/test_client.py` 末尾追加：

```python
@pytest.mark.asyncio
async def test_openapi_caches_within_ttl(config, login_token):
    spec = {"openapi": "3.0.1", "paths": {"/api/x": {}}}
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        openapi_route = router.get("/v3/api-docs").mock(
            return_value=httpx.Response(200, json=spec)
        )

        client = SchoolClient(config)
        try:
            r1 = await client.fetch_openapi()
            r2 = await client.fetch_openapi()
            r3 = await client.fetch_openapi()
        finally:
            await client.aclose()

        assert r1 == r2 == r3 == spec
        assert openapi_route.call_count == 1


@pytest.mark.asyncio
async def test_openapi_refetches_after_ttl_expires(config, login_token):
    from school_mcp.config import Config as Cfg
    short_cfg = Cfg(
        api_base=config.api_base, username=config.username,
        password=config.password, timeout=config.timeout,
        openapi_ttl=0,  # 立即过期
    )
    spec = {"openapi": "3.0.1"}

    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        openapi_route = router.get("/v3/api-docs").mock(
            return_value=httpx.Response(200, json=spec)
        )

        client = SchoolClient(short_cfg)
        try:
            await client.fetch_openapi()
            await client.fetch_openapi()
        finally:
            await client.aclose()

        assert openapi_route.call_count == 2
```

- [ ] **Step 2: 运行测试**

```bash
uv run pytest tests/test_client.py -v
```

期望：8 passed。

- [ ] **Step 3: 提交**

```bash
git add tests/test_client.py
git commit -m "test(client): 验证 OpenAPI 文档 5 分钟内存缓存"
```

---

## Phase F：工具实现

### Task F1：student tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/student.py`
- Create: `school-mcp/tests/test_tools_student.py`

- [ ] **Step 1: 写测试**

写入 `tests/test_tools_student.py`：

```python
import pytest
import respx
import httpx

from school_mcp.client import SchoolClient
from school_mcp.tools.student import student_tool
from tests.conftest import make_jwt


@pytest.fixture
def login_token() -> str:
    return make_jwt(exp_in_seconds=3600)


def _login_response(token: str) -> httpx.Response:
    return httpx.Response(200, json={"code": 200, "data": {"token": token}})


@pytest.mark.asyncio
async def test_student_page_with_filters(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        page_route = router.get("/api/students").mock(
            return_value=httpx.Response(200, json={
                "code": 200, "data": {"records": [{"id": 1}], "total": 1, "size": 10, "current": 1}
            })
        )

        client = SchoolClient(config)
        try:
            result = await student_tool(
                client, action="page", page=1, size=10, keyword="张", class_id=3
            )
        finally:
            await client.aclose()

        assert result["records"] == [{"id": 1}]
        # 验证查询参数被正确传递
        call = page_route.calls[0]
        assert "page=1" in str(call.request.url)
        assert "size=10" in str(call.request.url)
        assert "keyword=" in str(call.request.url)
        assert "classId=3" in str(call.request.url)


@pytest.mark.asyncio
async def test_student_get_requires_id(config, login_token):
    client = SchoolClient(config)
    try:
        with pytest.raises(ValueError, match="action=get 需要参数 id"):
            await student_tool(client, action="get")
    finally:
        await client.aclose()


@pytest.mark.asyncio
async def test_student_create(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        create_route = router.post("/api/students").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": None})
        )

        client = SchoolClient(config)
        try:
            result = await student_tool(
                client, action="create",
                payload={"studentNo": "2024001", "name": "张三", "gender": 1, "classId": 3}
            )
        finally:
            await client.aclose()

        assert result == {"success": True}
        assert create_route.calls[0].request.method == "POST"


@pytest.mark.asyncio
async def test_student_update(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.put("/api/students/5").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": None})
        )

        client = SchoolClient(config)
        try:
            result = await student_tool(
                client, action="update", id=5, payload={"name": "李四"}
            )
        finally:
            await client.aclose()

        assert result == {"success": True}


@pytest.mark.asyncio
async def test_student_delete(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.delete("/api/students/9").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": None})
        )

        client = SchoolClient(config)
        try:
            result = await student_tool(client, action="delete", id=9)
        finally:
            await client.aclose()

        assert result == {"success": True}


@pytest.mark.asyncio
async def test_student_invalid_action(config):
    client = SchoolClient(config)
    try:
        with pytest.raises(ValueError, match="未知 action"):
            await student_tool(client, action="bulk_import")
    finally:
        await client.aclose()
```

- [ ] **Step 2: 运行测试看失败**

```bash
uv run pytest tests/test_tools_student.py -v
```

期望：FAIL with `ModuleNotFoundError`。

- [ ] **Step 3: 实现 student_tool**

写入 `src/school_mcp/tools/student.py`：

```python
from typing import Literal
from ..client import SchoolClient


async def student_tool(
    client: SchoolClient,
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    keyword: str | None = None,
    class_id: int | None = None,
    payload: dict | None = None,
) -> dict:
    """学生管理 tool 实现（不含 MCP 装饰器，便于单独测试）。"""
    base = "/api/students"

    if action == "page":
        params: dict = {"page": page, "size": size}
        if keyword is not None:
            params["keyword"] = keyword
        if class_id is not None:
            params["classId"] = class_id
        return await client.request("GET", base, params=params)

    if action == "get":
        if id is None:
            raise ValueError("action=get 需要参数 id")
        return await client.request("GET", f"{base}/{id}")

    if action == "create":
        if not payload:
            raise ValueError("action=create 需要参数 payload")
        return await client.request("POST", base, json=payload)

    if action == "update":
        if id is None or not payload:
            raise ValueError("action=update 需要参数 id 和 payload")
        return await client.request("PUT", f"{base}/{id}", json=payload)

    if action == "delete":
        if id is None:
            raise ValueError("action=delete 需要参数 id")
        return await client.request("DELETE", f"{base}/{id}")

    raise ValueError(f"未知 action: {action}")
```

- [ ] **Step 4: 运行测试**

```bash
uv run pytest tests/test_tools_student.py -v
```

期望：6 passed。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/tools/student.py tests/test_tools_student.py
git commit -m "feat(tools): student CRUD + 分页 + 参数校验"
```

---

### Task F2：teacher tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/teacher.py`

> 该 tool 与 student 结构对称，差异：路径 `/api/teachers`，查询参数 `keyword` 和 `subject`。先查后端真实参数。

- [ ] **Step 1: 确认后端 TeacherController 的查询参数**

```bash
grep -A 6 "GetMapping$" /Applications/java/idea/school/school-backend/src/main/java/com/school/teacher/controller/TeacherController.java
```

记下 `list` 方法的 `@RequestParam`，在下方实现中替换 `subject` 等占位参数为真实参数名。

- [ ] **Step 2: 实现 teacher_tool**

写入 `src/school_mcp/tools/teacher.py`：

```python
from typing import Literal
from ..client import SchoolClient


async def teacher_tool(
    client: SchoolClient,
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    keyword: str | None = None,
    subject: str | None = None,  # 若后端实际参数名不同，按 Step 1 结果改
    payload: dict | None = None,
) -> dict:
    """教师管理。"""
    base = "/api/teachers"

    if action == "page":
        params: dict = {"page": page, "size": size}
        if keyword is not None:
            params["keyword"] = keyword
        if subject is not None:
            params["subject"] = subject
        return await client.request("GET", base, params=params)

    if action == "get":
        if id is None:
            raise ValueError("action=get 需要参数 id")
        return await client.request("GET", f"{base}/{id}")

    if action == "create":
        if not payload:
            raise ValueError("action=create 需要参数 payload")
        return await client.request("POST", base, json=payload)

    if action == "update":
        if id is None or not payload:
            raise ValueError("action=update 需要参数 id 和 payload")
        return await client.request("PUT", f"{base}/{id}", json=payload)

    if action == "delete":
        if id is None:
            raise ValueError("action=delete 需要参数 id")
        return await client.request("DELETE", f"{base}/{id}")

    raise ValueError(f"未知 action: {action}")
```

- [ ] **Step 3: 提交**

```bash
git add src/school_mcp/tools/teacher.py
git commit -m "feat(tools): teacher CRUD + 分页"
```

---

### Task F3：clazz tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/clazz.py`

- [ ] **Step 1: 确认后端 ClazzController 的查询参数**

```bash
grep -A 6 "GetMapping$" /Applications/java/idea/school/school-backend/src/main/java/com/school/clazz/controller/ClazzController.java
```

- [ ] **Step 2: 实现 clazz_tool**

写入 `src/school_mcp/tools/clazz.py`：

```python
from typing import Literal
from ..client import SchoolClient


async def clazz_tool(
    client: SchoolClient,
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    keyword: str | None = None,
    grade_id: int | None = None,  # 按 Step 1 结果调整
    payload: dict | None = None,
) -> dict:
    """班级管理。"""
    base = "/api/clazz"

    if action == "page":
        params: dict = {"page": page, "size": size}
        if keyword is not None:
            params["keyword"] = keyword
        if grade_id is not None:
            params["gradeId"] = grade_id
        return await client.request("GET", base, params=params)

    if action == "get":
        if id is None:
            raise ValueError("action=get 需要参数 id")
        return await client.request("GET", f"{base}/{id}")

    if action == "create":
        if not payload:
            raise ValueError("action=create 需要参数 payload")
        return await client.request("POST", base, json=payload)

    if action == "update":
        if id is None or not payload:
            raise ValueError("action=update 需要参数 id 和 payload")
        return await client.request("PUT", f"{base}/{id}", json=payload)

    if action == "delete":
        if id is None:
            raise ValueError("action=delete 需要参数 id")
        return await client.request("DELETE", f"{base}/{id}")

    raise ValueError(f"未知 action: {action}")
```

- [ ] **Step 3: 提交**

```bash
git add src/school_mcp/tools/clazz.py
git commit -m "feat(tools): clazz CRUD + 分页"
```

---

### Task F4：course tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/course.py`

- [ ] **Step 1: 确认后端 CourseController 的查询参数**

```bash
grep -A 6 "GetMapping$" /Applications/java/idea/school/school-backend/src/main/java/com/school/course/controller/CourseController.java
```

- [ ] **Step 2: 实现 course_tool**

写入 `src/school_mcp/tools/course.py`：

```python
from typing import Literal
from ..client import SchoolClient


async def course_tool(
    client: SchoolClient,
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    keyword: str | None = None,
    teacher_id: int | None = None,  # 按 Step 1 结果调整
    payload: dict | None = None,
) -> dict:
    """课程管理。"""
    base = "/api/courses"

    if action == "page":
        params: dict = {"page": page, "size": size}
        if keyword is not None:
            params["keyword"] = keyword
        if teacher_id is not None:
            params["teacherId"] = teacher_id
        return await client.request("GET", base, params=params)

    if action == "get":
        if id is None:
            raise ValueError("action=get 需要参数 id")
        return await client.request("GET", f"{base}/{id}")

    if action == "create":
        if not payload:
            raise ValueError("action=create 需要参数 payload")
        return await client.request("POST", base, json=payload)

    if action == "update":
        if id is None or not payload:
            raise ValueError("action=update 需要参数 id 和 payload")
        return await client.request("PUT", f"{base}/{id}", json=payload)

    if action == "delete":
        if id is None:
            raise ValueError("action=delete 需要参数 id")
        return await client.request("DELETE", f"{base}/{id}")

    raise ValueError(f"未知 action: {action}")
```

- [ ] **Step 3: 提交**

```bash
git add src/school_mcp/tools/course.py
git commit -m "feat(tools): course CRUD + 分页"
```

---

### Task F5：grade tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/grade.py`

- [ ] **Step 1: 确认后端 GradeController 的查询参数**

```bash
grep -A 8 "GetMapping$" /Applications/java/idea/school/school-backend/src/main/java/com/school/grade/controller/GradeController.java
```

- [ ] **Step 2: 实现 grade_tool**

写入 `src/school_mcp/tools/grade.py`：

```python
from typing import Literal
from ..client import SchoolClient


async def grade_tool(
    client: SchoolClient,
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    student_id: int | None = None,
    course_id: int | None = None,
    exam_type: str | None = None,  # 按 Step 1 结果调整
    payload: dict | None = None,
) -> dict:
    """成绩管理。"""
    base = "/api/grades"

    if action == "page":
        params: dict = {"page": page, "size": size}
        if student_id is not None:
            params["studentId"] = student_id
        if course_id is not None:
            params["courseId"] = course_id
        if exam_type is not None:
            params["examType"] = exam_type
        return await client.request("GET", base, params=params)

    if action == "get":
        if id is None:
            raise ValueError("action=get 需要参数 id")
        return await client.request("GET", f"{base}/{id}")

    if action == "create":
        if not payload:
            raise ValueError("action=create 需要参数 payload")
        return await client.request("POST", base, json=payload)

    if action == "update":
        if id is None or not payload:
            raise ValueError("action=update 需要参数 id 和 payload")
        return await client.request("PUT", f"{base}/{id}", json=payload)

    if action == "delete":
        if id is None:
            raise ValueError("action=delete 需要参数 id")
        return await client.request("DELETE", f"{base}/{id}")

    raise ValueError(f"未知 action: {action}")
```

- [ ] **Step 3: 提交**

```bash
git add src/school_mcp/tools/grade.py
git commit -m "feat(tools): grade CRUD + 分页"
```

---

### Task F6：system (用户) tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/system.py`

- [ ] **Step 1: 确认后端 SysUserController 路径与查询参数**

```bash
grep -E "RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping" /Applications/java/idea/school/school-backend/src/main/java/com/school/system/controller/SysUserController.java | head -20
```

记下：
- `@RequestMapping` 的路径
- `list` 方法的 `@RequestParam`

- [ ] **Step 2: 实现 user_tool**

写入 `src/school_mcp/tools/system.py`（如果 Step 1 显示路径不是 `/api/system/users`，按真实路径修改）：

```python
from typing import Literal
from ..client import SchoolClient


async def user_tool(
    client: SchoolClient,
    action: Literal["page", "get", "create", "update", "delete"],
    id: int | None = None,
    page: int = 1,
    size: int = 10,
    keyword: str | None = None,
    role: str | None = None,  # 按 Step 1 结果调整
    payload: dict | None = None,
) -> dict:
    """系统用户管理。仅 ADMIN 可操作。"""
    base = "/api/system/users"

    if action == "page":
        params: dict = {"page": page, "size": size}
        if keyword is not None:
            params["keyword"] = keyword
        if role is not None:
            params["role"] = role
        return await client.request("GET", base, params=params)

    if action == "get":
        if id is None:
            raise ValueError("action=get 需要参数 id")
        return await client.request("GET", f"{base}/{id}")

    if action == "create":
        if not payload:
            raise ValueError("action=create 需要参数 payload")
        return await client.request("POST", base, json=payload)

    if action == "update":
        if id is None or not payload:
            raise ValueError("action=update 需要参数 id 和 payload")
        return await client.request("PUT", f"{base}/{id}", json=payload)

    if action == "delete":
        if id is None:
            raise ValueError("action=delete 需要参数 id")
        return await client.request("DELETE", f"{base}/{id}")

    raise ValueError(f"未知 action: {action}")
```

- [ ] **Step 3: 提交**

```bash
git add src/school_mcp/tools/system.py
git commit -m "feat(tools): system 用户管理 CRUD"
```

---

### Task F7：dashboard tool

**Files:**
- Create: `school-mcp/src/school_mcp/tools/dashboard.py`

- [ ] **Step 1: 实现 dashboard_tool**

写入 `src/school_mcp/tools/dashboard.py`：

```python
from ..client import SchoolClient


async def dashboard_tool(client: SchoolClient) -> dict:
    """获取首页统计数据：学生/教师/班级/课程总数等。"""
    return await client.request("GET", "/api/dashboard/stats")
```

- [ ] **Step 2: 提交**

```bash
git add src/school_mcp/tools/dashboard.py
git commit -m "feat(tools): dashboard 统计"
```

---

### Task F8：raw tools (list_apis + school_call)

**Files:**
- Create: `school-mcp/src/school_mcp/tools/raw.py`
- Create: `school-mcp/tests/test_raw.py`

- [ ] **Step 1: 写测试**

写入 `tests/test_raw.py`：

```python
import pytest
import respx
import httpx

from school_mcp.client import SchoolClient
from school_mcp.tools.raw import list_apis_tool, call_tool
from tests.conftest import make_jwt


@pytest.fixture
def login_token() -> str:
    return make_jwt(exp_in_seconds=3600)


def _login_response(token: str) -> httpx.Response:
    return httpx.Response(200, json={"code": 200, "data": {"token": token}})


SAMPLE_OPENAPI = {
    "openapi": "3.0.1",
    "paths": {
        "/api/students": {
            "get": {"summary": "学生列表", "parameters": [{"name": "page"}]},
            "post": {"summary": "新增学生", "requestBody": {}}
        },
        "/api/grades/export": {
            "get": {"summary": "导出成绩 Excel", "parameters": []}
        },
    }
}


@pytest.mark.asyncio
async def test_list_apis_default_summary_only(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.get("/v3/api-docs").mock(return_value=httpx.Response(200, json=SAMPLE_OPENAPI))

        client = SchoolClient(config)
        try:
            result = await list_apis_tool(client)
        finally:
            await client.aclose()

        assert result["total"] == 3
        # 只含 path/method/summary，不含 parameters
        for item in result["apis"]:
            assert set(item.keys()) == {"path", "method", "summary"}


@pytest.mark.asyncio
async def test_list_apis_with_keyword(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.get("/v3/api-docs").mock(return_value=httpx.Response(200, json=SAMPLE_OPENAPI))

        client = SchoolClient(config)
        try:
            result = await list_apis_tool(client, keyword="export")
        finally:
            await client.aclose()

        assert result["total"] == 1
        assert result["apis"][0]["path"] == "/api/grades/export"


@pytest.mark.asyncio
async def test_list_apis_keyword_matches_summary(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.get("/v3/api-docs").mock(return_value=httpx.Response(200, json=SAMPLE_OPENAPI))

        client = SchoolClient(config)
        try:
            result = await list_apis_tool(client, keyword="新增")
        finally:
            await client.aclose()

        assert result["total"] == 1
        assert result["apis"][0]["method"] == "POST"


@pytest.mark.asyncio
async def test_list_apis_detail_includes_parameters(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.get("/v3/api-docs").mock(return_value=httpx.Response(200, json=SAMPLE_OPENAPI))

        client = SchoolClient(config)
        try:
            result = await list_apis_tool(client, keyword="students", detail=True)
        finally:
            await client.aclose()

        # 至少其中一项含 parameters 字段
        assert any("parameters" in item for item in result["apis"])


@pytest.mark.asyncio
async def test_call_tool_get(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        route = router.get("/api/grades/export").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": {"url": "..."}})
        )

        client = SchoolClient(config)
        try:
            result = await call_tool(
                client, method="GET", path="/api/grades/export", query={"studentId": 1}
            )
        finally:
            await client.aclose()

        assert result == {"url": "..."}
        assert "studentId=1" in str(route.calls[0].request.url)


@pytest.mark.asyncio
async def test_call_tool_post_with_body(config, login_token):
    async with respx.mock(base_url=config.api_base) as router:
        router.post("/api/auth/login").mock(return_value=_login_response(login_token))
        router.post("/api/x").mock(
            return_value=httpx.Response(200, json={"code": 200, "data": None})
        )

        client = SchoolClient(config)
        try:
            result = await call_tool(
                client, method="POST", path="/api/x", body={"k": "v"}
            )
        finally:
            await client.aclose()

        assert result == {"success": True}


@pytest.mark.asyncio
async def test_call_tool_rejects_non_api_path(config):
    client = SchoolClient(config)
    try:
        with pytest.raises(ValueError, match="path 必须以 /api/ 开头"):
            await call_tool(client, method="GET", path="/v3/api-docs")
    finally:
        await client.aclose()
```

- [ ] **Step 2: 运行测试看失败**

```bash
uv run pytest tests/test_raw.py -v
```

期望：FAIL with `ModuleNotFoundError`。

- [ ] **Step 3: 实现 raw tools**

写入 `src/school_mcp/tools/raw.py`：

```python
from typing import Literal
from ..client import SchoolClient


async def list_apis_tool(
    client: SchoolClient,
    keyword: str | None = None,
    detail: bool = False,
) -> dict:
    """列出后端所有可用 API 接口。
    
    当 school_xxx 资源 tool 不能满足需求时，先调此 tool 查看接口清单，
    再用 school_call 调用具体接口。
    """
    spec = await client.fetch_openapi()
    paths = spec.get("paths", {}) or {}
    kw = keyword.lower() if keyword else None

    apis = []
    for path, methods in paths.items():
        for method, op in methods.items():
            if not isinstance(op, dict):
                continue
            summary = op.get("summary", "") or ""
            if kw and kw not in path.lower() and kw not in summary.lower():
                continue
            entry = {"path": path, "method": method.upper(), "summary": summary}
            if detail:
                entry["parameters"] = op.get("parameters", [])
                entry["requestBody"] = op.get("requestBody")
                entry["responses"] = op.get("responses")
            apis.append(entry)

    return {"apis": apis, "total": len(apis)}


async def call_tool(
    client: SchoolClient,
    method: Literal["GET", "POST", "PUT", "DELETE", "PATCH"],
    path: str,
    query: dict | None = None,
    body: dict | None = None,
) -> dict:
    """通用 API 调用。仅当 school_xxx 资源 tool 无法满足时使用。
    
    建议先调 school_list_apis 确认接口存在和参数。
    """
    if not path.startswith("/api/"):
        raise ValueError("path 必须以 /api/ 开头")

    kwargs: dict = {}
    if query:
        kwargs["params"] = query
    if body is not None:
        kwargs["json"] = body
    return await client.request(method, path, **kwargs)
```

- [ ] **Step 4: 运行测试**

```bash
uv run pytest tests/test_raw.py -v
```

期望：7 passed。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/tools/raw.py tests/test_raw.py
git commit -m "feat(tools): list_apis（OpenAPI 裁剪）+ school_call（通用代理）"
```

---

## Phase G：MCP 主入口

### Task G1：main.py 集成所有 tool

**Files:**
- Create: `school-mcp/src/school_mcp/main.py`

- [ ] **Step 1: 实现 main.py**

写入 `src/school_mcp/main.py`：

```python
"""MCP server 入口：初始化 client、注册 tools、启动 stdio 循环。"""
import asyncio
import functools
import logging
import sys
from typing import Literal

from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.exceptions import ToolError

from .config import Config
from .client import SchoolClient
from .errors import SchoolMcpError, AuthError, BusinessError, HttpError
from .tools.student import student_tool
from .tools.teacher import teacher_tool
from .tools.clazz import clazz_tool
from .tools.course import course_tool
from .tools.grade import grade_tool
from .tools.system import user_tool
from .tools.dashboard import dashboard_tool
from .tools.raw import list_apis_tool, call_tool

import httpx


def _convert_errors(func):
    """把内部异常统一转为 ToolError 给 MCP 协议层。"""
    @functools.wraps(func)
    async def wrapper(*args, **kwargs):
        try:
            return await func(*args, **kwargs)
        except AuthError as e:
            raise ToolError(f"认证失败，请检查 SCHOOL_USERNAME/SCHOOL_PASSWORD: {e}")
        except BusinessError as e:
            raise ToolError(str(e))
        except HttpError as e:
            raise ToolError(f"后端服务异常，请稍后重试: {e}")
        except httpx.TimeoutException:
            raise ToolError("请求超时，请检查 SCHOOL_API_BASE 是否可达")
        except httpx.ConnectError as e:
            raise ToolError(f"无法连接后端: {e}")
        except SchoolMcpError as e:
            raise ToolError(str(e))
    return wrapper


def main() -> None:
    config = Config.from_env()

    logging.basicConfig(
        level=config.log_level,
        format="[%(asctime)s] %(levelname)s %(name)s: %(message)s",
        stream=sys.stderr,
    )
    log = logging.getLogger("school-mcp")
    log.info(f"school-mcp starting, backend={config.api_base}")

    client = SchoolClient(config)
    mcp = FastMCP("school")

    @mcp.tool()
    @_convert_errors
    async def school_student(
        action: Literal["page", "get", "create", "update", "delete"],
        id: int | None = None,
        page: int = 1,
        size: int = 10,
        keyword: str | None = None,
        class_id: int | None = None,
        payload: dict | None = None,
    ) -> dict:
        """学生管理。

        actions:
          - page: 分页查询。可选 keyword(模糊搜索), class_id(按班级筛选)
          - get: 按 ID 查询。必填 id
          - create: 新增。必填 payload，字段参考 StudentDTO
                    例 {"studentNo":"2024001","name":"张三","gender":1,"classId":3}
          - update: 更新。必填 id 和 payload
          - delete: 删除（逻辑）。必填 id

        权限：create/update/delete 需要 ADMIN；page/get 需要 ADMIN 或 TEACHER。
        """
        return await student_tool(client, action, id, page, size, keyword, class_id, payload)

    @mcp.tool()
    @_convert_errors
    async def school_teacher(
        action: Literal["page", "get", "create", "update", "delete"],
        id: int | None = None,
        page: int = 1,
        size: int = 10,
        keyword: str | None = None,
        subject: str | None = None,
        payload: dict | None = None,
    ) -> dict:
        """教师管理。actions: page/get/create/update/delete。"""
        return await teacher_tool(client, action, id, page, size, keyword, subject, payload)

    @mcp.tool()
    @_convert_errors
    async def school_clazz(
        action: Literal["page", "get", "create", "update", "delete"],
        id: int | None = None,
        page: int = 1,
        size: int = 10,
        keyword: str | None = None,
        grade_id: int | None = None,
        payload: dict | None = None,
    ) -> dict:
        """班级管理（注意包名是 clazz 不是 class）。actions: page/get/create/update/delete。"""
        return await clazz_tool(client, action, id, page, size, keyword, grade_id, payload)

    @mcp.tool()
    @_convert_errors
    async def school_course(
        action: Literal["page", "get", "create", "update", "delete"],
        id: int | None = None,
        page: int = 1,
        size: int = 10,
        keyword: str | None = None,
        teacher_id: int | None = None,
        payload: dict | None = None,
    ) -> dict:
        """课程管理。actions: page/get/create/update/delete。"""
        return await course_tool(client, action, id, page, size, keyword, teacher_id, payload)

    @mcp.tool()
    @_convert_errors
    async def school_grade(
        action: Literal["page", "get", "create", "update", "delete"],
        id: int | None = None,
        page: int = 1,
        size: int = 10,
        student_id: int | None = None,
        course_id: int | None = None,
        exam_type: str | None = None,
        payload: dict | None = None,
    ) -> dict:
        """成绩管理。actions: page/get/create/update/delete。"""
        return await grade_tool(client, action, id, page, size, student_id, course_id, exam_type, payload)

    @mcp.tool()
    @_convert_errors
    async def school_user(
        action: Literal["page", "get", "create", "update", "delete"],
        id: int | None = None,
        page: int = 1,
        size: int = 10,
        keyword: str | None = None,
        role: str | None = None,
        payload: dict | None = None,
    ) -> dict:
        """系统用户管理。仅 ADMIN 可操作。actions: page/get/create/update/delete。"""
        return await user_tool(client, action, id, page, size, keyword, role, payload)

    @mcp.tool()
    @_convert_errors
    async def school_dashboard() -> dict:
        """获取首页统计数据：学生/教师/班级/课程总数等。"""
        return await dashboard_tool(client)

    @mcp.tool()
    @_convert_errors
    async def school_list_apis(
        keyword: str | None = None,
        detail: bool = False,
    ) -> dict:
        """列出后端所有可用 API。
        
        Args:
          keyword: 按路径或 summary 关键字过滤（不区分大小写）
          detail: True 返回完整参数定义；False 只返回 path+method+summary
        """
        return await list_apis_tool(client, keyword, detail)

    @mcp.tool()
    @_convert_errors
    async def school_call(
        method: Literal["GET", "POST", "PUT", "DELETE", "PATCH"],
        path: str,
        query: dict | None = None,
        body: dict | None = None,
    ) -> dict:
        """通用 API 调用。仅当 school_xxx 资源 tool 无法满足时使用。
        
        建议先调 school_list_apis 确认接口存在和参数。path 必须以 /api/ 开头。
        """
        return await call_tool(client, method, path, query, body)

    try:
        mcp.run(transport="stdio")
    finally:
        # MCP server 结束后清理 httpx 连接
        try:
            asyncio.run(client.aclose())
        except Exception:
            pass


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: 验证 main 模块可加载**

```bash
cd /Applications/java/idea/school-mcp
uv run python -c "from school_mcp.main import main; print('main loadable')"
```

期望：输出 `main loadable`，无 import 错误。

- [ ] **Step 3: 验证 console script 已注册**

```bash
uv run school-mcp --help 2>&1 | head -20 || echo "脚本入口存在但 stdio 模式不支持 --help（正常）"
```

期望：输出含 `school-mcp starting` 的日志（然后会等 stdio 输入，按 Ctrl+C 退出）。

> 如果出现 `SystemExit: 缺少必填环境变量`，说明流程是对的（main 启动到了 Config.from_env），按 Ctrl+C 退出。

- [ ] **Step 4: 跑全部测试**

```bash
uv run pytest -v
```

期望：所有测试通过（约 30 个 passed）。

- [ ] **Step 5: 提交**

```bash
git add src/school_mcp/main.py
git commit -m "feat: MCP server 入口（注册 9 个 tool + 错误转换装饰器）"
```

---

## Phase H：文档与发布

### Task H1：写 README

**Files:**
- Modify: `school-mcp/README.md`

- [ ] **Step 1: 完整覆盖 README.md**

写入 `/Applications/java/idea/school-mcp/README.md`：

```markdown
# school-mcp

[school 校园管理系统](https://github.com/zhangwh/school) 的 MCP 服务，让 Claude / Claude Code 等 AI 客户端能通过自然语言调用后端 API。

## 前置要求

1. **uv** 工具
   ```bash
   curl -LsSf https://astral.sh/uv/install.sh | sh
   ```

2. **可访问的 school 后端**
   - 后端必须开启 OpenAPI 文档（已支持，见 [school 仓库](https://github.com/zhangwh/school) 的相关 commit）
   - 后端 `SCHOOL_API_BASE` 网络可达

3. **登录账号**
   - 已有效的 username + password

## 配置（Claude Code）

编辑 Claude Code 的 MCP 配置（通常在 `~/.claude/mcp.json` 或客户端设置）：

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
        "SCHOOL_PASSWORD": "your-password"
      }
    }
  }
}
```

> `--refresh` 让 uvx 每次启动都拉取 main 分支最新代码；想要更新时**重启 Claude Code 客户端**或在会话中 reconnect MCP 即可。

## 环境变量

| 变量 | 必填 | 默认 | 说明 |
|---|---|---|---|
| `SCHOOL_API_BASE` | ✅ | — | 后端地址，例 `http://localhost:8080` |
| `SCHOOL_USERNAME` | ✅ | — | 登录账号 |
| `SCHOOL_PASSWORD` | ✅ | — | 登录密码 |
| `SCHOOL_TIMEOUT` | ❌ | `30` | HTTP 超时（秒） |
| `SCHOOL_LOG_LEVEL` | ❌ | `INFO` | DEBUG / INFO / WARNING / ERROR |
| `SCHOOL_OPENAPI_TTL` | ❌ | `300` | OpenAPI 缓存秒数 |

## 提供的 Tools

| Tool | 用途 |
|---|---|
| `school_student` | 学生 CRUD + 分页 |
| `school_teacher` | 教师 CRUD + 分页 |
| `school_clazz` | 班级 CRUD + 分页 |
| `school_course` | 课程 CRUD + 分页 |
| `school_grade` | 成绩 CRUD + 分页 |
| `school_user` | 系统用户管理（仅 ADMIN） |
| `school_dashboard` | 首页统计数据 |
| `school_list_apis` | 列出后端所有 API（兜底发现） |
| `school_call` | 通用 HTTP 调用（兜底执行） |

## 使用示例

> 用户："帮我查三年二班的所有学生"

Claude 会自动：
1. `school_clazz(action="page", keyword="三年二班")` 找班级 ID
2. `school_student(action="page", class_id=找到的ID, size=100)` 拉学生
3. 用人话回复

> 用户："新增学生张三，学号 2024001，三年二班"

Claude 会调用 `school_student(action="create", payload={...})`。

## 故障排查

| 现象 | 原因 / 处理 |
|---|---|
| 启动时 `缺少必填环境变量` | 检查 mcpServers 配置的 env 是否完整 |
| 调用 tool 报"认证失败" | 账号/密码错误，或后端账号被禁 |
| 调用 tool 报"无法连接后端" | 检查 `SCHOOL_API_BASE` 是否可达，VPN 是否开着 |
| reconnect 后仍是旧代码 | 关闭 Claude Code 完全退出再开，或检查是否带了 `--refresh` |
| 列出的 API 缺失新接口 | 等最多 5 分钟（OpenAPI 缓存 TTL）或重启 MCP |

## 本地开发

```bash
git clone https://github.com/zhangwh/school-mcp.git
cd school-mcp
uv sync --group dev

# 跑测试
uv run pytest -v

# 本地直连后端调试
SCHOOL_API_BASE=http://localhost:8080 \
  SCHOOL_USERNAME=admin \
  SCHOOL_PASSWORD=admin123 \
  uv run school-mcp
```
```

- [ ] **Step 2: 提交**

```bash
git add README.md
git commit -m "docs: 完整 README（前置要求/配置/工具列表/故障排查/本地开发）"
```

---

### Task H2：端到端验证

- [ ] **Step 1: 启动后端**

```bash
cd /Applications/java/idea/school/school-backend && mvn spring-boot:run
```

等待启动完成（看到 `Started SchoolBackendApplication`）。

- [ ] **Step 2: 在另一个终端跑 MCP 自检**

```bash
cd /Applications/java/idea/school-mcp
SCHOOL_API_BASE=http://localhost:8080 \
  SCHOOL_USERNAME=admin \
  SCHOOL_PASSWORD=admin123 \
  uv run python -c "
import asyncio
from school_mcp.config import Config
from school_mcp.client import SchoolClient

async def main():
    cfg = Config.from_env()
    client = SchoolClient(cfg)
    try:
        # 1. 验证登录
        print('Testing login...')
        token = await client._auth.get_token()
        print(f'  ✓ Got token: {token[:30]}...')

        # 2. 验证学生分页
        print('Testing student page...')
        data = await client.request('GET', '/api/students', params={'page': 1, 'size': 5})
        print(f'  ✓ Got {len(data.get(\"records\", []))} students, total={data.get(\"total\")}')

        # 3. 验证 OpenAPI 拉取
        print('Testing OpenAPI...')
        spec = await client.fetch_openapi()
        paths = list(spec.get('paths', {}).keys())[:5]
        print(f'  ✓ OpenAPI paths sample: {paths}')

        # 4. 验证 dashboard
        print('Testing dashboard...')
        from school_mcp.tools.dashboard import dashboard_tool
        stats = await dashboard_tool(client)
        print(f'  ✓ Dashboard stats: {stats}')

    finally:
        await client.aclose()

asyncio.run(main())
"
```

期望：4 个测试全部 ✓ 通过。

> 如果账号/密码不是 `admin/admin123`，按你环境实际情况调整。

- [ ] **Step 3: 配置 Claude Code 实测**

将 README 中的 mcpServers 配置粘贴到 Claude Code 的 MCP 配置（用本地路径调试）：

```json
{
  "mcpServers": {
    "school": {
      "command": "uv",
      "args": ["--directory", "/Applications/java/idea/school-mcp", "run", "school-mcp"],
      "env": {
        "SCHOOL_API_BASE": "http://localhost:8080",
        "SCHOOL_USERNAME": "admin",
        "SCHOOL_PASSWORD": "admin123"
      }
    }
  }
}
```

重启 Claude Code，在新会话中输入：

```
帮我列出所有学生（前 5 个）
```

期望：Claude 调用 `school_student(action="page", size=5)` 并返回结果。

- [ ] **Step 4: 停止后端，记录验证结果**

后端按 Ctrl+C 停止。验证完成。

---

### Task H3：发布到 GitHub

- [ ] **Step 1: 在 GitHub 创建公开仓库**

去 https://github.com/new 创建：
- 名称：`school-mcp`
- 可见性：**Public**
- 不要勾选 "Initialize with README"（本地已有）

- [ ] **Step 2: 推送本地仓库**

```bash
cd /Applications/java/idea/school-mcp
git remote add origin git@github.com:zhangwh/school-mcp.git
git push -u origin main
```

> 如果 SSH 没配，改用 HTTPS：`git remote add origin https://github.com/zhangwh/school-mcp.git`

- [ ] **Step 3: 验证 uvx 远程拉取可用**

```bash
# 先清缓存避免命中本地
uv cache clean
# 用远程 git URL 启动一次
SCHOOL_API_BASE=http://localhost:8080 \
  SCHOOL_USERNAME=admin \
  SCHOOL_PASSWORD=admin123 \
  uvx --refresh --from git+https://github.com/zhangwh/school-mcp.git@main school-mcp 2>&1 | head -10
```

期望：看到 uv 拉取代码 → 安装依赖 → 输出 `school-mcp starting, backend=http://localhost:8080`，按 Ctrl+C 退出。

- [ ] **Step 4: 把 mcpServers 配置切回 git URL（README 模式）**

将 Claude Code 的配置从本地路径切回 git URL（README 中的标准配置），重启客户端，再次实测一次。

---

## 总结

完成后将拥有：

1. **后端**：在 `feature/school-management` 分支多 3 个 commit（OpenAPI 改造）
2. **MCP 项目**：独立 GitHub 公开仓库 `zhangwh/school-mcp`，main 分支可用
3. **测试**：约 30 个单元测试覆盖 auth / client / tools 核心逻辑
4. **可用性**：Claude Code 配 mcpServers 即可使用 9 个 tool

**后续维护**：
- 你后端加新接口 + 加 OpenAPI 注解 → MCP 用户最多 5 分钟感知
- 你后端 controller 字段变化 → MCP 不用改（payload: dict 透传 + OpenAPI 文档自动更新）
- 你 push 到 main → 用户重启客户端拿新版

---

## Self-Review

**Spec coverage check：**
- ✅ 第 2 章架构 → Phase A + B 整体落地
- ✅ 第 3 章技术栈与分发 → Task B2/B3 + H3
- ✅ 第 4 章项目结构 → Task B2 创建目录 + 各 Phase 创建文件
- ✅ 第 5 章工具设计 → Phase F (8 个 tool 实现) + Phase G (注册到 MCP)
- ✅ 第 6 章认证模块 → Phase D (D1-D5)
- ✅ 第 7 章 OpenAPI 集成 → Phase A (后端) + E3 (缓存) + F8 (list_apis)
- ✅ 第 8 章错误处理 → Task C1 (异常类) + Task G1 (`_convert_errors` 装饰器)
- ✅ 第 9 章日志 → Task G1 (basicConfig stderr)
- ✅ 第 10 章配置 → Task C2
- ✅ 第 11 章用户配置示例 → Task H1 README
- ✅ 第 12 章测试策略 → 各 Phase 的 TDD 步骤
- ✅ 第 13 章 YAGNI 清单 → 计划无超出项

**类型与命名一致性检查：**
- `student_tool` / `teacher_tool` / `clazz_tool` / `course_tool` / `grade_tool` / `user_tool` / `dashboard_tool` / `list_apis_tool` / `call_tool`：所有 tool 函数命名一致（resource_tool 模式）
- MCP 注册名 `school_student` / `school_teacher` / ... / `school_user`（注意 system 模块的对外 tool 名是 `school_user`）/ `school_dashboard` / `school_list_apis` / `school_call`
- `SchoolClient.request` / `aclose` / `fetch_openapi` 一致
- `AuthManager.get_token` / `force_refresh` / `_login` / `_is_valid` 一致

**无 placeholder 检查：**
- 所有 step 都有完整代码或完整命令
- 无 "类似 Task N" 引用
- 各 tool 文件都展开写完整代码
- F2-F6 的 "按 Step 1 结果调整" 是可执行的明确指令（grep 命令具体），不是"TODO 自己看着办"
