/**
 * 统一请求封装 - 基于 uni.request
 */

// #ifdef H5
const BASE_URL = ''
// #endif
// #ifndef H5
const BASE_URL = 'http://localhost:8080'
// #endif

let isRefreshing = false
let pendingRequests = []

const processQueue = (error, token = null) => {
  pendingRequests.forEach(({ resolve, reject }) => {
    error ? reject(error) : resolve(token)
  })
  pendingRequests = []
}

const refreshToken = () => {
  return new Promise((resolve, reject) => {
    const rt = uni.getStorageSync('refreshToken')
    if (!rt) return reject(new Error('无刷新令牌'))
    uni.request({
      url: BASE_URL + '/api/auth/refresh',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { refreshToken: rt },
      success: (res) => {
        if (res.statusCode === 200 && res.data?.data) {
          const { accessToken, refreshToken: newRt } = res.data.data
          uni.setStorageSync('accessToken', accessToken)
          if (newRt) uni.setStorageSync('refreshToken', newRt)
          resolve(accessToken)
        } else {
          reject(new Error('刷新失败'))
        }
      },
      fail: () => reject(new Error('网络错误'))
    })
  })
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('accessToken')
    const header = { 'Content-Type': 'application/json', ...options.header }
    if (token) header['Authorization'] = `Bearer ${token}`

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: async (res) => {
        if (res.statusCode === 401) {
          if (!isRefreshing) {
            isRefreshing = true
            try {
              const newToken = await refreshToken()
              isRefreshing = false
              processQueue(null, newToken)
              // 重试原请求
              request(options).then(resolve).catch(reject)
            } catch (err) {
              isRefreshing = false
              processQueue(err)
              uni.removeStorageSync('accessToken')
              uni.removeStorageSync('refreshToken')
              uni.removeStorageSync('userInfo')
              uni.navigateTo({ url: '/pages/login/index' })
              reject(err)
            }
          } else {
            // 排队等待刷新完成
            new Promise((r, rj) => pendingRequests.push({ resolve: r, reject: rj }))
              .then(() => request(options).then(resolve).catch(reject))
              .catch(reject)
          }
          return
        }
        if (res.statusCode === 403) {
          uni.showToast({ title: '无访问权限', icon: 'none' })
          reject(new Error('无访问权限'))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const msg = res.data?.message || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail: () => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(new Error('网络错误'))
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })
export default request
