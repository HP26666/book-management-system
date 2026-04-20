import { clearAuthSession, getAccessToken, getRefreshToken, updateAccessToken } from './auth'
import { getBaseUrl } from './config'

let refreshPromise = null

function showError(message) {
  uni.showToast({
    title: message || '请求失败',
    icon: 'none',
    duration: 2000
  })
}

function normalizeError(error) {
  if (typeof error === 'string') {
    return { message: error }
  }
  return error || { message: '请求失败' }
}

function rawRequest(options) {
  const token = getAccessToken()
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${getBaseUrl()}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      timeout: options.timeout || 15000,
      header: {
        'Content-Type': 'application/json',
        ...(options.header || {}),
        ...(token && !options.skipAuth ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (response) => {
        const payload = response.data || {}
        if (response.statusCode >= 200 && response.statusCode < 300) {
          if (typeof payload.code !== 'undefined') {
            if (payload.code === 200) {
              resolve(payload.data)
              return
            }
            reject({ statusCode: response.statusCode, message: payload.message || '请求失败', payload })
            return
          }
          resolve(payload)
          return
        }

        reject({
          statusCode: response.statusCode,
          message: payload.message || response.errMsg || '请求失败',
          payload
        })
      },
      fail: (error) => {
        reject({ statusCode: 0, message: error.errMsg || '网络异常' })
      }
    })
  })
}

async function refreshAccessToken() {
  if (!refreshPromise) {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      throw new Error('登录已失效')
    }

    refreshPromise = rawRequest({
      url: '/auth/refresh',
      method: 'POST',
      data: { refreshToken },
      skipAuth: true,
      timeout: 10000
    })
      .then((data) => {
        updateAccessToken(data.accessToken)
        return data.accessToken
      })
      .catch((error) => {
        clearAuthSession()
        throw error
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

export async function request(options) {
  try {
    return await rawRequest(options)
  } catch (error) {
    const normalized = normalizeError(error)
    const canRetry = normalized.statusCode === 401 && !options.__retried && !options.skipAuthRefresh && getRefreshToken()

    if (canRetry) {
      try {
        await refreshAccessToken()
        return await request({ ...options, __retried: true })
      } catch (refreshError) {
        const finalError = normalizeError(refreshError)
        if (!options.silentError) {
          showError(finalError.message)
        }
        throw finalError
      }
    }

    if (normalized.statusCode === 401) {
      clearAuthSession()
    }

    if (!options.silentError) {
      showError(normalized.message)
    }

    throw normalized
  }
}

export const http = {
  get(url, data, options = {}) {
    return request({ url, data, method: 'GET', ...options })
  },
  post(url, data, options = {}) {
    return request({ url, data, method: 'POST', ...options })
  },
  put(url, data, options = {}) {
    return request({ url, data, method: 'PUT', ...options })
  },
  delete(url, data, options = {}) {
    return request({ url, data, method: 'DELETE', ...options })
  }
}