<template>
  <div ref="adapterWrapRef" class="screen-adapter-wrap" :class="{ 'is-flow': isFlow }">
    <div
      ref="adapterContentRef"
      class="screen-adapter-content"
      :style="contentStyle"
    >
      <slot :scale="scale" :is-scaled="scaleMode === 'scale' && !isFlow" :is-flow="isFlow" />
    </div>

    <!-- 底部视口缩放状态与自适应切换控件 (可视化大屏行业最佳实践与教学展示) -->
    <div v-if="!isFlow" class="adapter-control-panel">
      <div class="adapter-status" :title="'基准设计稿: ' + width + '×' + height + '，当前渲染缩放因子: ' + scale.toFixed(2)">
        <span class="status-indicator"></span>
        <span class="status-text">{{ (scale * 100).toFixed(0) }}% 等比适配</span>
      </div>
      <button 
        class="adapter-mode-btn" 
        :class="{ active: scaleMode === 'scale' }"
        :title="scaleMode === 'scale' ? '当前处于等比缩放模式（保证文字不换行、图表不挤压）' : '当前处于自由拉伸模式'"
        @click="toggleScaleMode"
      >
        <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 3 21 3 21 9"></polyline>
          <polyline points="9 21 3 21 3 15"></polyline>
          <line x1="21" y1="3" x2="14" y2="10"></line>
          <line x1="3" y1="21" x2="10" y2="14"></line>
        </svg>
        <span>{{ scaleMode === 'scale' ? '等比锁比' : '自由铺满' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

interface Props {
  /** 设计稿基准逻辑宽度 (默认 1920) */
  width?: number
  /** 设计稿基准逻辑高度 (默认 960，适配 1080p 扣除 Header 的视口) */
  height?: number
  /** 默认缩放模式: scale (等比例缩放保持宽高比) | stretch (自由铺满) */
  defaultMode?: 'scale' | 'stretch'
  /**
   * 窄屏流式模式阈值 (默认 900px)。
   * 视口宽度小于等于该值时，放弃「整体等比缩放」改为纵向自然流式排布，
   * 原因：1920 设计稿缩放到 390px 手机屏后缩放因子仅 0.2，
   * 正文从 12px 变成约 2.3px，信息完全不可读，还不如让页面纵向滚动。
   */
  flowBreakpoint?: number
}

const props = withDefaults(defineProps<Props>(), {
  width: 1920,
  height: 960,
  defaultMode: 'scale',
  flowBreakpoint: 900
})

const emit = defineEmits<{
  (e: 'resize', scale: number): void
  (e: 'flow-change', isFlow: boolean): void
}>()

const adapterWrapRef = ref<HTMLElement | null>(null)
const adapterContentRef = ref<HTMLElement | null>(null)
const scale = ref<number>(1)
const scaleMode = ref<'scale' | 'stretch'>(props.defaultMode)
const isFlow = ref<boolean>(false)

/**
 * 核心专业算法: 视口等比缩放因子计算 (Uniform Scale Factor Computation)
 * 公式: scale = min(clientWidth / baseWidth, clientHeight / baseHeight)
 */
const calcScale = () => {
  if (!adapterWrapRef.value) return 1
  const clientW = adapterWrapRef.value.clientWidth || window.innerWidth
  const clientH = adapterWrapRef.value.clientHeight || (window.innerHeight - 66)

  if (scaleMode.value === 'stretch' || isFlow.value) {
    return 1
  }

  const scaleX = clientW / props.width
  const scaleY = clientH / props.height
  // 取二者较小值，实现 Letterboxing/Pillarboxing (四周等比留白)，避免内容超出视口
  return Math.min(scaleX, scaleY)
}

/** 判定当前是否应进入窄屏流式模式 */
const resolveFlow = () => {
  const viewportW = adapterWrapRef.value?.clientWidth || window.innerWidth
  isFlow.value = viewportW <= props.flowBreakpoint
}

/**
 * 防抖函数 (Debounce)
 * 避免窗口拖拽 resize 过程中每秒触发几十次重排引起掉帧
 */
let timer: ReturnType<typeof setTimeout> | null = null
const debouncedResize = () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    updateScale()
  }, 100)
}

