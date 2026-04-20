import request from './request'

export function getCategoryTree() {
  return request.get('/categories/tree')
}

export function getCategoryList(params) {
  return request.get('/categories', { params })
}

export function createCategory(payload) {
  return request.post('/categories', payload)
}

export function updateCategory(id, payload) {
  return request.put(`/categories/${id}`, payload)
}

export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}