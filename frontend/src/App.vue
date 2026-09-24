<template>
  <!-- 云端演示服务唤醒提示条：Render 免费实例冷启动 30~60 秒期间给用户确定性反馈 -->
  <transition name="wake-fade">
    <div v-if="serviceWaking" class="wake-banner">
      <span class="wake-spinner"></span>
      <span class="wake-text">{{ serviceWakingHint || '正在唤醒云端演示服务，请稍候…' }}</span>
    </div>
  </transition>

  <router-view />
</template>

<script setup lang="ts">
import { serviceWaking, serviceWakingHint } from '@/utils/request'
</script>

<style>
/* 根视图重置 */
#app {
  width: 100vw;
  height: 100vh;
  height: 100dvh; /* 移动端浏览器工具栏收起/展开时保持真实可视高度 */
  overflow: hidden;
}

.wake-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 9px 16px;
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.95), rgba(99, 102, 241, 0.95));
  color: #ffffff;
  font-size: 12.5px;
  font-weight: 500;
  letter-spacing: 0.3px;
  box-shadow: 0 2px 14px rgba(2, 132, 199, 0.4);

  .wake-spinner {
    width: 13px;
    height: 13px;
    flex-shrink: 0;
    border: 2px solid rgba(255, 255, 255, 0.35);
    border-top-color: #ffffff;
    border-radius: 50%;
    animation: wake-spin 0.8s linear infinite;
  }

  .wake-text {
    text-align: center;
  }
}

@keyframes wake-spin {
  to {
    transform: rotate(360deg);
  }
}

.wake-fade-enter-active,
.wake-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.wake-fade-enter-from,
.wake-fade-leave-to {
  opacity: 0;
  transform: translateY(-100%);
}
</style>
