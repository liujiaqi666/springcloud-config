import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const login = async (loginForm) => {
    try {
      const res = await api.login(loginForm)
      token.value = res.data.token
      userInfo.value = res.data.userInfo
      localStorage.setItem('token', res.data.token)
      return res.data
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  const getToken = () => token.value

  return {
    token,
    userInfo,
    login,
    logout,
    getToken
  }
})
