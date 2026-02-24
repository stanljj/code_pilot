# CodePilot Mini 设计方案与实现

> 面向 IT 工程师的智能代码助手，微信小程序 + 后端 API，支持公共问答与企业 RAG。

## 一、架构概览

```
微信小程序 → 后端 API 网关 → 请求分类
                ↓                    ↓
         公共问题 → 公有大模型(Qwen-Max/CodeQwen)
         企业上下文 → RAG 模块 ← 向量库(Milvus, Git/Jira/Confluence 同步)
                ↓
         结果融合引擎 → 安全过滤器(脱敏/合规) → 返回小程序
```

- **入口**：微信小程序（拍照/语音/粘贴）
- **网关**：统一鉴权、限流、路由
- **分类**：公共问题直连大模型；含企业上下文走 RAG
- **大模型**：阿里云 Qwen-Max（通用）+ CodeQwen（代码）
- **RAG**：LangChain + Milvus，企业私有知识隔离
- **安全**：默认不存代码、敏感信息脱敏、审计日志（SOC2）

## 二、技术选型

| 层级     | 技术 |
|----------|------|
| 前端     | 微信小程序 + Vue 管理台 |
| 后端     | Java 17 + Spring Boot 3.x |
| 数据库   | MySQL 8（元数据、审计、配置） |
| 向量库   | Milvus 2.4+（RAG 检索） |
| 大模型   | 阿里云 Qwen-Max + CodeQwen |
| OCR      | 微信 OCR 或 阿里云通用文字识别 |
| 部署     | 阿里云 Serverless（FC + API Gateway） |
| 认证     | 企业微信 OAuth 2.0 |

## 三、数据安全与合规

- **默认不存储代码**：请求处理完即销毁；可选“会话缓存”7 天。
- **敏感信息脱敏**：自动识别 IP、密钥、手机号等并替换为占位符；企业可配置脱敏规则。
- **私有知识隔离**：按企业租户启用对应知识库，企业间数据隔离。
- **审计日志**：记录谁、何时、查询什么，满足 SOC2。

## 四、项目结构

```
code-pilot-mini/
├── README.md
├── backend/                 # Java Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../codepilot/
│       │   ├── gateway/     # 网关与鉴权
│       │   ├── classify/    # 请求分类
│       │   ├── llm/         # Qwen/CodeQwen 调用
│       │   ├── rag/         # RAG + Milvus
│       │   ├── fusion/      # 结果融合
│       │   ├── security/    # 脱敏与合规
│       │   └── audit/       # 审计日志
│       └── resources/
│           ├── application.yml
│           └── db/migration/ # Flyway
├── frontend/                # Vue 3 管理台/Web 版
│   ├── package.json
│   └── src/
├── miniprogram/             # 微信小程序（微信开发者工具打开）
│   ├── app.js, app.json, app.wxss
│   ├── project.config.json
│   ├── utils/               # config、request、api
│   └── pages/               # index（提问）、result（结果）
├── docker-compose.yml       # 后端 + MySQL 一键启动
├── docker/
│   └── mysql-init/         # MySQL 首次启动建表脚本
└── db/                       # MySQL 表结构
    └── V1__init_schema.sql
```

## 五、API 约定（供小程序/前端调用）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | **微信小程序登录**：请求体 `{ "code": "wx.login 获得的 code" }`，返回 `{ "token": "JWT" }`；后续请求需带 `Authorization: Bearer <token>` |
| GET  | /api/auth/wecom/login | 企微 OAuth 跳转 |
| GET  | /api/auth/wecom/callback | 企微回调，换 token |
| POST | /api/v1/ask | 文本提问（需认证：JWT Bearer 或 Basic）；后端解析 token → UserDetails → resolveTenantId()，用于 RAG 租户隔离 |
| POST | /api/v1/ask/ocr | 上传图片，OCR 后走 ask 流程 |
| GET  | /api/v1/session/{id} | 可选：获取 7 天内会话缓存 |
| GET  | /api/rag/exists/{collectionName} | 检查RAG集合是否存在 |
| POST | /api/rag/collection/create | 创建RAG集合 |
| DELETE | /api/rag/collection/delete/{collectionName} | 删除RAG集合 |
| POST | /api/rag/search | 搜索向量数据 |
| GET  | /api/rag/count/{collectionName} | 获取集合中向量数量 |

