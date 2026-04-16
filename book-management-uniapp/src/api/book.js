import { get } from '../utils/request'

export const getBooks = (params) => get('/api/books', params)
export const getBookDetail = (id) => get(`/api/books/${id}`)
export const getBookByIsbn = (isbn) => get(`/api/books/isbn/${isbn}`)
export const getCategoryTree = () => get('/api/categories/tree')
