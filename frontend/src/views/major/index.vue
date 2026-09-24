<template>
  <div class="analysis-layout">
    <div v-if="majorLoad.state.value === 'error'" class="data-status error-status">
      {{ majorLoad.errorMessage.value }}
      <button class="dv-btn" @click="loadData">重试</button>
    </div>
    <div v-else-if="majorLoad.state.value === 'empty'" class="data-status">
      当前没有可用的专业分析数据。
    </div>
    <!-- 第一行：热门专业词云墙 (默认) / 学科门类分布切换 + 就业率TOP10 -->
    <div class="analysis-row">
      <DvBorderBox
        :title="viewMode === 'wordcloud' ? '全国高校热门开设专业与前沿学科词云墙 (真实开设高校数加权)' : '教育部 12 大学科门类开设数量与结构分布 (真实数仓事实聚合)'"
        style="flex: 6; min-height: 280px;"
      >
        <template #right>
          <div class="mode-switch-group">
            <button
              :class="['mode-btn', { active: viewMode === 'wordcloud' }]"
              @click="switchViewMode('wordcloud')"
            >
              🔥 热门专业词云
            </button>
            <button
              :class="['mode-btn', { active: viewMode === 'category' }]"
              @click="switchViewMode('category')"
            >
              📊 学科门类占比
            </button>
          </div>
        </template>
        <div ref="leftChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox title="全国高校毕业生平均就业率 TOP10 专业" style="flex: 4; min-height: 280px;">
        <div ref="jobChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>

    <!-- 第二行：新兴前沿专业新增开设走势 -->
    <div class="analysis-row">
      <DvBorderBox title="国家战略新兴前沿专业 (人工智能 / 大数据 / 软件工程) 历年增量布点走势" style="flex: 1; min-height: 280px;">
        <div ref="trendChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import {
  getEmploymentTop10,
  getNewEmergingTrend,
  getCategoryDistribution,
  getHotWordCloud,
  CategoryDistributionItem,
  HotWordCloudItem
} from '@/api/major'
import { getChartTheme, onThemeChange } from '@/utils/theme'
import { createLoadState } from '@/utils/loadState'

const viewMode = ref<'wordcloud' | 'category'>('wordcloud')
const majorLoad = createLoadState()

const leftChartRef = ref<HTMLDivElement | null>(null)
const jobChartRef = ref<HTMLDivElement | null>(null)
const trendChartRef = ref<HTMLDivElement | null>(null)

let leftChart: echarts.ECharts | null = null
let jobChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

// 缓存数据用于主题切换重绘
let cachedWordCloudData: HotWordCloudItem[] = []
let cachedCategoryData: CategoryDistributionItem[] = []
let cachedJobMajors: string[] = []
let cachedJobRates: number[] = []
let cachedTrendYears: string[] = []
let cachedTrendSeries: Array<{ name: string; data: number[] }> = []

let unregisterTheme: (() => void) | null = null

// 模式切换
const switchViewMode = (mode: 'wordcloud' | 'category') => {
  viewMode.value = mode
  nextTick(() => {
    if (mode === 'wordcloud' && cachedWordCloudData.length) {
      renderWordCloudChart(cachedWordCloudData)
    } else if (mode === 'category' && cachedCategoryData.length) {
      renderCategoryChart(cachedCategoryData)
    }
  })
}

