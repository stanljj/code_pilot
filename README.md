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
| 向量库   | Milvus（RAG 检索） |
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
| GET  | /api/auth/wecom/login | 企微 OAuth 跳转 |
| GET  | /api/auth/wecom/callback | 企微回调，换 token |
| POST | /api/v1/ask | 文本提问（分类+LLM/RAG+融合+脱敏） |
| POST | /api/v1/ask/ocr | 上传图片，OCR 后走 ask 流程 |
| GET  | /api/v1/session/{id} | 可选：获取 7 天内会话缓存 |

请求体示例：`{ "content": "问题或代码", "sessionId": "可选" }`  
响应体：`{ "requestType": "public|enterprise", "analysis", "suggestion", "code", "docRefs", "cardUrl" }`

## 六、本地运行

- **后端**：`cd backend && mvn spring-boot:run`，需配置 `application.yml` 中的阿里云 Key、Milvus、MySQL、企微应用。
- **前端**：`cd frontend && npm i && npm run dev`。
- **数据库**：执行 `db/V1__init_schema.sql` 或由 Flyway 自动执行。

## 七、Docker 部署

### 阿里云服务器部署（使用已有 MySQL 5.7）

服务器上已用 Docker 部署 MySQL 5.7（端口 3306）时，只部署后端：

1. 在 MySQL 中创建库并建表（若尚未执行）：
   - 创建数据库：`CREATE DATABASE IF NOT EXISTS code_pilot;`
   - 执行建表脚本：`docker/mysql-init/01-schema.sql` 中的语句（需先 `USE code_pilot;`）

2. 在项目根目录创建 `.env`（可复制 `.env.example` 后修改）：
   ```bash
   cp .env.example .env
   # 编辑 .env，填写 MYSQL_PASSWORD=你的MySQL root 密码
   ```

3. 在服务器上启动后端：
   ```bash
   docker compose up -d
   ```
   后端会通过 `host.docker.internal:3306` 连接本机 MySQL，端口 8080 对外提供 API。

4. 验证：`curl -u demo:demo http://47.96.135.97:8080/api/v1/ask`（需 POST 且带 body，此处仅验证连通性可看健康检查或直接访问 8080）。

大模型等敏感配置可通过环境变量传入（如 `DASHSCOPE_API_KEY`），在 `.env` 或 `docker-compose.yml` 的 `environment` 中配置。

### 仅构建并运行后端镜像（使用已有 MySQL）

```bash
cd backend
docker build -t codepilot-mini-backend:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/code_pilot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=xxx \
  --add-host=host.docker.internal:host-gateway \
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
3. **前端**：在 `frontend` 目录执行 `npm i && npm run dev`。打开 http://localhost:5173，按提示输入演示账号（如 demo/demo）后即可提问。
4. **微信小程序**：
   - 用 **微信开发者工具** 打开项目下的 `miniprogram` 目录（选择「小程序」类型，AppID 可先选「测试号」）。
   - 本地调试：在 `miniprogram/utils/config.js` 中把 `baseUrl` 改为你的后端地址（如 `http://localhost:8080`），并在开发者工具中勾选 **「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」**。
   - 请求会携带 Basic 认证（默认 demo/demo，与后端演示账号一致）。正式环境需将后端改为 HTTPS，并在微信公众平台配置 request 合法域名。
   - 首页输入问题或粘贴代码后点击「提交提问」，结果页展示分析、建议与代码片段。

## 十、可能问题与改进

- **DashScope SDK**：若 `QwenLlmService` 中 `Generation`/`Message` 包名与当前 SDK 版本不一致，请参考 [dashscope-sdk-java](https://github.com/modelscope/dashscope-sdk-java) 最新 API 调整 import 与调用方式。
- **RAG**：`MilvusRagService` 当前为占位实现，生产需接入 Milvus 建表、向量化 query、按 tenantId 隔离 collection，并与 Git/Jira/Confluence 同步脚本配合。
- **认证**：生产建议将 Basic 改为 JWT，企微回调用 code 换 userid 后签发 token，前端/小程序请求头带 `Authorization: Bearer <token>`。
- **OCR 接口**：若需“拍照识码”，可新增 `POST /api/v1/ask/ocr`，接收图片后调用微信 OCR 或阿里云通用文字识别，再将识别文本传入现有 ask 流程。
