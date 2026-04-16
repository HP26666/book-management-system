import request from '@/utils/request'

export const getReaderList = params => request.get('/readers', { params })
export const getReaderById = id => request.get(`/readers/${id}`)
export const updateReader = (id, data) => request.put(`/readers/${id}`, data)
export const getMyReaderInfo = () => request.get('/readers/mine')
