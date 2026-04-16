import request from '@/utils/request'

export const getBookList = params => request.get('/books', { params })
export const getBookById = id => request.get(`/books/${id}`)
export const createBook = data => request.post('/books', data)
export const updateBook = (id, data) => request.put(`/books/${id}`, data)
export const deleteBook = id => request.delete(`/books/${id}`)
export const updateBookStatus = (id, status) => request.put(`/books/${id}/status`, null, { params: { status } })
export const adjustStock = (id, data) => request.put(`/books/${id}/stock`, data)
