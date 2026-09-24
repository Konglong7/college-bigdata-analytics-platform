<template>
  <div class="analysis-layout">
    <div v-if="enrollmentLoad.state.value === 'error'" class="data-status error-status">
      {{ enrollmentLoad.errorMessage.value }}
      <button class="dv-btn" @click="loadData">重试</button>
    </div>
    <div v-else-if="enrollmentLoad.state.value === 'empty'" class="data-status">
      当前没有可用的招生分析数据。
    </div>
    <!-- 第一行：报名与录取对比 + 批次漏斗 -->
    <div class="analysis-row">
      <DvBorderBox title="历年高考报名人数与录取人数对比 (万人)" style="flex: 1; min-height: 270px;">
        <div ref="compareChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox title="全国高校录取批次层级漏斗分析" style="flex: 1; min-height: 270px;">
        <div ref="funnelChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>

    <!-- 第二行：各省份招生计划投放矩阵图 -->
    <div class="analysis-row">
      <DvBorderBox title="各省份顶尖高校招生计划投放矩阵图" style="flex: 1; min-height: 270px;">
        <div ref="matrixChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getCompareStats, getBatchFunnel, getMatrixPlan, CompareStatsData, FunnelItem, MatrixPlanData } from '@/api/enrollment'
import { getChartTheme, onThemeChange } from '@/utils/theme'
import { createLoadState } from '@/utils/loadState'

const compareChartRef = ref<HTMLDivElement | null>(null)
const funnelChartRef = ref<HTMLDivElement | null>(null)
const matrixChartRef = ref<HTMLDivElement | null>(null)

const chartInstances: echarts.ECharts[] = []
const enrollmentLoad = createLoadState()

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

// 缓存数据用于主题切换时重绘
let cachedCompareData: CompareStatsData | null = null
let cachedFunnelData: FunnelItem[] | null = null
let cachedMatrixData: MatrixPlanData | null = null

