const ACCESS_TOKEN_KEY = 'bookms-access-token'
const REFRESH_TOKEN_KEY = 'bookms-refresh-token'
const USER_INFO_KEY = 'bookms-user-info'

function emitAuthChanged(detail = {}) {
  window.dispatchEvent(new CustomEvent('auth:changed', { detail }))
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || ''
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

export function getStoredUserInfo() {
  const raw = localStorage.getItem(USER_INFO_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

export function setAuthSession({ accessToken = '', refreshToken = '', user = null } = {}) {
  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  }
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
  if (user) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))
  }
  emitAuthChanged({ accessToken, refreshToken, user })
}

export function updateAccessToken(accessToken) {
  if (!accessToken) {
    return
  }
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  emitAuthChanged({ accessToken, refreshToken: getRefreshToken(), user: getStoredUserInfo() })
}

export function updateUserInfo(user) {
  if (!user) {
    return
  }
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))
  emitAuthChanged({ accessToken: getAccessToken(), refreshToken: getRefreshToken(), user })
}

export function clearAuthSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
  emitAuthChanged({ accessToken: '', refreshToken: '', user: null })
}

export function buildLoginRedirect(toPath = '/dashboard') {
  if (!toPath || toPath === '/login') {
    return '/dashboard'
  }

  return toPath
}