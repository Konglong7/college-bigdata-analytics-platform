import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 如果返回没有 code 或是二进制流等则直接返回
    if (res && typeof res.code === 'number') {
      if (res.code === 200) {
        return res.data
      } else {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || 'Error'))
      }
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      ElMessage.error('登录状态已失效，请重新登录')
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    } else if (status === 403) {
      ElMessage.warning('权限不足，无法执行该操作')
    } else {
      const msg = error.response?.data?.message || error.message || '网络连接异常'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default service
