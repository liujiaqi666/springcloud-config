import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/exam/list',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: 'exam/list',
        name: 'ExamList',
        component: () => import('@/views/exam/ExamList.vue'),
        meta: { title: '考试列表' }
      },
      {
        path: 'exam/take/:id',
        name: 'TakeExam',
        component: () => import('@/views/exam/ExamTaking.vue'),
        meta: { title: '参加考试' }
      },
      {
        path: 'paper/list',
        name: 'PaperList',
        component: () => import('@/views/paper/PaperList.vue'),
        meta: { title: '试卷管理' }
      },
      {
        path: 'result/list',
        name: 'ResultList',
        component: () => import('@/views/result/ResultList.vue'),
        meta: { title: '考试成绩' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