// 1. 历年对比
const initCompareChart = (data: CompareStatsData) => {
  const chart = registerChart(compareChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    legend: { textStyle: { color: ct.textColor }, top: 5 },
    grid: { top: 35, right: 48, bottom: 25, left: 45 },
    xAxis: {
      type: 'category',
      data: data.years,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: [
      {
        type: 'value',
        name: '万人',
        splitLine: { lineStyle: { color: ct.splitLineColor } },
        axisLabel: { color: ct.textColor, fontSize: 11 }
      },
      {
        type: 'value',
        name: '录取率',
        min: 60,
        max: 100,
        splitLine: { show: false },
        axisLabel: {
          color: '#faad14',
          fontSize: 11,
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '报名人数',
        type: 'bar',
        data: data.applicants,
        itemStyle: { color: '#1089ff', borderRadius: [3, 3, 0, 0] }
      },
      {
        name: '录取人数',
        type: 'bar',
        data: data.admissions,
        itemStyle: { color: '#00e5ff', borderRadius: [3, 3, 0, 0] }
      },
      {
        name: '录取率 (%)',
        type: 'line',
        yAxisIndex: 1,
        data: data.rates,
        itemStyle: { color: '#faad14' },
        symbolSize: 6
      }
    ]
  })
}

// 2. 批次漏斗
const initFunnelChart = (data: FunnelItem[]) => {
  const chart = registerChart(funnelChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  const colors = ['#1089ff', '#00e5ff', '#00ffaa', '#faad14', '#ff7875']
  chart.setOption({
    tooltip: {
      ...ct.tooltip,
      trigger: 'item',
      formatter: '{b}: {c}%'
    },
    color: colors,
    series: [{
      name: '批次漏斗',
      type: 'funnel',
      left: '10%',
      top: 30,
      bottom: 20,
      width: '80%',
      min: 0,
      max: 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 3,
      label: { show: true, position: 'inside', color: '#fff', fontSize: 11, formatter: '{b}' },
      itemStyle: { borderColor: ct.isDark ? '#05122b' : '#ffffff', borderWidth: 1.5 },
      data
    }]
  }, true)
}

// 3. 投放矩阵 (覆盖全国 31 省份全量投放)
const initMatrixChart = (data: MatrixPlanData) => {
  const chart = registerChart(matrixChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  // 坐标映射：X轴为全国 31 个省市自治区，Y轴为代表性顶尖高校
  // 后端返回的 points 格式为 [uIdx, pIdx, plan]，因此映射为 [pIdx, uIdx, plan]
  const scatterPoints = data.points.map(p => [p[1], p[0], p[2]])

  chart.setOption({
    tooltip: {
      ...ct.tooltip,
      position: 'top',
      formatter: (params: any) => {
        const p = params.value
        const provName = data.provinces[p[0]] || ''
        const univName = data.universities[p[1]] || ''
        const plan = p[2]
        return `<div style="font-weight:bold;margin-bottom:3px;color:${ct.isDark ? '#00e5ff' : '#0284c7'}">${univName}</div>
                <div style="font-size:12px;">生源省份: <strong style="color:${ct.textColor}">${provName}</strong></div>
                <div style="font-size:12px;margin-top:2px;">招生计划投放: <strong style="color:#10b981;font-size:13px;">${plan}</strong> 人</div>`
      }
    },
    grid: { top: 25, right: 25, bottom: 42, left: 95 },
    textStyle: { color: ct.textColor },
    xAxis: {
      type: 'category',
      data: data.provinces,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: {
        color: ct.textColor,
        fontSize: 10.5,
        interval: 0,
        rotate: 32
      },
      splitLine: { show: true, lineStyle: { color: ct.splitLineColor } }
    },
    yAxis: {
      type: 'category',
      data: data.universities,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 },
      splitLine: { show: true, lineStyle: { color: ct.splitLineColor } }
    },
    dataZoom: [
      {
        type: 'inside',
        xAxisIndex: 0,
        zoomOnMouseWheel: true
      },
      {
        type: 'slider',
        xAxisIndex: 0,
        height: 12,
        bottom: 2,
        borderColor: 'transparent',
        backgroundColor: ct.isDark ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.04)',
        fillerColor: ct.isDark ? 'rgba(0, 229, 255, 0.2)' : 'rgba(2, 132, 199, 0.2)',
        handleStyle: { color: ct.isDark ? '#00e5ff' : '#0284c7' },
        textStyle: { color: ct.textColor, fontSize: 10 }
      }
    ],
    series: [{
      type: 'scatter',
      symbolSize: (val: any) => {
        const plan = val[2]
        return Math.min(26, Math.max(7, Math.round(Math.sqrt(plan) * 1.8)))
      },
      data: scatterPoints,
      itemStyle: {
        color: new echarts.graphic.RadialGradient(0.4, 0.3, 1, [
          { offset: 0, color: ct.isDark ? '#00e5ff' : '#38bdf8' },
          { offset: 1, color: ct.isDark ? '#1089ff' : '#0284c7' }
        ]),
        shadowBlur: ct.isDark ? 8 : 4,
        shadowColor: ct.isDark ? 'rgba(0, 229, 255, 0.4)' : 'rgba(2, 132, 199, 0.25)'
      }
    }]
  }, true)
}

const renderAllCharts = () => {
  if (cachedCompareData) initCompareChart(cachedCompareData)
  if (cachedFunnelData) initFunnelChart(cachedFunnelData)
  if (cachedMatrixData) initMatrixChart(cachedMatrixData)
}

const loadData = async () => {
  enrollmentLoad.start()
  try {
    const [compareData, funnelData, matrixData] = await Promise.all([
      getCompareStats(),
      getBatchFunnel(),
      getMatrixPlan()
    ])
    if (!compareData?.years?.length || !funnelData?.length || !matrixData?.universities?.length || !matrixData.provinces?.length) {
      enrollmentLoad.succeed(true)
      return
    }
    cachedCompareData = compareData
    cachedFunnelData = funnelData
    cachedMatrixData = matrixData
    initCompareChart(compareData)
    initFunnelChart(funnelData)
    initMatrixChart(matrixData)
    enrollmentLoad.succeed(false)
  } catch (error) {
    console.error('Failed to load enrollment analysis data:', error)
    cachedCompareData = null
    cachedFunnelData = null
    cachedMatrixData = null
    enrollmentLoad.fail('招生分析数据加载失败，请确认后端服务可用。')
  }
}

const handleResize = () => {
  chartInstances.forEach(c => c.resize())
}

let unsubTheme: (() => void) | null = null

onMounted(() => {
  nextTick(() => {
    loadData()
  })
  window.addEventListener('resize', handleResize)
  unsubTheme = onThemeChange(() => {
    renderAllCharts()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (unsubTheme) {
    unsubTheme()
  }
  chartInstances.forEach(c => c.dispose())
  chartInstances.length = 0
})
</script>

<style scoped lang="scss">
.analysis-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-status {
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-sub);
  background: rgba(15, 23, 42, 0.45);
}

.error-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--danger, #ff7875);
}
.analysis-row {
  display: flex;
  gap: 12px;
  flex: 1;
}
/* =========================================================
   移动端适配 (≤768px)：招生分析图表行由并排改为单列
========================================================= */
@media (max-width: 768px) {
  .analysis-layout {
    height: auto;
    min-height: 100%;
  }

  /* 两个图表并排会各被压到 ~165px，移动端统一纵向排列 */
  .analysis-row {
    flex: none;
    flex-direction: column;
    gap: 10px;
  }

  /* 取消卡片写死的 270px 最小高度，改由图表高度撑开 */
  .analysis-row > .dv-border-box {
    flex: none !important;
    width: 100%;
    min-height: auto !important;
    padding: 12px;
  }

  /* ECharts 容器保留真实高度，否则自动高度卡片内会塌陷不可见 */
  .chart-container {
    flex: none;
    height: 240px;
    min-height: 240px;
  }

  /* 卡片标题较长时允许换行 */
  :deep(.dv-title) {
    flex-wrap: wrap;
    gap: 6px;

    .title-text {
      font-size: 12.5px;
      line-height: 1.35;
    }
  }

  .data-status {
    padding: 8px 12px;
    font-size: 12px;
  }

  .error-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
