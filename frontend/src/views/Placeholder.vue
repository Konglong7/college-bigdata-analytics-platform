<template>
  <div style="padding: 20px; color: #00e5ff;">
    <h2>{{ title }}</h2>
    <p style="color: #8ba2d4; margin-top: 10px;">模块待迁移，正在逐步根据 HTML 原型迁移构建中...</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>
