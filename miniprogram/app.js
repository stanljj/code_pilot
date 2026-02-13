// CodePilot Mini - 微信小程序入口
// 调用后端 /api/v1/ask 进行智能问答（通用 + 代码）

App({
  globalData: {
    sessionId: null,
    userInfo: null
  },

  onLaunch() {
    // 可在此处做 wx.login 或企微静默登录，获取 sessionId
    console.log('[CodePilot] 小程序启动')
  }
})
