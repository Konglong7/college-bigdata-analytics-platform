<template>
  <header class="app-header">
    <!-- 顶部环境光微晕 -->
    <div class="header-glow"></div>

    <!-- 左侧导航区 -->
    <div class="header-left">
      <nav class="nav-menu">
        <button
          v-for="item in leftNavs"
          :key="item.path"
          :class="['nav-item', { active: isNavActive(item.path) }]"
          @click="navigate(item.path)"
        >
          <span class="nav-text">{{ item.name }}</span>
          <span v-if="isNavActive(item.path)" class="nav-indicator"></span>
        </button>
      </nav>
    </div>

    <!-- 中间品牌与标题区 -->
    <div class="header-center" @click="navigate('/dashboard')">
      <div class="brand-badge">
        <span class="pulse-dot"></span>
        <span class="badge-text">ACADEMIC INTELLIGENCE PLATFORM</span>
      </div>
      <h1 class="brand-title">全国高校大数据分析可视化平台</h1>
    </div>

    <!-- 右侧导航与状态控制区 -->
    <div class="header-right">
      <nav class="nav-menu">
        <button
          v-for="item in rightNavs"
          :key="item.path"
          :class="['nav-item', { active: isNavActive(item.path) }]"
          @click="navigate(item.path)"
        >
          <span class="nav-text">{{ item.name }}</span>
          <span v-if="isNavActive(item.path)" class="nav-indicator"></span>
        </button>
      </nav>

      <div class="header-divider"></div>

      <!-- 工具操作区：全屏与主题切换 -->
      <div class="tool-actions">
        <!-- 全屏演示切换 -->
        <button
          class="tool-btn"
          :title="isFullscreen ? '退出全屏' : '进入全屏大屏演示汇报模式'"
          @click="handleToggleFullscreen"
        >
          <svg v-if="!isFullscreen" viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"></path>
          </svg>
          <svg v-else viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M8 3v3a2 2 0 0 1-2 2H3m18 0h-3a2 2 0 0 1-2-2V3m0 18v-3a2 2 0 0 1 2-2h3M3 16h3a2 2 0 0 1 2 2v3"></path>
          </svg>
          <span class="btn-label">{{ isFullscreen ? '窗口' : '全屏' }}</span>
        </button>

        <!-- 深色/浅色主题双模切换 -->
        <button
          class="tool-btn theme-toggle-btn"
          :title="currentTheme === 'dark' ? '切换为浅色白模式 (适合毕业论文报告截图)' : '切换为暗黑模式 (适合大屏投屏演示)'"
          @click="toggleTheme"
        >
          <span class="theme-icon">{{ currentTheme === 'dark' ? '☀️' : '🌙' }}</span>
          <span class="btn-label">{{ currentTheme === 'dark' ? '浅色' : '深色' }}</span>
        </button>
      </div>

      <!-- 用户信息与退出 -->
      <div class="user-meta">
        <div class="user-badge" :title="'当前用户: ' + username">
          <div class="user-avatar">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </div>
          <span class="user-name">{{ username }}</span>
        </div>

        <button class="logout-btn" title="退出系统" @click="handleLogout">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          <span>退出</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { currentTheme, toggleTheme } from '@/utils/theme'

const route = useRoute()
const router = useRouter()

const username = computed(() => localStorage.getItem('username') || '访客')

const leftNavs = [
  { name: '数据驾驶舱', path: '/dashboard' },
  { name: '高校查询', path: '/university' },
  { name: '志愿推荐', path: '/recommend' },
  { name: '高校对比', path: '/compare' },
  { name: '专业分析', path: '/major' },
  { name: '招生分析', path: '/enrollment' }
]

const rightNavs = [
  { name: '数据采集', path: '/collect' },
  { name: '数据清洗', path: '/clean' },
  { name: '趋势预测', path: '/predict' },
  { name: '数据仓库', path: '/warehouse' },
  { name: '系统后台', path: '/admin' }
]

// 全屏状态
const isFullscreen = ref(false)

const updateFullscreenState = () => {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  document.addEventListener('fullscreenchange', updateFullscreenState)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', updateFullscreenState)
})

const handleToggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(() => {
      ElMessage.warning('当前环境不支持全屏切换')
    })
  } else {
    document.exitFullscreen().catch(() => {})
  }
}

const isNavActive = (path: string) => {
  if (path === '/university') {
    return route.path.startsWith('/university')
  }
  return route.path === path
}

