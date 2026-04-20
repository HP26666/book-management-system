import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  clearAuthSession,
  getAccessToken,
  getRefreshToken,
  setAuthSession,
  updateAccessToken
} from '../utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

export const rawClient = axios.create({
  timeout: 10000
})

let refreshPromise = null

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    window.location.replace(`/login?redirect=${redirect}`)
  }
}

async function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post('/api/auth/refresh', {
        refreshToken: getRefreshToken()
      })
      .then((response) => {
        const payload = response.data
        if (payload?.code !== 200) {
          throw new Error(payload?.message || '刷新登录态失败')
        }
        updateAccessToken(payload.data.accessToken)
        return payload.data.accessToken
      })
      .catch((error) => {
        clearAuthSession()
        redirectToLogin()
        throw error
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

request.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  async (response) => {
    const payload = response.data
    if (payload && typeof payload.code !== 'undefined') {
      if (payload.code === 200) {
        return payload.data
      }

      const businessError = new Error(payload.message || '请求失败')
      businessError.code = payload.code
      businessError.payload = payload
      throw businessError
    }

    return payload
  },
  async (error) => {
    const originalRequest = error.config || {}
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '请求失败'

    if (status === 401 && !originalRequest.__retried && !originalRequest.skipAuthRefresh && getRefreshToken()) {
      try {
        originalRequest.__retried = true
        const accessToken = await refreshAccessToken()
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return request(originalRequest)
      } catch {
        return Promise.reject(error)
      }
    }

    if (status === 401) {
      clearAuthSession()
      redirectToLogin()
    }

    if (!originalRequest.silentError) {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export function persistLoginResponse(payload) {
  setAuthSession({
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    user: payload.user
  })
}

export default request