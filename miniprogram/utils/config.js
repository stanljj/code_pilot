/**
 * 小程序配置：后端地址与认证
 * 本地调试：baseUrl 改为 http://127.0.0.1:8080 或 http://localhost:8080，并勾选「不校验合法域名…」
 * 正式环境：baseUrl 为 https，并在微信公众平台配置 request 合法域名
 */
const BASE_URL_LOCAL = 'http://127.0.0.1:8080'
// const BASE_URL_REMOTE = 'http://47.96.135.97:8080'

const config = {
  // 本地调试用下面这行，发布/真机测远端用 BASE_URL_REMOTE
  baseUrl: BASE_URL_LOCAL,
  // baseUrl: BASE_URL_REMOTE,
  // 是否本地调试（用于登录页展示提示）
  isLocalDebug: true,
  // 演示 Basic 认证（未拿到 JWT 时与后端 demo/demo 对应）
  basicAuth: 'ZGVtbzpkZW1v'  // Base64('demo:demo')
}

// 根据 baseUrl 自动判定是否本地调试（便于切换 baseUrl 时不用改 isLocalDebug）
const host = (config.baseUrl || '').replace(/^https?:\/\//, '').split('/')[0]
config.isLocalDebug = /^(localhost|127\.0\.0\.1)(:\d+)?$/i.test(host)

module.exports = config
