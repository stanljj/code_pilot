# OCR 功能模块

## 概述

OCR（Optical Character Recognition，光学字符识别）模块提供了图片文字识别功能，支持"拍照识码"功能。用户可以通过上传图片，系统将自动识别图片中的文字内容，并将其传递给现有的ask流程进行处理。

## 接口说明

### POST /api/v1/ask/ocr

接收图片并进行文字识别，然后将识别结果传入现有的ask流程。

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| image | File | 是 | 要识别的图片文件，支持常见的图片格式 |
| sessionId | String | 否 | 会话ID，用于7天会话缓存 |
| source | String | 否 | 来源，默认为"photo" |

#### 请求示例

```bash
curl -X POST http://localhost:8080/api/v1/ask/ocr \
  -H "Authorization: Basic ZGVtbzpkZW1v" \
  -F "image=@screenshot.jpg" \
  -F "sessionId=123456" \
  -F "source=photo"
```

#### 响应

返回与 `/api/v1/ask` 接口相同的响应格式，包含问题分析、建议、代码等内容。

## 实现架构

### 服务层

- **OcrService**: OCR服务接口
- **WeComOcrService**: 微信企业号OCR服务实现
- **AliyunOcrService**: 阿里云OCR服务实现
- **CompositeOcrService**: 复合OCR服务，按优先级尝试不同OCR服务

### 控制器层

- **OcrController**: 处理OCR请求，调用OCR服务识别图片，然后将结果传递给ask流程

## 配置说明

### 微信企业号OCR配置

```yaml
wecom:
  corp-id: ${WECOM_CORP_ID:}          # 企业微信CorpID
  agent-id: ${WECOM_AGENT_ID:}        # 应用AgentID
  secret: ${WECOM_SECRET:}           # 应用Secret
  redirect-uri: ${WECOM_REDIRECT_URI:http://localhost:8080/api/auth/wecom/callback}
```

### 阿里云OCR配置

```yaml
aliyun:
  access-key-id: ${ALIBABA_CLOUD_ACCESS_KEY_ID:}        # 阿里云AccessKey ID
  access-key-secret: ${ALIBABA_CLOUD_ACCESS_KEY_SECRET:} # 阿里云AccessKey Secret
```

## 工作流程

1. 客户端通过POST请求发送图片到 `/api/v1/ask/ocr`
2. [OcrController](file:///c:/Users/45255/code_pilot/backend/src/main/java/com/codepilot/ocr/OcrController.java) 接收请求并验证图片格式
3. [CompositeOcrService](file:///c:/Users/45255/code_pilot/backend/src/main/java/com/codepilot/ocr/CompositeOcrService.java) 按优先级尝试不同的OCR服务进行文字识别
4. 识别出的文字内容被封装成AskRequest对象
5. 调用现有的ask流程([AskController](file:///c:/Users/45255/code_pilot/backend/src/main/java/com/codepilot/gateway/AskController.java))进行处理
6. 返回处理结果给客户端

## 扩展性

系统设计具有良好的扩展性，可以轻松添加新的OCR服务实现，只需要：

1. 实现 [OcrService](file:///c:/Users/45255/code_pilot/backend/src/main/java/com/codepilot/ocr/OcrService.java) 接口
2. 将其实现类标记为 `@Service` 注解
3. [CompositeOcrService](file:///c:/Users/45255/code_pilot/backend/src/main/java/com/codepilot/ocr/CompositeOcrService.java) 会自动发现并使用新的OCR服务