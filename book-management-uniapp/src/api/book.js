import { http } from '../utils/request'

export function getBookList(params) {
  return http.get('/books', params)
}

export function getBookDetail(id) {
  return http.get(`/books/${id}`)
}