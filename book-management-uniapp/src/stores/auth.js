import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(uni.getStorageSync('accessToken') || '')
  const userInfo = ref(JSON.parse(uni.getStorageSync('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')

  const setAuth = (accessToken, rt, user) => {
    token.value = accessToken
    userInfo.value = user
    uni.setStorageSync('accessToken', accessToken)
    uni.setStorageSync('refreshToken', rt)
    uni.setStorageSync('userInfo', JSON.stringify(user))
  }

  const doLogin = async (uname, password) => {
    const res = await authApi.login({ username: uname, password })
    if (res.code === 200 && res.data) {
      setAuth(res.data.accessToken, res.data.refreshToken, res.data.user)
    }
    return res
  }

  const doWechatLogin = () => {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          try {
            const res = await authApi.wechatLogin(loginRes.code)
            if (res.code === 200 && res.data) {
              setAuth(res.data.accessToken, res.data.refreshToken, res.data.user)
            }
            resolve(res)
          } catch (e) {
            reject(e)
          }
        },
        fail: () => reject(new Error('微信登录失败'))
      })
    })
  }

  const doLogout = () => {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('userInfo')
  }

  const fetchUserInfo = async () => {
    try {
      const res = await authApi.getMyInfo()
      if (res.code === 200 && res.data) {
        userInfo.value = res.data
        uni.setStorageSync('userInfo', JSON.stringify(res.data))
      }
    } catch (_) { /* ignore */ }
  }

  return { token, userInfo, isLoggedIn, username, setAuth, doLogin, doWechatLogin, doLogout, fetchUserInfo }
})
