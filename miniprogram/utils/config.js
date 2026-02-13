/**
 * 小程序配置：后端地址与认证
 * 本地调试必做两步：
 * 1. 本文件 baseUrl 保持或改为你的后端地址（如 http://localhost:8080 或 http://127.0.0.1:8080）
 * 2. 微信开发者工具右上角「详情」→「本地设置」→ 勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」
 * 正式环境：baseUrl 需为 https，并在微信公众平台配置 request 合法域名
 */
const config = {
  // 后端 API 根地址（勿以 / 结尾）。本地调试用 localhost 或 127.0.0.1
  baseUrl: 'http://127.0.0.1:8080',
  // 演示 Basic 认证（与后端 demo/demo 对应）
  basicAuth: 'ZGVtbzpkZW1v'  // Base64('demo:demo')
}

module.exports = config
