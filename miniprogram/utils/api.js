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

module.exports = {
  ask
}
