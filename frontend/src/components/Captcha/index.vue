<template>
  <div class="captcha-box" title="点击刷新验证码" @click="generateCode">
    <canvas ref="canvasRef" :width="width" :height="height" class="captcha-canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = withDefaults(
  defineProps<{
    width?: number
    height?: number
    charCount?: number
  }>(),
  {
    width: 120,
    height: 42,
    charCount: 4
  }
)

const emit = defineEmits<{
  (e: 'change', code: string): void
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const identifyCode = ref('')

// 排除易混淆的 0, O, 1, I, l
const CHAR_POOL = '23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz'
const COLOR_POOL = ['#00e5ff', '#1089ff', '#00ffaa', '#faad14', '#ff7875', '#d3adf7']

const randomNum = (min: number, max: number) => {
  return Math.floor(Math.random() * (max - min) + min)
}

const randomColor = () => {
  return COLOR_POOL[randomNum(0, COLOR_POOL.length)]
}

const drawText = (ctx: CanvasRenderingContext2D, char: string, i: number) => {
  ctx.fillStyle = randomColor()
  ctx.font = `${randomNum(20, 26)}px 'Segoe UI', Arial, sans-serif`
  ctx.textBaseline = 'middle'
  
  const x = (i + 0.5) * (props.width / props.charCount)
  const y = props.height / 2
  const deg = randomNum(-25, 25)

  ctx.translate(x, y)
  ctx.rotate((deg * Math.PI) / 180)
  ctx.fillText(char, -8, 0)
  ctx.rotate((-deg * Math.PI) / 180)
  ctx.translate(-x, -y)
}

const drawLine = (ctx: CanvasRenderingContext2D) => {
  ctx.strokeStyle = randomColor()
  ctx.lineWidth = randomNum(1, 2)
  ctx.beginPath()
  ctx.moveTo(randomNum(0, props.width), randomNum(0, props.height))
  ctx.bezierCurveTo(
    randomNum(0, props.width), randomNum(0, props.height),
    randomNum(0, props.width), randomNum(0, props.height),
    randomNum(0, props.width), randomNum(0, props.height)
  )
  ctx.stroke()
}

const drawDot = (ctx: CanvasRenderingContext2D) => {
  ctx.fillStyle = randomColor()
  ctx.beginPath()
  ctx.arc(randomNum(0, props.width), randomNum(0, props.height), 1, 0, 2 * Math.PI)
  ctx.fill()
}

const generateCode = () => {
  let code = ''
  for (let i = 0; i < props.charCount; i++) {
    code += CHAR_POOL[randomNum(0, CHAR_POOL.length)]
  }
  identifyCode.value = code
  emit('change', code)

  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 绘制半透明科技底色
  ctx.fillStyle = 'rgba(5, 18, 43, 0.85)'
  ctx.fillRect(0, 0, props.width, props.height)

  // 绘制干扰线
  for (let i = 0; i < 3; i++) {
    drawLine(ctx)
  }

  // 绘制字符
  for (let i = 0; i < code.length; i++) {
    drawText(ctx, code[i], i)
  }

  // 绘制噪点
  for (let i = 0; i < 25; i++) {
    drawDot(ctx)
  }
}

const verify = (input: string) => {
  if (!input) return false
  return input.trim().toLowerCase() === identifyCode.value.toLowerCase()
}

defineExpose({
  refresh: generateCode,
  verify,
  getCode: () => identifyCode.value
})

onMounted(() => {
  generateCode()
})
</script>

<style scoped lang="scss">
.captcha-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  background: rgba(0, 0, 0, 0.4);
  transition: all 0.3s;
  user-select: none;
  flex-shrink: 0;

  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
    transform: scale(1.02);
  }
}

.captcha-canvas {
  display: block;
}
</style>
