// 登录页：微信登录 → 后端 /api/auth/login → 存 token → 跳转首页
const auth = require('../../utils/auth.js')
const config = require('../../utils/config.js')

Page({
  data: {
    loading: false,
    baseUrl: config.baseUrl,
    isLocalDebug: config.isLocalDebug
  },

  onLoad() {
    if (auth.getToken()) {
      wx.redirectTo({ url: '/pages/index/index' })
      return
    }
    this.setData({
      baseUrl: config.baseUrl,
      isLocalDebug: config.isLocalDebug
    })
  },

  onLogin() {
    if (this.data.loading) return
    this.setData({ loading: true })
    auth.login()
      .then(() => {
        this.setData({ loading: false })
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          wx.redirectTo({ url: '/pages/index/index' })
        }, 500)
      })
      .catch((err) => {
        this.setData({ loading: false })
        const msg = err.message || '登录失败'
        wx.showToast({
          title: msg.length > 20 ? '登录失败，请查看控制台' : msg,
          icon: 'none',
          duration: 3000
        })
        console.error('[Login]', msg, err)
      })
  }
})
