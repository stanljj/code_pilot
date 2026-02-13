/**
 * 封装 wx.request：统一 Base URL、Basic 认证、错误提示
 */
const config = require('./config.js')

function request(options) {
  const url = options.url.startsWith('http') ? options.url : (config.baseUrl + options.url)
  const header = {
    'Content-Type': 'application/json',
    ...(options.header || {})
  }
  if (config.basicAuth) {
    header['Authorization'] = 'Basic ' + config.basicAuth
  }
  return new Promise((resolve, reject) => {
    wx.request({
      url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const msg = (res.data && res.data.message) || res.errMsg || '请求失败'
          reject(new Error(msg))
        }
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

module.exports = { request }
