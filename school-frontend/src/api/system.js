import request from '@/utils/request'

export function getUsers(params) {
  return request.get('/system/users', { params })
}

export function createUser(data) {
  return request.post('/system/users', data)
}

export function updateUser(id, data) {
  return request.put(`/system/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/system/users/${id}`)
}

export function resetPassword(id) {
  return request.put(`/system/users/${id}/reset-password`)
}
