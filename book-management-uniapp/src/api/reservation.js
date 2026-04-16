import { get, post, put } from '../utils/request'

export const createReservation = (data) => post('/api/reservations', data)
export const getReservations = (params) => get('/api/reservations', params)
export const cancelReservation = (id) => put(`/api/reservations/${id}/cancel`)