请求体示例：`{ "content": "问题或代码", "sessionId": "可选" }`  
响应体：`{ "requestType": "public|enterprise", "analysis", "suggestion", "code", "docRefs", "cardUrl" }`

## 六、本地运行

- **后端**：`cd backend && mvn spring-boot:run`，需配置 `application.yml` 中的阿里云 Key、Milvus、MySQL、企微应用。
- **前端**：`cd frontend && npm i && npm run dev`。
- **数据库**：执行 `db/V1__init_schema.sql` 或由 Flyway 自动执行。

## 七、Docker 部署

### Docker 镜像加速配置（重要）

**强烈建议在部署前配置Docker镜像加速器，特别是在中国大陆地区，以避免镜像拉取超时问题：**

#### 方法一：使用自动化配置脚本（推荐）

```bash
# 在Linux服务器上运行
sudo chmod +x setup-docker-mirror.sh
sudo ./setup-docker-mirror.sh
```

#### 方法二：手动配置

1. 创建或编辑 Docker 配置文件：
   - Linux: `/etc/docker/daemon.json`
   - Windows: 在 Docker Desktop 设置中配置
   - macOS: 在 Docker Desktop 设置中配置

2. 将以下内容写入配置文件：
   ```json
   {
     "registry-mirrors": [
       "https://docker.m.daocloud.io",
       "https://dockerproxy.com",
       "https://hub-mirror.c.163.com",
       "https://mirror.ccs.tencentyun.com"
     ]
   }
   ```

3. 重启 Docker 服务以使配置生效：
   ```bash
   sudo systemctl restart docker
   ```

**注意：** 我们提供了 `docker-daemon.json` 和 `setup-docker-mirror.sh` 文件作为配置参考。

**推荐使用已验证的DaoCloud镜像：** 我们的部署脚本会自动检测网络状况并优先使用已验证可用的DaoCloud镜像源（`docker.m.daocloud.io`），这在中国大陆通常是最稳定的选择。

### 阿里云服务器部署（使用已有 MySQL 5.7）

服务器上已用 Docker 部署 MySQL 5.7（端口 3306）时，部署完整环境（包含Milvus向量库）：

1. 在 MySQL 中创建库并建表（若尚未执行）：
   - 创建数据库：`CREATE DATABASE IF NOT EXISTS code_pilot;`
   - 执行建表脚本：`docker/mysql-init/01-schema.sql` 中的语句（需先 `USE code_pilot;`）

2. 在项目根目录创建 `.env`（可复制 `.env.example` 后修改）：
   ```bash
   cp .env.example .env
   # 编辑 .env，填写 MYSQL_PASSWORD=你的MySQL root 密码
   ```

3. 在服务器上启动完整环境（包含后端、Milvus向量库等）：
   ```bash
   docker compose up -d
   ```
   后端会通过 `host.docker.internal:3306` 连接本机 MySQL，端口 8080 对外提供 API，Milvus向量库将在容器内部通过 `milvus:19530` 地址被访问。

4. 验证：`curl -u demo:demo http://47.96.135.97:8080/api/v1/ask`（需 POST 且带 body，此处仅验证连通性可看健康检查或直接访问 8080）。

大模型等敏感配置可通过环境变量传入（如 `DASHSCOPE_API_KEY`），在 `.env` 或 `docker-compose.yml` 的 `environment` 中配置.

### Milvus 向量库独立部署

如果需要独立部署Milvus向量库服务：

```bash
# 启动Milvus服务（包含Etcd和MinIO）
docker compose -f docker-compose.milvus.yml up -d

# 查看Milvus服务状态
docker compose -f docker-compose.milvus.yml ps

# 停止Milvus服务
docker compose -f docker-compose.milvus.yml down
```

**中国大陆地区用户部署说明：**

由于网络原因，在中国大陆地区可能无法正常拉取Docker Hub镜像，我们提供了多种解决方案：

1. **使用修复网络连接问题的配置（最高推荐，如果所有镜像都已存在）**：
   ```bash
   # 启动Milvus服务（使用服务器上完全已有的镜像 - 修复了etcd配置和容器网络连接问题）
   docker compose -f docker-compose.milvus-network-fixed.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-network-fixed.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-network-fixed.yml down
   ```

