import { defineStore } from 'pinia'
import { getMe, logout, wechatLogin } from '../api/auth'
import { getMyReaderInfo } from '../api/reader'
import {
  clearAuthSession,
  getAccessToken,
  getRefreshToken,
  getStoredReaderInfo,
  getStoredUserInfo,
  setAuthSession,
  updateReaderInfo,
  updateUserInfo
} from '../utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: getAccessToken(),
    refreshToken: getRefreshToken(),
    userInfo: getStoredUserInfo(),
    readerInfo: getStoredReaderInfo(),
    initialized: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
    displayName: (state) => state.userInfo?.realName || state.userInfo?.username || '未登录'
  },
  actions: {
    hydrate() {
      this.accessToken = getAccessToken()
      this.refreshToken = getRefreshToken()
      this.userInfo = getStoredUserInfo()
      this.readerInfo = getStoredReaderInfo()
      this.initialized = true
    },
    async bootstrap() {
      this.hydrate()
      if (!this.accessToken) {
        return
      }
      await Promise.allSettled([this.fetchMe(), this.fetchReaderInfo()])
    },
    async loginWithCode(code) {
      const data = await wechatLogin({ code })
      setAuthSession({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user
      })
      this.hydrate()
      await this.fetchReaderInfo().catch(() => null)
      return data
    },
    async fetchMe() {
      const data = await getMe()
      updateUserInfo(data)
      this.userInfo = data
      return data
    },
    async fetchReaderInfo() {
      const data = await getMyReaderInfo()
      updateReaderInfo(data)
      this.readerInfo = data
      return data
    },
    async logout() {
      try {
        if (this.accessToken) {
          await logout().catch(() => null)
        }
      } finally {
        clearAuthSession()
        this.hydrate()
      }
    }
  }
})