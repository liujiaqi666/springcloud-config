import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'exam/list',
        name: 'ExamList',
        component: () => import('@/views/exam/ExamList.vue'),
        meta: { title: '考试列表' }
      },
      {
        path: 'exam/take/:id',
        name: 'TakeExam',
        component: () => import('@/views/exam/TakeExam.vue'),
        meta: { title: '参加考试' }
      },
      {
        path: 'exam/result/:id',
        name: 'ExamResult',
        component: () => import('@/views/exam/ExamResult.vue'),
        meta: { title: '考试结果' }
      },
      {
        path: 'question/list',
        name: 'QuestionList',
        component: () => import('@/views/question/QuestionList.vue'),
        meta: { title: '题库管理', roles: ['TEACHER'] }
      },
      {
        path: 'paper/list',
        name: 'PaperList',
        component: () => import('@/views/paper/PaperList.vue'),
        meta: { title: '试卷管理', roles: ['TEACHER'] }
      },
      {
        path: 'arrangement/list',
        name: 'ArrangementList',
        component: () => import('@/views/arrangement/ArrangementList.vue'),
        meta: { title: '考试安排', roles: ['TEACHER'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
