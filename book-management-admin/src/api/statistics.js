import request from '@/utils/request'

export const getDashboardStats = () => request.get('/statistics/dashboard')
