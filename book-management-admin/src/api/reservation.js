import request from '@/utils/request'

export const getReservationList = params => request.get('/reservations', { params })
export const getMyReservations = () => request.get('/reservations/mine')
export const createReservation = data => request.post('/reservations', data)
export const cancelReservation = id => request.put(`/reservations/${id}/cancel`)
