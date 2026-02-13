# CodePilot Mini 微信小程序

本目录为可在 **微信开发者工具** 中打开的微信小程序项目，用于调用后端 `/api/v1/ask` 进行智能问答。

## 如何打开

1. 安装并打开 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)。
2. 选择「小程序」→「导入项目」。
3. 目录选择本仓库下的 **`miniprogram`** 文件夹（即当前目录）。
4. AppID 可先选「测试号」或使用自己的小程序 AppID。
5. 确定后即可预览和调试。

## 本地调试（连本机后端）

1. 确保后端已启动（如 `cd backend && mvn spring-boot:run`，端口 8080）。
2. 打开 `utils/config.js`，将 `baseUrl` 改为你的后端地址，例如：
   - 本机：`http://localhost:8080`
   - 局域网：`http://你的电脑IP:8080`
3. 在微信开发者工具右上角「详情」→「本地设置」中，勾选 **「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」**。
4. 在首页输入问题或粘贴代码，点击「提交提问」即可看到结果页。

## 认证说明

当前请求会携带 HTTP Basic 认证（用户名/密码：demo/demo），与后端演示账号一致。若后端修改了账号或改为 JWT，需同步修改 `utils/request.js` 或 `utils/config.js`。

## 目录说明

| 路径 | 说明 |
|------|------|
| `app.js` / `app.json` / `app.wxss` | 小程序入口与全局配置、样式 |
| `project.config.json` | 微信开发者工具项目配置 |
| `utils/config.js` | 后端 baseUrl、Basic 认证 |
| `utils/request.js` | 封装 wx.request（带鉴权） |
| `utils/api.js` | 封装 ask 接口 |
| `pages/index/` | 首页：输入与提交提问 |
| `pages/result/` | 结果页：展示分析、建议、代码 |
