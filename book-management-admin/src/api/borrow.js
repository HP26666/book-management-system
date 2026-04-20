import request from './request'

export function getBorrowList(params) {
  return request.get('/borrows', { params })
}

export function getBorrow(id) {
  return request.get(`/borrows/${id}`)
}

export function applyBorrow(payload) {
  return request.post('/borrows', payload)
}

export function approveBorrow(id) {
  return request.put(`/borrows/${id}/approve`)
}

export function rejectBorrow(id, payload) {
  return request.put(`/borrows/${id}/reject`, payload)
}

export function returnBorrow(id) {
  return request.put(`/borrows/${id}/return`)
}

export function renewBorrow(id) {
  return request.put(`/borrows/${id}/renew`)
}