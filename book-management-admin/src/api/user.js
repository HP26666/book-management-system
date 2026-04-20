import request from './request'

export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUser(id) {
  return request.get(`/users/${id}`)
}

export function createUser(payload) {
  return request.post('/users', payload)
}

export function updateUser(id, payload) {
  return request.put(`/users/${id}`, payload)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function assignRoles(id, payload) {
  return request.put(`/users/${id}/roles`, payload)
}