// 首页：输入问题或代码，提交到 /api/v1/ask
const api = require('../../utils/api.js')
const app = getApp()

Page({
  data: {
    content: '',
    loading: false
  },

  onInput(e) {
    this.setData({ content: (e.detail && e.detail.value) || '' })
  },

  onSubmit() {
    const content = (this.data.content || '').trim()
    if (!content) {
      wx.showToast({ title: '请输入问题或代码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    api.ask(content, app.globalData.sessionId, 'paste')
      .then((res) => {
        this.setData({ loading: false })
        wx.setStorageSync('lastAskResult', res)
        wx.navigateTo({ url: '/pages/result/result' })
      })
      .catch((err) => {
        this.setData({ loading: false })
        const msg = err.message || '请求失败'
        wx.showToast({
          title: msg.indexOf('合法域名') > -1 ? msg : msg + '；请勾选「不校验合法域名」',
          icon: 'none',
          duration: 3500
        })
      })
  }
})
