import request from './request'

export function getNoticeList(params) {
  return request.get('/notices', { params })
}

export function getNotice(id) {
  return request.get(`/notices/${id}`)
}

export function createNotice(payload) {
  return request.post('/notices', payload)
}

export function updateNotice(id, payload) {
  return request.put(`/notices/${id}`, payload)
}

export function deleteNotice(id) {
  return request.delete(`/notices/${id}`)
}