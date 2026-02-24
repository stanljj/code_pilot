/**
 * 后端 API 封装：/api/v1/ask 等
 */
const { request } = require('./request.js')

/**
 * 发送提问请求
 * @param {string} content - 问题或代码内容
 * @param {string} [sessionId] - 可选会话 ID
 * @param {string} [source] - 可选来源：paste | photo | voice
 * @returns {Promise<AskResponse>}
 */
function ask(content, sessionId, source) {
  return request({
    method: 'POST',
    url: '/api/v1/ask',
    data: {
      content,
      sessionId: sessionId || null,
      source: source || 'paste'
    }
  })
}

/**
 * 发送OCR请求
 * @param {string} imagePath - 图片文件路径
 * @param {string} [sessionId] - 可选会话 ID
 * @param {string} [source] - 可选来源，默认为'photo'
 * @returns {Promise<AskResponse>}
 */
function ocrAsk(imagePath, sessionId, source = 'photo') {
  return request({
    method: 'POST',
    url: '/api/v1/ask/ocr',
    uploadFile: true,
    data: {
      image: imagePath,
      sessionId: sessionId || null,
      source: source
    }
  })
}

module.exports = {
  ask,
  ocrAsk
}
