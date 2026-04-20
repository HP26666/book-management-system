import { http } from '../utils/request'

export function getBorrowList(params) {
  return http.get('/borrows', params)
}

export function applyBorrow(payload) {
  return http.post('/borrows', payload)
}

export function renewBorrow(id) {
  return http.put(`/borrows/${id}/renew`)
}