2. **使用修复版完全本地镜像（推荐，如果所有镜像都已存在）**：
   ```bash
   # 启动Milvus服务（使用服务器上完全已有的镜像 - 修复了etcd配置问题）
   docker compose -f docker-compose.milvus-fixed.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-fixed.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-fixed.yml down
   ```

3. **使用完全本地镜像（推荐，如果所有镜像都已存在）**：
   ```bash
   # 启动Milvus服务（使用服务器上完全已有的镜像 - 无需网络拉取）
   docker compose -f docker-compose.milvus-all-local.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-all-local.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-all-local.yml down
   ```

4. **使用本地已有的镜像（强烈推荐，如果镜像已存在）**：
   ```bash
   # 启动Milvus服务（使用服务器上已有的镜像 - 最高成功率）
   docker compose -f docker-compose.milvus-local.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-local.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-local.yml down
   ```

5. **使用现有镜像（推荐，如果镜像已存在）**：
   ```bash
   # 启动Milvus服务（使用服务器上已存在的镜像）
   docker compose -f docker-compose.milvus-existing.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-existing.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-existing.yml down
   ```

6. **使用已验证的DaoCloud镜像（备选方案）**：
   ```bash
   # 启动Milvus服务（使用已验证的DaoCloud镜像）
   docker compose -f docker-compose.milvus-dao.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-dao.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-dao.yml down
   ```

7. **使用直接镜像加速（备选方案）**：
   ```bash
   # 启动Milvus服务（使用直接镜像加速）
   docker compose -f docker-compose.milvus-direct.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-direct.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-direct.yml down
   ```

8. **使用DaoCloud镜像加速（备选方案）**：
   ```bash
   # 启动Milvus服务（使用DaoCloud镜像加速）
   docker compose -f docker-compose.milvus-daocloud.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-daocloud.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-daocloud.yml down
   ```

9. **使用中科大镜像加速（备选方案）**：
   ```bash
   # 启动Milvus服务（使用中科大镜像加速）
   docker compose -f docker-compose.milvus-ustc.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-ustc.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-ustc.yml down
   ```

10. **使用中国区镜像（最后备选方案）**：
   ```bash
   # 启动Milvus服务（使用中国区镜像）
   docker compose -f docker-compose.milvus-cn.yml up -d
   
   # 查看Milvus服务状态
   docker compose -f docker-compose.milvus-cn.yml ps
   
   # 停止Milvus服务
   docker compose -f docker-compose.milvus-cn.yml down
   ```

Milvus服务默认端口：
- gRPC端口: 19530
- REST API端口: 9091
- Metrics端口: 19121
- MinIO控制台端口: 9001

### 仅构建并运行后端镜像（使用已有 MySQL 和 Milvus）

```bash
cd backend
docker build -t codepilot-mini-backend:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/code_pilot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=xxx \
  -e MILVUS_HOST=your-milvus-host \
  -e MILVUS_PORT=19530 \
  --add-host=host.docker.internal=host-gateway \
  codepilot-mini-backend:latest
```

## 八、部署（阿里云 Serverless）

- 将 `backend` 打成 JAR，部署到 **函数计算 FC**，HTTP 触发器 + **API 网关** 暴露。
- 按调用量计费，成本可控；密钥与配置使用 FC 环境变量或 KMS，禁止写死在代码中。

---

## 九、使用说明

1. **MySQL**：创建库 `codepilot`，执行 `db/V1__init_schema.sql` 或由后端 Flyway 自动执行。
2. **后端**：在 `backend` 目录执行 `mvn spring-boot:run`。首次运行需配置环境变量（或 `application.yml`）：
   - `MYSQL_*`：数据库连接
   - `DASHSCOPE_API_KEY`：阿里云 DashScope API Key（不配置则返回演示文案）
   - `WECOM_*`：企业微信应用（不配置则前端可走“演示认证”）
   - **微信小程序登录**：`wechat.miniprogram.app-id`、`wechat.miniprogram.app-secret`（在微信公众平台小程序设置中获取）；`jwt.secret`、`jwt.expiration-ms`（可选，默认 7 天）。并执行 `backend/src/main/resources/db/migration/V2__wx_user.sql` 创建 `wx_user` 表（或开启 Flyway 自动执行）。
