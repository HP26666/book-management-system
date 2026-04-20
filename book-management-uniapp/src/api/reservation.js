import { http } from '../utils/request'

export function getReservationList(params) {
  return http.get('/reservations', params)
}

export function createReservation(payload) {
  return http.post('/reservations', payload)
}

export function cancelReservation(id) {
  return http.put(`/reservations/${id}/cancel`)
}