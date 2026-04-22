import request from '@/utils/request'

export function getGrades(params) {
  return request.get('/grades', { params })
}

export function createGrade(data) {
  return request.post('/grades', data)
}

export function updateGrade(id, data) {
  return request.put(`/grades/${id}`, data)
}

export function getGradeStatistics(params) {
  return request.get('/grades/statistics', { params })
}
