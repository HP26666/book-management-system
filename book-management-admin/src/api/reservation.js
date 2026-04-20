import request from './request'

export function getReservationList(params) {
  return request.get('/reservations', { params })
}

export function createReservation(payload) {
  return request.post('/reservations', payload)
}

export function cancelReservation(id) {
  return request.put(`/reservations/${id}/cancel`)
}