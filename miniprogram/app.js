// CodePilot Mini - 微信小程序入口
// 按流程图：启动时 wx.login → code → POST /api/auth/login → 存 JWT；请求 /ask 时带 Authorization: Bearer <token>
const auth = require('./utils/auth.js')

App({
  globalData: {
    sessionId: null,
    userInfo: null
  },

  onLaunch() {
    console.log('[CodePilot] 小程序启动')
    auth.ensureToken().then(() => {
      console.log('[CodePilot] 登录态已就绪')
    }).catch((err) => {
      console.warn('[CodePilot] 登录未完成（可继续使用 Basic 演示）', err.message)
    })
  }
})