const navigate = (path: string) => {
  router.push(path)
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前系统吗？', '系统退出提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    ElMessage.success('已安全退出')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.app-header {
  height: var(--header-height);
  background: var(--header-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--header-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 18px;
  position: relative;
  z-index: 100;
  user-select: none;
  flex-shrink: 0;
  box-shadow: 0 4px 24px -2px rgba(0, 0, 0, 0.2);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.header-glow {
  position: absolute;
  top: 0;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(56, 189, 248, 0.6), transparent);
  pointer-events: none;
}

html.light .header-glow {
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.4), transparent);
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.header-left {
  justify-content: flex-start;
}

.header-right {
  justify-content: flex-end;
  gap: 10px;
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0 12px;
  flex-shrink: 0;
  white-space: nowrap;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.92;
  }

  .brand-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 2px;
    white-space: nowrap;

    .pulse-dot {
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: #10b981;
      box-shadow: 0 0 8px #10b981;
      animation: pulse-ring 2s infinite ease-in-out;
    }

    .badge-text {
      font-size: 9px;
      font-weight: 600;
      letter-spacing: 1.5px;
      color: var(--text-sub);
      font-family: var(--font-mono);
      white-space: nowrap;
    }
  }

  .brand-title {
    font-size: 17px;
    font-weight: 700;
    letter-spacing: 1.2px;
    margin: 0;
    white-space: nowrap;
    background: linear-gradient(180deg, #ffffff 0%, #e0f2fe 75%, #bae6fd 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.4));
  }
}

html.light .brand-title {
  background: linear-gradient(180deg, #0284c7 0%, #0369a1 100%) !important;
  -webkit-background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  filter: none !important;
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.6; }
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 3px;
  flex-wrap: nowrap;
}

.nav-item {
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-sub);
  font-size: 12.5px;
  font-weight: 500;
  cursor: pointer;
  padding: 5px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
  white-space: nowrap;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;

  &:hover {
    color: var(--text-main);
    background: rgba(148, 163, 184, 0.1);
  }

  &.active {
    color: var(--primary-light);
    background: rgba(14, 165, 233, 0.12);
    border-color: rgba(14, 165, 233, 0.35);

    .nav-indicator {
      position: absolute;
      bottom: -1px;
      left: 15%;
      right: 15%;
      height: 2px;
      background: var(--primary-light);
      border-radius: 1px;
    }
  }
}

html.light .nav-item.active {
  color: #0284c7;
  background: #e0f2fe;
  border-color: #7dd3fc;
  .nav-indicator {
    background: #0284c7;
  }
}

.header-divider {
  width: 1px;
  height: 22px;
  background: var(--border-color);
  flex-shrink: 0;
}

.tool-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  color: var(--text-sub);
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 11.5px;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;

  .theme-icon {
    font-size: 13px;
    line-height: 1;
  }

  &:hover {
    color: var(--primary-light);
    border-color: var(--primary-light);
    background: rgba(14, 165, 233, 0.1);
  }
}

html.light .tool-btn:hover {
  color: #0284c7;
  border-color: #0284c7;
  background: #e0f2fe;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-main);

  .user-avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(14, 165, 233, 0.25), rgba(99, 102, 241, 0.25));
    border: 1px solid var(--border-color);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary-light);
  }

  .user-name {
    font-weight: 500;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(244, 63, 94, 0.1);
  border: 1px solid rgba(244, 63, 94, 0.3);
  color: var(--danger);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11.5px;
  transition: all 0.2s ease;

  &:hover {
    background: var(--danger);
    border-color: var(--danger);
    color: #ffffff;
    box-shadow: 0 0 10px rgba(244, 63, 94, 0.35);
  }
}

html.light {
  .user-badge {
    background: #ffffff !important;
    border-color: #e2e8f0 !important;
    box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05) !important;
  }

  .logout-btn {
    background: #fff1f2 !important;
    border-color: #fecdd3 !important;
    color: #e11d48 !important;
    &:hover {
      background: #e11d48 !important;
      color: #ffffff !important;
      border-color: #e11d48 !important;
      box-shadow: 0 2px 8px rgba(225, 29, 72, 0.25) !important;
    }
  }
}

@media (max-width: 1536px) {
  .brand-title {
    font-size: 15px !important;
  }
  .nav-item {
    padding: 3.5px 5.5px !important;
    font-size: 11.5px !important;
  }
}

@media (max-width: 1366px) {
  .app-header {
    padding: 0 10px !important;
  }
  .brand-badge {
    display: none !important;
  }
  .brand-title {
    font-size: 14px !important;
  }
  .nav-item {
    padding: 3px 4.5px !important;
    font-size: 11px !important;
  }
  .btn-label {
    display: none; /* 较窄视口仅保留图标 */
  }
}

@media (max-width: 1120px) {
  .brand-title {
    font-size: 13px !important;
  }
  .user-meta .user-name {
    display: none;
  }
}
</style>
