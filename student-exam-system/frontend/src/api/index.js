import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回的状态码不是 200，说明接口有错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401: 未授权，需要重新登录
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('Response error:', error)
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// API 接口定义
export default {
  // 登录
  login(data) {
    return service.post('/auth/login', data)
  },

  // 获取考试列表
  getExamList(page, size) {
    return service.get('/exam/list', { params: { page, size } })
  },

  // 开始考试
  startExam(examId) {
    return service.post(`/exam/${examId}/start`)
  },

  // 提交考试
  submitExam(examId, answers) {
    return service.post(`/exam/${examId}/submit`, { answers })
  },

  // 获取试卷列表
  getPaperList(page, size) {
    return service.get('/paper/list', { params: { page, size } })
  },

  // 添加试卷
  addPaper(data) {
    return service.post('/paper', data)
  },

  // 更新试卷
  updatePaper(data) {
    return service.put('/paper', data)
  },

  // 删除试卷
  deletePaper(id) {
    return service.delete(`/paper/${id}`)
  },

  // 获取成绩列表
  getResultList(params) {
    return service.get('/result/list', { params })
  },

  // 获取成绩详情
  getResultDetail(id) {
    return service.get(`/result/${id}/detail`)
  },

  // 获取题目列表
  getQuestionList(params) {
    return service.get('/question/list', { params })
  },

  // 添加题目
  addQuestion(data) {
    return service.post('/question', data)
  },

  // 更新题目
  updateQuestion(data) {
    return service.put('/question', data)
  },

  // 删除题目
  deleteQuestion(id) {
    return service.delete(`/question/${id}`)
  }
}
