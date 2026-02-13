// 结果页：展示 analysis、suggestion、code、docRefs
Page({
  data: {
    requestType: '',
    analysis: '',
    suggestion: '',
    code: '',
    docRefs: [],
    cardUrl: ''
  },

  onLoad() {
    const res = wx.getStorageSync('lastAskResult')
    if (!res) {
      wx.showToast({ title: '暂无结果', icon: 'none' })
      return
    }
    this.setData({
      requestType: res.requestType || '',
      analysis: res.analysis || '',
      suggestion: res.suggestion || '',
      code: res.code || '',
      docRefs: res.docRefs || [],
      cardUrl: res.cardUrl || ''
    })
  },

  onBack() {
    wx.navigateBack()
  },

  copyCode() {
    if (!this.data.code) return
    wx.setClipboardData({
      data: this.data.code,
      success: () => wx.showToast({ title: '已复制代码' })
    })
  },

  openLink(e) {
    const url = e.currentTarget && e.currentTarget.dataset && e.currentTarget.dataset.url
    if (url) {
      wx.setClipboardData({
        data: url,
        success: () => wx.showToast({ title: '链接已复制' })
      })
    }
  }
})
