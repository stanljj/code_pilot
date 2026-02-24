/**
 * 小程序登录与 Token 管理（按流程图：wx.login → code → POST /auth/login → 存 JWT）
 * 请求 /api/v1/ask 时在 Header 中携带 Authorization: Bearer <token>
 */
const config = require('./config.js')

const TOKEN_KEY = 'codepilot_token'

/**
 * 获取本地缓存的 token，没有则返回空字符串
 */
function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

/**
 * 保存 token 到本地
 */
function setToken(token) {
  if (token) {
    wx.setStorageSync(TOKEN_KEY, token)
  }
}

/**
 * 清除 token（登出时可用）
 */
function clearToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

/**
 * 执行登录：wx.login 取 code → 调后端 POST /api/auth/login { code } → 存 token
 * @returns {Promise<string>} 成功返回 token，失败 reject
 */
function login() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (!res.code) {
          reject(new Error('微信登录未返回 code'))
          return
        }
        const url = config.baseUrl + '/api/auth/login'
        wx.request({
          url,
          method: 'POST',
          header: { 'Content-Type': 'application/json' },
          data: { code: res.code },
          success(reqRes) {
            if (reqRes.statusCode >= 200 && reqRes.statusCode < 300 && reqRes.data && reqRes.data.token) {
              setToken(reqRes.data.token)
              resolve(reqRes.data.token)
            } else {
              const msg = (reqRes.data && reqRes.data.message) || reqRes.errMsg || '登录失败'
              reject(new Error(msg))
            }
          },
          fail(err) {
            reject(err)
          }
        })
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

/**
 * 确保已登录：若本地无 token 则先执行 login()，再返回 token（用于发起 ask 等请求前）
 * @returns {Promise<string>}
 */
function ensureToken() {
  const token = getToken()
  if (token) return Promise.resolve(token)
  return login()
}

module.exports = {
  getToken,
  setToken,
  clearToken,
  login,
  ensureToken
}
