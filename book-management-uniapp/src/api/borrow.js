import { get, post, put } from '../utils/request'

export const applyBorrow = (data) => post('/api/borrows', data)
export const getBorrows = (params) => get('/api/borrows', params)
export const getBorrowDetail = (id) => get(`/api/borrows/${id}`)
export const renewBorrow = (id) => put(`/api/borrows/${id}/renew`)
