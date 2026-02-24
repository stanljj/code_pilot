# /api/v1/ask 接口测试用例

## 接口信息
- **接口路径**: POST /api/v1/ask
- **接口描述**: 向CodePilot Mini系统提交问题并获得回答
- **请求格式**: application/json
- **响应格式**: application/json

## 请求参数

### 必填参数
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| content | string | 用户输入的问题内容 | "如何在Spring Boot中配置数据库连接池？" |

### 可选参数
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| sessionId | string | 会话ID，用于7天会话缓存 | "session_1234567890" |
| source | string | 问题来源 | "photo", "voice", "paste" |

## 响应参数
| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| requestType | string | 请求类型 | "public", "enterprise" |
| analysis | string | 问题分析摘要 | "这是一个关于Spring Boot数据库连接池配置的问题..." |
| suggestion | string | 修复/实现建议 | "推荐使用HikariCP作为默认连接池..." |
| code | string | 高亮显示的代码片段 | "# application.yml 配置示例..." |
| docRefs | array | 相关文档引用 | [{"title": "《Spring Boot 官方文档》", "link": "..."}] |
| cardUrl | string | 分享卡片URL | "https://work.weixin.qq.com/share/abc123" |

## 测试用例

### 正常情况测试用例

#### 测试用例1: 基本问题咨询
- **描述**: 发起一个基本的技术问题咨询
- **请求**:
```json
{
  "content": "如何在Spring Boot中配置数据库连接池？"
}
```
- **预期响应**: 返回关于数据库连接池配置的分析、建议和代码示例

#### 测试用例2: 带会话ID的问题
- **描述**: 发起问题并提供会话ID以支持上下文记忆
- **请求**:
```json
{
  "content": "如何处理跨域请求？",
  "sessionId": "session_1234567890",
  "source": "paste"
}
```
- **预期响应**: 返回关于跨域处理的分析、建议和代码示例

#### 测试用例3: 代码审查问题
- **描述**: 提交代码片段进行审查和改进建议
- **请求**:
```json
{
  "content": "以下代码有什么问题？\n\n@RestController\npublic class UserController {\n    @GetMapping(\"/users\")\n    public List<User> getUsers() {\n        return userRepository.findAll();\n    }\n}\n\n上述代码没有进行异常处理，也没有考虑性能优化，请给出改进方案。",
  "sessionId": "session_abcdefg",
  "source": "paste"
}
```
- **预期响应**: 返回对代码的分析、改进建议和优化后的代码示例

#### 测试用例4: 企业级问题
- **描述**: 提出企业内部规范相关的问题
- **请求**:
```json
{
  "content": "根据公司内部规范，如何实现用户权限管理模块？",
  "sessionId": "session_enterprise_001",
  "source": "photo"
}
```
- **预期响应**: 返回企业级权限管理的分析、建议和代码示例

#### 测试用例5: 语音转文字问题
- **描述**: 通过语音输入的问题
- **请求**:
```json
{
  "content": "帮我生成一个Spring Boot的CRUD控制器模板",
  "source": "voice"
}
```
- **预期响应**: 返回Spring Boot CRUD控制器的模板代码

### 异常情况测试用例

#### 测试用例6: 缺少必填参数
- **描述**: 发起请求但缺少content参数
- **请求**:
```json
{
  "sessionId": "session_1234567890"
}
```
- **预期响应**: HTTP 400 错误，提示 "content 不能为空"

#### 测试用例7: content参数过长
- **描述**: 发起请求但content参数超过20000字符限制
- **请求**:
```json
{
  "content": "一个长度超过20000字符的字符串...",
  "sessionId": "session_1234567890"
}
```
- **预期响应**: HTTP 400 错误，提示内容长度超出限制

#### 测试用例8: 无效的source值
- **描述**: 发起请求但source参数值不在允许范围内
- **请求**:
```json
{
  "content": "如何配置数据库？",
  "source": "invalid_source"
}
```
- **预期响应**: 请求被接受，但source参数会被忽略或使用默认值

## 请求头
- Content-Type: application/json

## 环境变量
- baseUrl: http://localhost:8080 (开发环境)
- baseUrl: https://api.codepilot-mini.com (生产环境)

## 注意事项
1. content参数为必填项，最大长度为20000字符
2. sessionId用于维护会话上下文，支持7天缓存
3. source参数用于标识问题来源，有助于后续分析
4. 企业级问题可能会触发RAG检索，返回更相关的内部文档
5. 敏感信息会在响应前进行脱敏处理