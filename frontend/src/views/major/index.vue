<template>
  <div class="analysis-layout">
    <!-- 第一行：重点省份学科热力分布 + 就业率TOP10 -->
    <div class="analysis-row">
      <DvBorderBox title="重点省份与主要学科门类开设热力分布" style="flex: 6; min-height: 270px;">
        <div ref="heatmapChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox title="就业率 TOP10 专业" style="flex: 4; min-height: 270px;">
        <div ref="jobChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>

    <!-- 第二行：新兴专业趋势 -->
    <div class="analysis-row">
      <DvBorderBox title="新兴前沿专业 (人工智能 / 大数据等) 新增开设院校趋势" style="flex: 1; min-height: 270px;">
        <div ref="trendChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getEmploymentTop10, getNewEmergingTrend } from '@/api/major'

const heatmapChartRef = ref<HTMLDivElement | null>(null)
const jobChartRef = ref<HTMLDivElement | null>(null)
const trendChartRef = ref<HTMLDivElement | null>(null)

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

// 1. 重点省份与主要学科门类开设热力分布
const provinces = ['北京', '江苏', '广东', '山东', '河南', '湖北', '浙江', '四川', '陕西', '辽宁']
const disciplines = ['工学', '理学', '管理学', '医学', '文学', '经济学', '艺术学']

const initHeatmapChart = (data: Array<[number, number, number]>) => {
  const chart = registerChart(heatmapChartRef.value)
  if (!chart) return

  chart.setOption({
    tooltip: {
      position: 'top',
      backgroundColor: 'rgba(5, 18, 43, 0.9)',
      borderColor: '#00e5ff',
      textStyle: { color: '#fff' },
      formatter: (params: any) => {
        const p = params.value
        return `${provinces[p[0]]} · ${disciplines[p[1]]}<br/>开设热度指数: <strong style="color:#00ffaa;">${p[2]}</strong>`
      }
    },
    grid: { top: 20, right: 70, bottom: 25, left: 55 },
    xAxis: {
      type: 'category',
      data: provinces,
      splitArea: { show: true },
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: disciplines,
      splitArea: { show: true },
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    visualMap: {
      min: 65,
      max: 100,
      calculable: true,
      orient: 'vertical',
      right: 12,
      top: 'middle',
      itemWidth: 10,
      itemHeight: 100,
      inRange: { color: ['#0b234d', '#1089ff', '#00e5ff'] },
      textStyle: { color: '#8ba2d4', fontSize: 10 }
    },
    series: [{
      name: '学科开设热度',
      type: 'heatmap',
      data,
      label: { show: true, color: '#fff', fontSize: 10 },
      itemStyle: {
        borderColor: '#05122b',
        borderWidth: 1
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 229, 255, 0.5)'
        }
      }
    }]
  })
}

// 2. 就业率 TOP10 横向柱状图
const initJobChart = (majors: string[], rates: number[]) => {
  const chart = registerChart(jobChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    grid: { top: 20, right: 40, bottom: 20, left: 75 },
    xAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: majors,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [{
      type: 'bar',
      data: rates,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#00ffaa' },
          { offset: 1, color: '#1089ff' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', formatter: '{c}%', color: '#fff', fontSize: 11 }
    }]
  })
}

// 3. 新兴专业新增走势
const initTrendChart = (years: string[], seriesData: Array<{ name: string; data: number[] }>) => {
  const chart = registerChart(trendChartRef.value)
  if (!chart) return

  const colors = ['#00e5ff', '#00ffaa', '#faad14']
  chart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#8ba2d4' }, top: 5 },
    xAxis: {
      type: 'category',
      data: years,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '开设院校数',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: seriesData.map((item, idx) => ({
      name: item.name,
      type: 'line',
      smooth: true,
      data: item.data,
      itemStyle: { color: colors[idx % colors.length] },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors[idx % colors.length] + '40' },
          { offset: 1, color: colors[idx % colors.length] + '05' }
        ])
      }
    }))
  })
}

const loadData = async () => {
  // 1. 重点省份与主要学科门类开设热力分布
  const heatmapData: Array<[number, number, number]> = []
  const baseScores = [
    [98, 99, 95, 92, 98, 97, 96], // 北京
    [99, 92, 94, 90, 91, 93, 88], // 江苏
    [95, 88, 93, 89, 87, 96, 90], // 广东
    [94, 85, 89, 88, 83, 84, 87], // 山东
    [86, 80, 86, 91, 81, 78, 82], // 河南
    [92, 89, 88, 90, 85, 87, 85], // 湖北
    [90, 86, 92, 84, 86, 92, 92], // 浙江
    [88, 82, 84, 89, 83, 82, 88], // 四川
    [91, 87, 82, 83, 85, 80, 81], // 陕西
    [87, 81, 79, 82, 80, 75, 76]  // 辽宁
  ]
  baseScores.forEach((row, pIdx) => {
    row.forEach((score, dIdx) => {
      heatmapData.push([pIdx, dIdx, score])
    })
  })
  initHeatmapChart(heatmapData)

  // 2. 就业率 TOP10
  let majors = ['自动化', '数字媒体', '通信工程', 'AI', '物联网', '计算机', '微电子', '网络工程', '软件工程', '信息安全']
  let rates = [89, 90, 91, 92, 92, 93, 94, 95, 96, 97]
  try {
    const res = await getEmploymentTop10()
    if (res && res.majors) {
      majors = res.majors
      rates = res.rates
    }
  } catch {}
  initJobChart(majors, rates)

  // 3. 新兴专业新增走势
  let years = ['2019', '2020', '2021', '2022', '2023', '2024']
  let series = [
    { name: '人工智能', data: [35, 180, 130, 95, 80, 60] },
    { name: '数据科学', data: [20, 50, 100, 150, 120, 90] }
  ]
  try {
    const res = await getNewEmergingTrend()
    if (res && res.years) {
      years = res.years
      series = res.series
    }
  } catch {}
  initTrendChart(years, series)
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
