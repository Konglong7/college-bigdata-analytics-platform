import { ref } from 'vue'

export type AppTheme = 'dark' | 'light'

const THEME_KEY = 'app-theme'

// 响应式当前主题
export const currentTheme = ref<AppTheme>('dark')

// 主题切换事件监听器队列
type ThemeChangeCallback = (theme: AppTheme) => void
const listeners = new Set<ThemeChangeCallback>()

/**
 * 应用主题到 DOM 根节点及持久化
 */
export function applyTheme(theme: AppTheme) {
  currentTheme.value = theme
  const root = document.documentElement

  if (theme === 'dark') {
    root.classList.add('dark')
    root.classList.remove('light')
  } else {
    root.classList.remove('dark')
    root.classList.add('light')
  }

  try {
    localStorage.setItem(THEME_KEY, theme)
  } catch (e) {
    console.warn('Failed to save theme to localStorage', e)
  }

  // 广播通知已订阅的图表重新渲染
  listeners.forEach(fn => {
    try {
      fn(theme)
    } catch (err) {
      console.error('Error executing theme change callback:', err)
    }
  })
}

/**
 * 切换深色 / 浅色模式
 */
export function toggleTheme(): AppTheme {
  const next = currentTheme.value === 'dark' ? 'light' : 'dark'
  applyTheme(next)
  return next
}

/**
 * 初始化主题设置
 */
export function initTheme() {
  const saved = localStorage.getItem(THEME_KEY) as AppTheme | null
  if (saved === 'dark' || saved === 'light') {
    applyTheme(saved)
  } else {
    // 默认大屏科技感深色模式
    applyTheme('dark')
  }
}

/**
 * 订阅主题变化 (用于 ECharts 等组件自适应重绘)
 * @returns 取消订阅函数
 */
export function onThemeChange(callback: ThemeChangeCallback): () => void {
  listeners.add(callback)
  return () => {
    listeners.delete(callback)
  }
}

/**
 * 获取适配当前深浅色模式的 ECharts 视觉配色 Token
 */
export function getChartTheme(theme: AppTheme = currentTheme.value) {
  const isDark = theme === 'dark'
  return {
    isDark,
    textColor: isDark ? '#94a3b8' : '#475569',
    subTextColor: isDark ? '#8ba2d4' : '#64748b',
    titleColor: isDark ? '#f8fafc' : '#0f172a',
    axisLineColor: isDark ? '#334155' : '#cbd5e1',
    splitLineColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)',
    radarSplitArea: isDark
      ? ['rgba(5, 18, 43, 0.8)', 'rgba(8, 28, 68, 0.8)']
      : ['rgba(241, 245, 249, 0.7)', 'rgba(255, 255, 255, 0.9)'],
    radarSplitLine: isDark ? 'rgba(0, 229, 255, 0.2)' : 'rgba(14, 165, 233, 0.22)',
    radarAxisLine: isDark ? 'rgba(0, 229, 255, 0.3)' : 'rgba(14, 165, 233, 0.35)',
    tooltip: {
      backgroundColor: isDark ? 'rgba(10, 17, 33, 0.92)' : 'rgba(255, 255, 255, 0.96)',
      borderColor: isDark ? '#38bdf8' : '#cbd5e1',
      borderWidth: 1,
      textStyle: {
        color: isDark ? '#f8fafc' : '#0f172a',
        fontSize: 12
      },
      extraCssText: isDark
        ? 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.6); backdrop-filter: blur(8px);'
        : 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12); backdrop-filter: blur(8px);'
    }
  }
}

