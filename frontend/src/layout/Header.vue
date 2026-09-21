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

      <!-- 用户信息与操作 -->
      <div class="user-meta">
        <div class="user-badge" :title="'当前用户: ' + username">
          <div class="user-avatar">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </div>
          <span class="user-name">{{ username }}</span>
        </div>

        <button class="logout-btn" title="退出系统" @click="handleLogout">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

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
  background: rgba(10, 17, 33, 0.88);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(56, 189, 248, 0.16);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  position: relative;
  z-index: 100;
  user-select: none;
  flex-shrink: 0;
  box-shadow: 0 4px 24px -2px rgba(0, 0, 0, 0.5), inset 0 1px 0 0 rgba(255, 255, 255, 0.08);
}

.header-glow {
  position: absolute;
  top: 0;
  left: 25%;
  right: 25%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(56, 189, 248, 0.6), transparent);
  pointer-events: none;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  flex: 1;
}

.header-left {
  justify-content: flex-start;
}

.header-right {
  justify-content: flex-end;
  gap: 14px;
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0 16px;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.95;
  }

  .brand-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 2px;

    .pulse-dot {
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: #10b981;
      box-shadow: 0 0 8px #10b981;
      animation: pulse-ring 2s infinite ease-in-out;
    }

    .badge-text {
      font-size: 9.5px;
      font-weight: 600;
      letter-spacing: 1.5px;
      color: rgba(148, 163, 184, 0.85);
      font-family: var(--font-mono);
    }
  }

  .brand-title {
    font-size: 19px;
    font-weight: 700;
    letter-spacing: 1.8px;
    margin: 0;
    background: linear-gradient(180deg, #ffffff 0%, #e0f2fe 75%, #bae6fd 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.5));
  }
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.6; }
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-sub);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 6px 11px;
  border-radius: 6px;
  transition: all 0.2s ease;
  white-space: nowrap;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;

  &:hover {
    color: #e2e8f0;
    background: rgba(255, 255, 255, 0.05);
  }

  &.active {
    color: var(--primary-light);
    background: rgba(56, 189, 248, 0.12);
    border-color: rgba(56, 189, 248, 0.35);
    box-shadow: 0 0 12px rgba(56, 189, 248, 0.15);

    .nav-indicator {
      position: absolute;
      bottom: -1px;
      left: 20%;
      right: 20%;
      height: 2px;
      background: var(--primary-light);
      border-radius: 1px;
      box-shadow: 0 0 6px var(--primary-light);
    }
  }
}

.header-divider {
  width: 1px;
  height: 24px;
  background: rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  font-size: 12.5px;
  color: #e2e8f0;

  .user-avatar {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(56, 189, 248, 0.25), rgba(99, 102, 241, 0.25));
    border: 1px solid rgba(56, 189, 248, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary-light);
  }

  .user-name {
    font-weight: 500;
    max-width: 90px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: rgba(244, 63, 94, 0.1);
  border: 1px solid rgba(244, 63, 94, 0.3);
  color: #fda4af;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.2s ease;

  &:hover {
    background: var(--danger);
    border-color: var(--danger);
    color: #ffffff;
    box-shadow: 0 0 10px rgba(244, 63, 94, 0.4);
  }
}
</style>
