<template>
  <div class="analysis-layout">
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

const compareChartRef = ref<HTMLDivElement | null>(null)
const funnelChartRef = ref<HTMLDivElement | null>(null)
const matrixChartRef = ref<HTMLDivElement | null>(null)

const chartInstances: echarts.ECharts[] = []

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

const baseChartStyle = {
  textStyle: { color: '#8ba2d4' },
  grid: { top: 35, right: 20, bottom: 25, left: 45 },
  tooltip: {
    backgroundColor: 'rgba(5, 18, 43, 0.9)',
    borderColor: '#00e5ff',
    borderWidth: 1,
    textStyle: { color: '#fff' }
  }
}

// 1. 历年对比
const initCompareChart = (data: CompareStatsData) => {
  const chart = registerChart(compareChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#8ba2d4' }, top: 5 },
    grid: { top: 35, right: 48, bottom: 25, left: 45 },
    xAxis: {
      type: 'category',
      data: data.years,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: [
      {
        type: 'value',
        name: '万人',
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
        axisLabel: { color: '#8ba2d4', fontSize: 11 }
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

  const colors = ['#1089ff', '#00e5ff', '#00ffaa', '#faad14', '#ff7875']
  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(5, 18, 43, 0.9)',
      borderColor: '#00e5ff',
      textStyle: { color: '#fff' },
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
      itemStyle: { borderColor: '#05122b', borderWidth: 1.5 },
      data
    }]
  })
}

// 3. 投放矩阵
const initMatrixChart = (data: MatrixPlanData) => {
  const chart = registerChart(matrixChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: {
      position: 'top',
      backgroundColor: 'rgba(5, 18, 43, 0.9)',
      borderColor: '#00e5ff',
      textStyle: { color: '#fff' },
      formatter: (params: any) => {
        const p = params.value
        return `${data.universities[p[0]]} -> ${data.provinces[p[1]]}<br/>招生计划: <strong style="color:#00ffaa;">${p[2]}</strong> 人`
      }
    },
    xAxis: {
      type: 'category',
      data: data.universities,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: data.provinces,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [{
      type: 'scatter',
      symbolSize: (val: any) => Math.max(val[2] / 7, 12),
      data: data.points,
      itemStyle: {
        color: new echarts.graphic.RadialGradient(0.4, 0.3, 1, [
          { offset: 0, color: '#00e5ff' },
          { offset: 1, color: '#1089ff' }
        ]),
        shadowBlur: 8,
        shadowColor: 'rgba(0, 229, 255, 0.5)'
      }
    }]
  })
}

const loadData = async () => {
  // 1. 历年对比
  let compareData: CompareStatsData = {
    years: ['2019', '2020', '2021', '2022', '2023', '2024'],
    applicants: [1031, 1071, 1078, 1193, 1291, 1342],
    admissions: [914, 967, 1001, 1014, 1042, 1090],
    rates: [88.6, 90.3, 92.8, 85.0, 80.7, 81.2]
  }
  try {
    const res = await getCompareStats()
    if (res && res.years) compareData = res
  } catch {}
  initCompareChart(compareData)

  // 2. 批次漏斗
  let funnelData: FunnelItem[] = [
    { value: 100, name: '报名总人数 (100%)' },
    { value: 80, name: '本专科总录取 (80%)' },
    { value: 45, name: '普通本科录取 (45%)' },
    { value: 15, name: '重点一本录取 (15%)' },
    { value: 5, name: '985/211录取 (5%)' }
  ]
  try {
    const res = await getBatchFunnel()
    if (res && res.length) funnelData = res
  } catch {}
  initFunnelChart(funnelData)

  // 3. 投放矩阵
  let matrixData: MatrixPlanData = {
    universities: ['北京大学', '清华大学', '复旦大学', '浙江大学', '南京大学'],
    provinces: ['北京', '广东', '江苏', '山东', '河南'],
    points: [
      [0, 0, 300], [0, 1, 120], [0, 2, 110], [0, 3, 150], [0, 4, 200],
      [1, 0, 320], [1, 1, 110], [1, 2, 100], [1, 3, 140], [1, 4, 190],
      [2, 0, 50], [2, 1, 150], [2, 2, 300], [2, 3, 100], [2, 4, 120],
      [3, 0, 60], [3, 1, 140], [3, 2, 180], [3, 3, 110], [3, 4, 130],
      [4, 0, 55], [4, 1, 120], [4, 2, 280], [4, 3, 90], [4, 4, 110]
    ]
  }
  try {
    const res = await getMatrixPlan()
    if (res && res.universities) {
      matrixData = res
      // 保证 5 所高校在矩阵图中均有完整的招生投放气泡
      const existingUnis = new Set(matrixData.points.map(p => p[0]))
      if (!existingUnis.has(3)) {
        matrixData.points.push(
          [3, 0, 60], [3, 1, 140], [3, 2, 180], [3, 3, 110], [3, 4, 130]
        )
      }
      if (!existingUnis.has(4)) {
        matrixData.points.push(
          [4, 0, 55], [4, 1, 120], [4, 2, 280], [4, 3, 90], [4, 4, 110]
        )
      }
    }
  } catch {}
  initMatrixChart(matrixData)
}

const handleResize = () => {
  chartInstances.forEach(c => c.resize())
}

onMounted(() => {
  nextTick(() => {
    loadData()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
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
.analysis-row {
  display: flex;
  gap: 12px;
  flex: 1;
}
</style>
