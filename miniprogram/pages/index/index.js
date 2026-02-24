// 首页：输入问题或代码，提交到 /api/v1/ask
const api = require('../../utils/api.js')
const auth = require('../../utils/auth.js')
const app = getApp()

// 走 RAG 的测试文案（命中企业关键词 + 已登录有 tenantId 时后端会走 RAG）
const RAG_SAMPLE_CONTENTS = [
  '根据我们项目规范，代码评审要注意什么？',
  'JIRA 里这个 bug 怎么修？',
  '根据 Confluence 文档，部署流程是什么？',
  '我们团队的 repo 分支策略是怎样的？',
  '内部接口 v1.2 的变更说明在哪？'
]

Page({
  data: {
    content: '',
    loading: false,
    imagePath: '',
    ragSamples: RAG_SAMPLE_CONTENTS
  },

  onFillRagSample(e) {
    const content = e.currentTarget.dataset.content || ''
    if (content) this.setData({ content })
  },

  onReLogin() {
    auth.clearToken()
    wx.reLaunch({ url: '/pages/login/login' })
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
  },

  // 选择图片
  onChooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['original', 'compressed'],
      sourceType: ['album'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        this.setData({
          imagePath: tempFilePath
        })
        wx.showToast({
          title: '图片选择成功',
          icon: 'success'
        })
      },
      fail: (err) => {
        console.error('选择图片失败', err)
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        })
      }
    })
  },

  // 拍照
  onTakePhoto() {
    wx.chooseImage({
      count: 1,
      sizeType: ['original', 'compressed'],
      sourceType: ['camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        this.setData({
          imagePath: tempFilePath
        })
        wx.showToast({
          title: '拍照成功',
          icon: 'success'
        })
      },
      fail: (err) => {
        console.error('拍照失败', err)
        wx.showToast({
          title: '拍照失败',
          icon: 'none'
        })
      }
    })
  },

  // 清除图片
  onClearImage() {
    this.setData({
      imagePath: ''
    })
    wx.showToast({
      title: '图片已清除',
      icon: 'success'
    })
  },

  // OCR提交
  onOcrSubmit() {
    if (!this.data.imagePath) {
      wx.showToast({
        title: '请先选择图片',
        icon: 'none'
      })
      return
    }

    this.setData({ loading: true })
    
    api.ocrAsk(this.data.imagePath, app.globalData.sessionId, 'photo')
      .then((res) => {
        this.setData({ 
          loading: false,
          imagePath: ''  // 识别成功后清除图片
        })
        wx.setStorageSync('lastAskResult', res)
        wx.navigateTo({ url: '/pages/result/result' })
      })
      .catch((err) => {
        this.setData({ loading: false })
        const msg = err.message || 'OCR识别失败'
        wx.showToast({
          title: msg.indexOf('合法域名') > -1 ? msg : msg + '；请勾选「不校验合法域名」',
          icon: 'none',
          duration: 3500
        })
      })
  }
})
