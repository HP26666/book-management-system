import { http } from '../utils/request'

export function wechatLogin(payload) {
  return http.post('/auth/wechat-login', payload, { skipAuth: true, skipAuthRefresh: true })
}

export function getMe() {
  return http.get('/auth/me')
}

export function logout() {
  return http.post('/auth/logout')
}