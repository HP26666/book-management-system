import request, { persistLoginResponse } from './request'

export async function login(payload) {
  const data = await request.post('/auth/login', payload)
  persistLoginResponse(data)
  return data
}

export function register(payload) {
  return request.post('/auth/register', payload)
}

export function logout() {
  return request.post('/auth/logout')
}

export function refresh(payload) {
  return request.post('/auth/refresh', payload, { skipAuthRefresh: true })
}

export function getMe() {
  return request.get('/auth/me')
}

export async function wechatLogin(payload) {
  const data = await request.post('/auth/wechat-login', payload)
  persistLoginResponse(data)
  return data
}