import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const roles = ref([])

  const isLoggedIn = computed(() => !!token.value)

  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isTeacher = computed(() => roles.value.includes('TEACHER'))
  const isStudent = computed(() => roles.value.includes('STUDENT'))

  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    roles.value = res.data.roles
    localStorage.setItem('token', res.data.token)
    return res
  }

  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    roles.value = res.data.roles
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    localStorage.removeItem('token')
  }

  return {
    token, userInfo, roles,
    isLoggedIn, isAdmin, isTeacher, isStudent,
    login, fetchUserInfo, logout
  }
})
