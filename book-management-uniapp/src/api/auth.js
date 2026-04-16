import { post, get } from '../utils/request'

export const login = (data) => post('/api/auth/login', data)
export const register = (data) => post('/api/auth/register', data)
export const wechatLogin = (code) => post('/api/auth/wechat-login', { code })
export const refreshToken = (refreshToken) => post('/api/auth/refresh', { refreshToken })
export const getMyInfo = () => get('/api/auth/me')
export const logout = () => post('/api/auth/logout')
