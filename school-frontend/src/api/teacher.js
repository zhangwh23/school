import request from '@/utils/request'

export function getTeachers(params) {
  return request.get('/teachers', { params })
}

export function getTeacher(id) {
  return request.get(`/teachers/${id}`)
}

export function createTeacher(data) {
  return request.post('/teachers', data)
}

export function updateTeacher(id, data) {
  return request.put(`/teachers/${id}`, data)
}

export function deleteTeacher(id) {
  return request.delete(`/teachers/${id}`)
}
