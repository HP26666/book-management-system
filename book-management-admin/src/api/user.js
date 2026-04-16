import request from '@/utils/request'

export const getUserList = params => request.get('/users', { params })
export const getUserById = id => request.get(`/users/${id}`)
export const createUser = data => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = id => request.delete(`/users/${id}`)
export const updateUserStatus = (id, status) => request.put(`/users/${id}/status`, null, { params: { status } })
export const resetPassword = id => request.put(`/users/${id}/reset-password`)
export const changePassword = data => request.put('/users/me/password', data)
export const updateProfile = data => request.put('/users/me', data)
