let defaultBaseUrl = 'http://localhost:8080/api'

// #ifdef MP-WEIXIN
defaultBaseUrl = 'http://127.0.0.1:8080/api'
// #endif

const BASE_URL_KEY = 'bookms:base-url'

export function getBaseUrl() {
  return uni.getStorageSync(BASE_URL_KEY) || defaultBaseUrl
}

export function setBaseUrl(url) {
  if (!url) {
    uni.removeStorageSync(BASE_URL_KEY)
    return
  }
  uni.setStorageSync(BASE_URL_KEY, url)
}

export function getDefaultBaseUrl() {
  return defaultBaseUrl
}