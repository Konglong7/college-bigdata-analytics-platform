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
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-main {
  height: calc(100vh - var(--header-height));
  width: 100vw;
  padding: 12px;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;

  &.is-dashboard {
    padding: 0;
    overflow: hidden;
  }
}
</style>
