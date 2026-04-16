import { get } from '../utils/request'

export const getNotices = (params) => get('/api/notices', params)
export const getNoticeDetail = (id) => get(`/api/notices/${id}`)
