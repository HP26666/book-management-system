import request from './request'

export function getReaderList(params) {
  return request.get('/readers', { params })
}

export function getReader(id) {
  return request.get(`/readers/${id}`)
}

export function createReader(payload) {
  return request.post('/readers', payload)
}

export function updateReader(id, payload) {
  return request.put(`/readers/${id}`, payload)
}

export function issueCard(id) {
  return request.put(`/readers/${id}/card`)
}

export function getMyReaderInfo() {
  return request.get('/readers/me')
}