/**
 * 封装 wx.request：统一 Base URL、优先 JWT（Bearer Token），无 token 时回退 Basic 认证
 */
const config = require('./config.js')
const auth = require('./auth.js')

function request(options) {
  const url = options.url.startsWith('http') ? options.url : (config.baseUrl + options.url)
  
  // 处理文件上传
  if (options.uploadFile) {
    return uploadFile(url, options.data, options.header)
  }
  
  const header = {
    'Content-Type': 'application/json',
    ...(options.header || {})
  }
  const token = auth.getToken()
  if (token) {
    header['Authorization'] = 'Bearer ' + token
  } else if (config.basicAuth) {
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

/**
 * 文件上传功能
 * @param {string} url - 上传地址
 * @param {Object} data - 包含文件路径和其他参数的对象
 * @param {Object} customHeader - 自定义头部
 * @returns {Promise}
 */
function uploadFile(url, data, customHeader) {
  const header = {
    ...(customHeader || {})
  }
  const token = auth.getToken()
  if (token) {
    header['Authorization'] = 'Bearer ' + token
  } else if (config.basicAuth) {
    header['Authorization'] = 'Basic ' + config.basicAuth
  }
  
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url,
      filePath: data.image,
      name: 'image',
      formData: {
        sessionId: data.sessionId || null,
        source: data.source || 'photo'
      },
      header,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            const result = JSON.parse(res.data)
            resolve(result)
          } catch (e) {
            resolve(res.data)
          }
        } else {
          const msg = res.errMsg || '上传失败'
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
