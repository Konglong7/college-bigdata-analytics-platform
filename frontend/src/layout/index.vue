<template>
  <div class="app-wrapper">
    <Header />
    <main :class="['app-main', { 'is-dashboard': route.path === '/dashboard' }]">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import Header from './Header.vue'

const route = useRoute()
</script>

<style scoped lang="scss">
.app-wrapper {
  width: 100vw;
  height: 100vh;
  height: 100dvh; /* 移动端浏览器工具栏收起/展开时保持真实可视高度，避免底部被切 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-main {
  height: calc(100vh - var(--header-height));
  height: calc(100dvh - var(--header-height));
  position: relative;
  width: 100vw;
  padding: 12px;
  overflow-y: auto;
  overflow-x: hidden;

  &.is-dashboard {
    padding: 0;
    overflow: hidden;
  }
}

/* 窄屏：左右内边距收紧，把有限的横向空间让给图表与卡片 */
@media (max-width: 900px) {
  .app-main {
    padding: 8px;

    &.is-dashboard {
      padding: 0;
    }
  }
}
</style>
