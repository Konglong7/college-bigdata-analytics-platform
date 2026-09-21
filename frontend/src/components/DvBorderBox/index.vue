<template>
  <div class="dv-border-box">
    <div class="card-glow-line"></div>
    <div v-if="title" class="dv-title">
      <div class="title-main">
        <span class="title-dot"></span>
        <span class="title-text">{{ title }}</span>
      </div>
      <div v-if="$slots.extra" class="title-extra">
        <slot name="extra"></slot>
      </div>
    </div>
    <div class="card-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>

<style scoped lang="scss">
.dv-border-box {
  position: relative;
  overflow: hidden;
}

.card-glow-line {
  position: absolute;
  top: 0;
  left: 10%;
  right: 10%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(56, 189, 248, 0.4), transparent);
  pointer-events: none;
}

.dv-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
  user-select: none;

  &::before {
    display: none; /* override main.scss default before pseudo */
  }

  .title-main {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .title-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--primary-light);
    box-shadow: 0 0 8px var(--primary-light);
    flex-shrink: 0;
  }

  .title-text {
    font-size: 13.5px;
    font-weight: 600;
    color: #e2e8f0;
    letter-spacing: 0.3px;
  }

  .title-extra {
    display: flex;
    align-items: center;
  }
}

.card-content {
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
</style>