const updateScale = () => {
  const wasFlow = isFlow.value
  resolveFlow()
  scale.value = calcScale()
  emit('resize', scale.value)
  if (wasFlow !== isFlow.value) {
    emit('flow-change', isFlow.value)
  }
}

const toggleScaleMode = () => {
  scaleMode.value = scaleMode.value === 'scale' ? 'stretch' : 'scale'
  nextTick(() => {
    updateScale()
  })
}

/** 动态计算容器样式 (CSS Transform Matrix / 窄屏自然流式) */
const contentStyle = computed(() => {
  if (isFlow.value) {
    // 窄屏：还原为文档流，宽度铺满、高度自适应，由父级滚动
    return {
      width: '100%',
      height: 'auto',
      transform: 'none',
      position: 'relative' as const,
      left: 'auto',
      top: 'auto'
    }
  }

  if (scaleMode.value === 'stretch') {
    return {
      width: '100%',
      height: '100%',
      transform: 'none',
      position: 'relative' as const,
      left: 'auto',
      top: 'auto'
    }
  }

  return {
    width: `${props.width}px`,
    height: `${props.height}px`,
    position: 'absolute' as const,
    left: '50%',
    top: '50%',
    // transform: translate(-50%, -50%) + scale() 保证始终在父容器正中间居中呈现
    transform: `translate(-50%, -50%) scale(${scale.value})`,
    transformOrigin: 'center center',
    transition: 'transform 0.15s cubic-bezier(0.4, 0, 0.2, 1)'
  }
})

onMounted(() => {
  nextTick(() => {
    updateScale()
  })
  window.addEventListener('resize', debouncedResize)
  window.addEventListener('orientationchange', debouncedResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', debouncedResize)
  window.removeEventListener('orientationchange', debouncedResize)
  if (timer) clearTimeout(timer)
})

defineExpose({
  updateScale,
  scale,
  scaleMode,
  isFlow
})
</script>

<style scoped lang="scss">
.screen-adapter-wrap {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background-color: var(--bg-main, #070c18);
  display: flex;
  align-items: center;
  justify-content: center;

  /* 窄屏流式模式：解除居中与裁切，允许纵向滚动 */
  &.is-flow {
    display: block;
    overflow-x: hidden;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
}

.screen-adapter-content {
  flex-shrink: 0;
  box-sizing: border-box;
}

/* 底部右下角微质感悬浮控制面板 */
.adapter-control-panel {
  position: absolute;
  right: 14px;
  bottom: 12px;
  z-index: 999;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  background: rgba(13, 22, 41, 0.75);
  border: 1px solid rgba(56, 189, 248, 0.25);
  border-radius: 20px;
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
  user-select: none;
  transition: all 0.2s ease;
  opacity: 0.7;

  &:hover {
    opacity: 1;
    border-color: rgba(56, 189, 248, 0.5);
    box-shadow: 0 4px 20px rgba(56, 189, 248, 0.2);
  }
}

.adapter-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: #94a3b8;
  font-family: var(--font-mono, monospace);

  .status-indicator {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #10b981;
    box-shadow: 0 0 6px #10b981;
  }

  .status-text {
    letter-spacing: 0.3px;
  }
}

.adapter-mode-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 7px;
  border-radius: 12px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(56, 189, 248, 0.15);
    border-color: rgba(56, 189, 248, 0.4);
    color: #38bdf8;
  }

  &.active {
    background: rgba(56, 189, 248, 0.2);
    border-color: rgba(56, 189, 248, 0.45);
    color: #38bdf8;
  }
}
</style>
