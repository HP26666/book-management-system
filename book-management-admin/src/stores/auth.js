import { defineStore } from 'pinia'
import { login, register, logout, refreshToken } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    username: state => state.user?.username || '',
    realName: state => state.user?.realName || '',
    avatarUrl: state => state.user?.avatarUrl || '',
    roles: state => state.user?.roles || [],
    isAdmin: state => state.user?.roles?.includes('admin') || false,
    isLibrarian: state => state.user?.roles?.includes('librarian') || false
  },

  actions: {
    async doLogin(loginForm) {
      const res = await login(loginForm)
      const data = res.data
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data
    },

    async doRegister(registerForm) {
      const res = await register(registerForm)
      const data = res.data
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data
    },

    async doLogout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.refreshToken = ''
        this.user = null
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
      }
    },

    async doRefreshToken() {
      const res = await refreshToken({ refreshToken: this.refreshToken })
      const data = res.data
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      return data
    }
  }
})
