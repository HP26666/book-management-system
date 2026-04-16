import request from '@/utils/request'

export const getBorrowList = params => request.get('/borrows', { params })
export const getMyBorrows = () => request.get('/borrows/mine')
export const applyBorrow = data => request.post('/borrows', data)
export const approveBorrow = id => request.put(`/borrows/${id}/approve`)
export const rejectBorrow = (id, reason) => request.put(`/borrows/${id}/reject`, null, { params: { reason } })
export const returnBook = id => request.put(`/borrows/${id}/return`)
export const renewBorrow = id => request.put(`/borrows/${id}/renew`)
export const confirmReturn = id => request.put(`/borrows/${id}/confirm-return`)
