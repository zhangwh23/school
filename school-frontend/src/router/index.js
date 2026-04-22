import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'students',
        name: 'Students',
        component: () => import('@/views/student/StudentList.vue'),
        meta: { title: '学生管理', icon: 'User', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'teachers',
        name: 'Teachers',
        component: () => import('@/views/teacher/TeacherList.vue'),
        meta: { title: '教师管理', icon: 'Avatar', roles: ['ADMIN'] }
      },
      {
        path: 'classes',
        name: 'Classes',
        component: () => import('@/views/clazz/ClazzList.vue'),
        meta: { title: '班级管理', icon: 'School', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/course/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Reading', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'grades',
        name: 'Grades',
        component: () => import('@/views/grade/GradeList.vue'),
        meta: { title: '成绩管理', icon: 'Document', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理', icon: 'Setting', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title || ''} - 校园管理系统`

  const userStore = useUserStore()

  if (to.meta.public) {
    if (userStore.isLoggedIn) {
      next({ path: '/' })
    } else {
      next()
    }
    return
  }

  if (!userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
      next({ path: '/login' })
      return
    }
  }

  if (to.meta.roles && !to.meta.roles.some(role => userStore.roles.includes(role))) {
    next({ path: '/dashboard' })
    return
  }

  next()
})

export default router
