import request from '@/utils/request'

export function getClasses(params) {
  return request.get('/classes', { params })
}

export function getClazz(id) {
  return request.get(`/classes/${id}`)
}

export function createClazz(data) {
  return request.post('/classes', data)
}

export function updateClazz(id, data) {
  return request.put(`/classes/${id}`, data)
}

export function deleteClazz(id) {
  return request.delete(`/classes/${id}`)
}

export function assignStudents(id, studentIds) {
  return request.post(`/classes/${id}/students`, studentIds)
}
