import request from './request'

export function getBookList(params) {
  return request.get('/books', { params })
}

export function getBook(id) {
  return request.get(`/books/${id}`)
}

export function createBook(payload) {
  return request.post('/books', payload)
}

export function updateBook(id, payload) {
  return request.put(`/books/${id}`, payload)
}

export function deleteBook(id) {
  return request.delete(`/books/${id}`)
}

export function parseIsbn(isbn) {
  return request.get(`/books/isbn/${isbn}`)
}

export function adjustStock(id, payload) {
  return request.put(`/books/${id}/stock`, payload)
}