// 1. 热门专业词云墙 (WordCloud)
const renderWordCloudChart = (data: HotWordCloudItem[]) => {
  if (!leftChartRef.value) return
  if (!leftChart) {
    leftChart = echarts.init(leftChartRef.value)
  }
  const theme = getChartTheme()

  // 针对不同学科门类定制现代学术科技配色
  const categoryColorMap: Record<string, string[]> = {
    '工学': ['#38bdf8', '#0284c7', '#0ea5e9', '#3b82f6', '#2563eb'],
    '理学': ['#818cf8', '#6366f1', '#4f46e5', '#a855f7'],
    '医学': ['#34d399', '#10b981', '#059669', '#14b8a6'],
    '经济学': ['#fbbf24', '#f59e0b', '#d97706'],
    '管理学': ['#f43f5e', '#e11d48', '#be123c'],
    '文学': ['#c084fc', '#a855f7', '#9333ea'],
    '法学': ['#22d3ee', '#06b6d4', '#0891b2'],
    '教育学': ['#fb7185', '#f43f5e', '#e11d48']
  }
  const defaultColors = ['#38bdf8', '#818cf8', '#34d399', '#fbbf24', '#f43f5e', '#c084fc', '#22d3ee']

  leftChart.setOption({
    tooltip: {
      show: true,
      formatter: (params: any) => {
        const item = params.data
        return `<div style="font-weight:bold;font-size:13px;color:#38bdf8;margin-bottom:4px;">${item.name}</div>
                <div style="font-size:12px;margin-bottom:2px;">所属门类: <strong style="color:#fbbf24;">${item.category || '综合学科'}</strong></div>
                <div style="font-size:12px;">全国开办高校: <strong style="color:#34d399;font-size:13px;">${item.value}</strong> 所</div>`
      },
      ...theme.tooltip
    },
    series: [
      {
        type: 'wordCloud',
        shape: 'circle',
        keepAspect: false,
        left: 'center',
        top: 'center',
        width: '94%',
        height: '90%',
        right: null,
        bottom: null,
        sizeRange: [13, 38],
        rotationRange: [-45, 45],
        rotationStep: 45,
        gridSize: 8,
        drawOutOfBound: false,
        layoutAnimation: true,
        textStyle: {
          fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
          fontWeight: 'bold',
          color: (itemParams: any) => {
            const cat = itemParams.data?.category || ''
            const colors = categoryColorMap[cat] || defaultColors
            return colors[Math.floor(Math.random() * colors.length)]
          }
        },
        emphasis: {
          focus: 'self',
          textStyle: {
            textShadowBlur: 14,
            textShadowColor: 'rgba(56, 189, 248, 0.75)'
          }
        },
        data: data.map(item => ({
          name: item.name,
          value: item.value,
          category: item.category
        }))
      }
    ]
  }, true)
}

// 1.2 学科门类分布 (Pie)
const renderCategoryChart = (data: CategoryDistributionItem[]) => {
  if (!leftChartRef.value) return
  if (!leftChart) {
    leftChart = echarts.init(leftChartRef.value)
  }
  const theme = getChartTheme()
  const colors = ['#0ea5e9', '#3b82f6', '#6366f1', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6', '#f43f5e', '#06b6d4']

  leftChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}门类<br/>开设专业统计: <strong>{c} 个</strong> ({d}%)',
      ...theme.tooltip
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 15,
      top: 'middle',
      textStyle: { color: theme.textColor, fontSize: 12 },
      pageTextStyle: { color: theme.textColor }
    },
    series: [
      {
        name: '学科门类',
        type: 'pie',
        radius: ['38%', '72%'],
        center: ['42%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: theme.isDark ? '#0d1629' : '#ffffff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%',
          color: theme.textColor,
          fontSize: 11
        },
        labelLine: {
          lineStyle: { color: theme.axisLineColor }
        },
        data: data.map((item, idx) => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: colors[idx % colors.length] }
        }))
      }
    ]
  }, true)
}

// 2. 就业率 TOP10 横向柱状图
const renderJobChart = (majors: string[], rates: number[]) => {
  if (!jobChartRef.value) return
  if (!jobChart) {
    jobChart = echarts.init(jobChartRef.value)
  }
  const theme = getChartTheme()

  jobChart.setOption({
    grid: { top: 20, right: 55, bottom: 20, left: 110, containLabel: false },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      ...theme.tooltip
    },
    xAxis: {
      type: 'value',
      max: 100,
      splitLine: { lineStyle: { color: theme.splitLineColor } },
      axisLabel: { color: theme.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: majors,
      inverse: true,
      axisLine: { lineStyle: { color: theme.axisLineColor } },
      axisLabel: { color: theme.textColor, fontSize: 11 }
    },
    series: [{
      type: 'bar',
      data: rates,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#38bdf8' },
          { offset: 1, color: '#10b981' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{c}%',
        color: theme.textColor,
        fontSize: 11
      }
    }]
  }, true)
}