3. **前端**：在 `frontend` 目录执行 `npm i && npm run dev`。打开 http://localhost:5173，按提示输入演示账号（如 demo/demo）后即可提问。
4. **微信小程序**（与流程图一致）：
   - 用 **微信开发者工具** 打开项目下的 `miniprogram` 目录（选择「小程序」类型，AppID 需与后端配置一致或使用测试号）。
   - **登录页**：首次打开为「登录」页，点击「微信登录」→ `wx.login()` 取 code → 请求 `POST /api/auth/login` → 后端校验 code 获 openid、签发 JWT → 存 token 并跳转首页；已有 token 时直接进首页。
   - **本地调试**：① `miniprogram/utils/config.js` 中设 `baseUrl: 'http://127.0.0.1:8080'`；② 开发者工具「详情」→「本地设置」→ 勾选「不校验合法域名、web-view、TLS 以及 HTTPS 证书」；③ 本机先启动后端（如 `mvn spring-boot:run`）。登录页会显示当前后端地址与本地调试提示。
   - 若未配置微信 app-id/app-secret，登录会 401，小程序可回退 Basic 认证（demo/demo）在首页直接提问。
   - 首页输入问题或粘贴代码后点击「提交提问」，结果页展示分析、建议与代码片段。**走 RAG 测试**：需先登录（有 tenantId），在首页点击「走 RAG 测试」下的任一句子填入（如「根据我们项目规范，代码评审要注意什么？」「JIRA 里这个 bug 怎么修？」），再提交即可让 ask 接口走 RAG 检索。

## 十、可能问题与改进

- **Docker 构建时拉取基础镜像失败**（如 `dial tcp ... i/o timeout` 或镜像站 500）：
  - 后端 Dockerfile 已默认使用国内镜像源 `docker.m.daocloud.io` 拉取 eclipse-temurin。若仍失败可尝试：
    1. 换镜像源：构建时加 `--build-arg BASE_MIRROR=docker.xuanyuan.me`，或直接改 `backend/Dockerfile` 第一行 `ARG BASE_MIRROR=...` 为可用域名（如 `docker.xuanyuan.me`）。
    2. 在 Docker 宿主机配置可用的 registry-mirror（腾讯云/轩辕等），并把 Dockerfile 中 `ARG BASE_MIRROR=docker.io` 改回官方源再构建。
    3. 在有外网环境先 `docker pull eclipse-temurin:17-jre-alpine` 并 `docker save`，到目标机 `docker load` 后再构建。
  - 阿里云镜像报 500 多为临时故障，可换用 DaoCloud/腾讯云/轩辕等或上述方式。
- **DashScope SDK**：若 `QwenLlmService` 中 `Generation`/`Message` 包名与当前 SDK 版本不一致，请参考 [dashscope-sdk-java](https://github.com/modelscope/dashscope-sdk-java) 最新 API 调整 import 与调用方式。
- **请求分类**：`SimpleRequestClassifier` 支持规则引擎 + 可选小模型。配置 `classifier.mode`：`rule`（仅规则）、`model`（仅小模型）、`rule_then_model`（先规则，有租户且规则未命中时再调小模型）。规则为正则列表 `classifier.rule.enterprise-patterns`；小模型使用 `classifier.model.model`（如 qwen-turbo），API Key 可单独配置或回退 `llm.qwen-max.api-key`。
- **RAG**：`MilvusRagService` 已实现完整功能，包括：
  - 连接 Milvus 向量数据库
  - 按 tenantId 隔离 collection（租户数据隔离）
  - 使用 DashScope embedding API 向量化查询和文档
  - 提供文档同步接口支持 Git/Jira/Confluence 数据源
  - 支持文档增删查等操作
- **认证**：已实现微信小程序登录 + JWT 流程：小程序 `wx.login()` → code → `POST /api/auth/login` → 后端 code2Session 获 openid，查/建用户关联 tenantId，签发 JWT；请求 `/api/v1/ask` 时携带 `Authorization: Bearer <token>`，后端解析 token 得到 UserDetails 与 tenantId（用于 RAG 隔离）。未配置微信时仍可使用 Basic（demo/demo）演示。
- **OCR 接口**：若需“拍照识码”，可新增 `POST /api/v1/ask/ocr`，接收图片后调用微信 OCR 或阿里云通用文字识别，再将识别文本传入现有 ask 流程。
