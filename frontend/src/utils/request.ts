import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 全局「云端服务唤醒中」状态。
 * Render 免费实例闲置 15 分钟后会休眠，被访问时容器需要 30~60 秒重新拉起，
 * 期间任何请求都会失败；App.vue 订阅该状态展示顶部提示条，避免用户误以为系统坏了。
 */
export const serviceWaking = ref(false)
export const serviceWakingHint = ref('')

/** 请求开始多久仍未成功，就判定为冷启动唤醒（避免正常快请求也弹提示） */
const WAKE_HINT_DELAY = 2500
/** 最大重试次数：1.5s + 3s + 6s + 12s + 24s ≈ 46.5s，足以覆盖冷启动窗口 */
const MAX_RETRY = 5
/** 指数退避基数（毫秒） */
const RETRY_BASE_DELAY = 1500
/** 超时时间：免费实例 CPU 仅 0.1，给足冷启动后的首次慢查询余量 */
const REQUEST_TIMEOUT = 30000

interface RetryableConfig extends InternalAxiosRequestConfig {
  /** 已重试次数 */
  _retryCount?: number
  /** 静默模式：不弹 ElMessage，由调用方自行处理错误态 */
  _silent?: boolean
}

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: REQUEST_TIMEOUT
})

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * 当前「故障连续段」的起始时间戳。
 * 只有出现一次成功响应才清零——不能在每次重试收尾处清零，
 * 否则退避过程中计时会被反复重置，唤醒提示条永远不会出现。
 */
let troubleStartedAt = 0

const beginRequest = () => {
  if (troubleStartedAt === 0) troubleStartedAt = Date.now()
}

/** 请求真正成功、或最终确认失败后，复位故障计时并收起提示条 */
const resetTrouble = () => {
  troubleStartedAt = 0
  serviceWaking.value = false
}

/** 超过阈值仍未成功 → 认定为冷启动，点亮唤醒提示 */
const markWakingIfSlow = () => {
  if (troubleStartedAt === 0) troubleStartedAt = Date.now()
  const elapsed = Date.now() - troubleStartedAt
  if (elapsed >= WAKE_HINT_DELAY) {
    serviceWaking.value = true
    serviceWakingHint.value = `云端演示服务正在唤醒（已等待 ${Math.round(elapsed / 1000)} 秒），首次打开约需 30~60 秒，请稍候…`
  }
}

/**
 * 判断错误是否值得重试。
 * 仅对 GET 幂等请求重试，避免重复提交写操作。
 * 覆盖三种冷启动表现：连接被拒 / 超时 / 网关 502、503。
 */
const isRetryable = (config?: RetryableConfig, status?: number): boolean => {
  if (!config) return false
  if ((config.method || 'get').toLowerCase() !== 'get') return false
  if ((config._retryCount ?? 0) >= MAX_RETRY) return false
  if (status !== undefined) return status >= 500
  return true
}

const scheduleRetry = async (config: RetryableConfig) => {
  config._retryCount = (config._retryCount ?? 0) + 1
  markWakingIfSlow()
  await delay(RETRY_BASE_DELAY * Math.pow(2, config._retryCount - 1))
  return service(config)
}

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    beginRequest()
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
    const config = response.config as RetryableConfig

    // 如果返回没有 code 或是二进制流等则直接返回
    if (res && typeof res.code === 'number') {
      if (res.code === 200) {
        resetTrouble()
        return res.data
      }

      // 后端 GlobalExceptionHandler 会把数据库/SQL 异常统一包成 HTTP 200 + code 500。
      // 冷启动阶段 H2 建表尚未完成时正是这种表现，必须重试而不是直接报错。
      if (res.code === 500 && isRetryable(config)) {
        return scheduleRetry(config)
      }

      if (!config._silent) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  async (error) => {
    const config = error.config as RetryableConfig | undefined
    const status = error.response?.status

    // 冷启动 / 网络抖动 → 退避重试
    if (isRetryable(config, error.response ? status : undefined)) {
      return scheduleRetry(config as RetryableConfig)
    }

    if ((config?._retryCount ?? 0) >= MAX_RETRY) {
      resetTrouble()
      ElMessage.error('云端演示服务唤醒超时，请刷新页面重试')
      return Promise.reject(error)
    }

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
    } else if (!config?._silent) {
      const msg = error.response?.data?.message || error.message || '网络连接异常'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default service
