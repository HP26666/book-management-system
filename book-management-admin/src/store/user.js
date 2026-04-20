import { defineStore } from 'pinia'
import { getMe, login as loginApi, logout as logoutApi } from '../api/auth'
import {
  buildLoginRedirect,
  clearAuthSession,
  getAccessToken,
  getRefreshToken,
  getStoredUserInfo,
  setAuthSession,
  updateUserInfo
} from '../utils/auth'

let authListenerBound = false

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getAccessToken(),
    refreshToken: getRefreshToken(),
    userInfo: getStoredUserInfo(),
    initialized: false
  }),
  getters: {
    roles: (state) => state.userInfo?.roles || [],
    permissions: (state) => state.userInfo?.permissions || [],
    isManager() {
      return this.roles.includes('admin') || this.roles.includes('librarian')
    },
    isAuthenticated: (state) => Boolean(state.token)
  },
  actions: {
    hydrate() {
      this.token = getAccessToken()
      this.refreshToken = getRefreshToken()
      this.userInfo = getStoredUserInfo()
      this.initialized = true

      if (!authListenerBound) {
        window.addEventListener('auth:changed', () => {
          this.token = getAccessToken()
          this.refreshToken = getRefreshToken()
          this.userInfo = getStoredUserInfo()
        })
        authListenerBound = true
      }
    },
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.user
      this.initialized = true
      return data
    },
    async fetchMe() {
      const data = await getMe()
      updateUserInfo(data)
      this.userInfo = data
      return data
    },
    setSession(payload) {
      setAuthSession(payload)
      this.token = getAccessToken()
      this.refreshToken = getRefreshToken()
      this.userInfo = getStoredUserInfo()
    },
    clearSession() {
      clearAuthSession()
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      this.initialized = true
    },
    async logout() {
      try {
        if (this.token) {
          await logoutApi()
        }
      } finally {
        this.clearSession()
      }
    },
    resolveRedirect(redirect) {
      return buildLoginRedirect(redirect)
    }
  }
})