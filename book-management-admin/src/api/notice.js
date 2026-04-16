import request from '@/utils/request'

export const getNoticeList = params => request.get('/notices', { params })
export const getPublishedNotices = () => request.get('/notices/published')
export const getNoticeById = id => request.get(`/notices/${id}`)
export const createNotice = data => request.post('/notices', data)
export const updateNotice = (id, data) => request.put(`/notices/${id}`, data)
export const deleteNotice = id => request.delete(`/notices/${id}`)