// 3. 新兴专业新增走势
const renderTrendChart = (years: string[], seriesData: Array<{ name: string; data: number[] }>) => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const theme = getChartTheme()
  const colors = ['#0ea5e9', '#10b981', '#f59e0b']

  trendChart.setOption({
    grid: { top: 35, right: 30, bottom: 25, left: 45 },
    tooltip: { trigger: 'axis', ...theme.tooltip },
    legend: {
      textStyle: { color: theme.textColor },
      top: 5
    },
    xAxis: {
      type: 'category',
      data: years,
      axisLine: { lineStyle: { color: theme.axisLineColor } },
      axisLabel: { color: theme.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '开设院校数 (所)',
      nameTextStyle: { color: theme.textColor },
      splitLine: { lineStyle: { color: theme.splitLineColor } },
      axisLabel: { color: theme.textColor, fontSize: 11 }
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
  }, true)
}

const handleResize = () => {
  leftChart?.resize()
  jobChart?.resize()
  trendChart?.resize()
}

const rerenderAllCharts = () => {
  if (viewMode.value === 'wordcloud' && cachedWordCloudData.length) {
    renderWordCloudChart(cachedWordCloudData)
  } else if (viewMode.value === 'category' && cachedCategoryData.length) {
    renderCategoryChart(cachedCategoryData)
  }
  if (cachedJobMajors.length) renderJobChart(cachedJobMajors, cachedJobRates)
  if (cachedTrendYears.length) renderTrendChart(cachedTrendYears, cachedTrendSeries)
}

const loadData = async () => {
  majorLoad.start()
  try {
    const [wordRes, catRes, jobRes, trendRes] = await Promise.all([
      getHotWordCloud(),
      getCategoryDistribution(),
      getEmploymentTop10(),
      getNewEmergingTrend()
    ])

    if (!wordRes?.length || !catRes?.length || !jobRes?.majors?.length || !trendRes?.years?.length) {
      majorLoad.succeed(true)
      return
    }

    cachedWordCloudData = wordRes
    cachedCategoryData = catRes
    cachedJobMajors = jobRes.majors
    cachedJobRates = jobRes.rates
    cachedTrendYears = trendRes.years
    cachedTrendSeries = trendRes.series

    if (viewMode.value === 'wordcloud') renderWordCloudChart(cachedWordCloudData)
    if (viewMode.value === 'category') renderCategoryChart(cachedCategoryData)
    renderJobChart(cachedJobMajors, cachedJobRates)
    renderTrendChart(cachedTrendYears, cachedTrendSeries)
    majorLoad.succeed(false)
  } catch (error) {
    console.error('Failed to load major analysis data:', error)
    cachedWordCloudData = []
    cachedCategoryData = []
    cachedJobMajors = []
    cachedJobRates = []
    cachedTrendYears = []
    cachedTrendSeries = []
    majorLoad.fail('专业分析数据加载失败，请确认后端服务可用。')
  }
}

onMounted(() => {
  nextTick(() => {
    loadData()
    window.addEventListener('resize', handleResize)
    unregisterTheme = onThemeChange(() => {
      nextTick(() => {
        rerenderAllCharts()
      })
    })
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (unregisterTheme) unregisterTheme()
  leftChart?.dispose()
  jobChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped lang="scss">
.analysis-layout {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  width: 100%;
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

.mode-switch-group {
  display: flex;
  gap: 6px;
  align-items: center;
}

.mode-btn {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(56, 189, 248, 0.25);
  color: #94a3b8;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
  outline: none;

  &:hover {
    color: #38bdf8;
    border-color: rgba(56, 189, 248, 0.6);
    background: rgba(56, 189, 248, 0.1);
  }

  &.active {
    color: #0f172a;
    background: #38bdf8;
    border-color: #38bdf8;
    font-weight: 600;
    box-shadow: 0 0 10px rgba(56, 189, 248, 0.4);
  }
}

html.light {
  .mode-btn {
    background: #f1f5f9 !important;
    border-color: #cbd5e1 !important;
    color: #475569 !important;
    &:hover {
      color: #0284c7 !important;
      border-color: #7dd3fc !important;
      background: #e0f2fe !important;
    }
    &.active {
      color: #ffffff !important;
      background: #0284c7 !important;
      border-color: #0284c7 !important;
      box-shadow: 0 0 10px rgba(2, 132, 199, 0.3) !important;
    }
  }
}
</style>
