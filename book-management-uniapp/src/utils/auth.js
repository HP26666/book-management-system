const ACCESS_TOKEN_KEY = 'bookms:access-token'
const REFRESH_TOKEN_KEY = 'bookms:refresh-token'
const USER_INFO_KEY = 'bookms:user-info'
const READER_INFO_KEY = 'bookms:reader-info'

export function getAccessToken() {
  return uni.getStorageSync(ACCESS_TOKEN_KEY) || ''
}

export function getRefreshToken() {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || ''
}

export function getStoredUserInfo() {
  return uni.getStorageSync(USER_INFO_KEY) || null
}

export function getStoredReaderInfo() {
  return uni.getStorageSync(READER_INFO_KEY) || null
}

export function setAuthSession(payload) {
  uni.setStorageSync(ACCESS_TOKEN_KEY, payload.accessToken || '')
  uni.setStorageSync(REFRESH_TOKEN_KEY, payload.refreshToken || '')
  uni.setStorageSync(USER_INFO_KEY, payload.user || null)
}

export function updateAccessToken(token) {
  uni.setStorageSync(ACCESS_TOKEN_KEY, token || '')
}

export function updateUserInfo(user) {
  uni.setStorageSync(USER_INFO_KEY, user || null)
}

export function updateReaderInfo(reader) {
  uni.setStorageSync(READER_INFO_KEY, reader || null)
}

export function clearAuthSession() {
  uni.removeStorageSync(ACCESS_TOKEN_KEY)
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
  uni.removeStorageSync(USER_INFO_KEY)
  uni.removeStorageSync(READER_INFO_KEY)
}