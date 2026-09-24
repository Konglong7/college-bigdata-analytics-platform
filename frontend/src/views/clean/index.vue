<template>
  <div class="clean-page">
    <div v-if="cleanLoad.state.value === 'error'" class="data-status error-status">
      {{ cleanLoad.errorMessage.value }}
      <button class="dv-btn" @click="loadData">重试</button>
    </div>
    <div v-else-if="cleanLoad.state.value === 'empty'" class="data-status">
      当前没有可用的数据清洗质量记录。
    </div>
    <!-- 顶部ETL清洗指标 -->
    <div class="stat-banner">
      <DvBorderBox class="stat-card">
        <div class="stat-icon">🗄️</div>
        <div class="stat-info">
          <div class="stat-label">原始数据量 (Raw Data)</div>
          <div class="stat-value">{{ summary.rawCount.toLocaleString() }}<span>条</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">✂️</div>
        <div class="stat-info">
          <div class="stat-label">重复数据过滤</div>
          <div class="stat-value" style="color: var(--warning);">
            {{ summary.duplicateCount.toLocaleString() }}<span>条</span>
          </div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">⚠️</div>
        <div class="stat-info">
          <div class="stat-label">异常与脏数据修复</div>
          <div class="stat-value" style="color: var(--danger);">
            {{ summary.errorCount.toLocaleString() }}<span>条</span>
          </div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">✨</div>
        <div class="stat-info">
          <div class="stat-label">有效清洗入库量</div>
          <div class="stat-value" style="color: var(--success);">
            {{ summary.cleanCount.toLocaleString() }}<span>条</span>
          </div>
        </div>
      </DvBorderBox>
    </div>

    <!-- 中间：ETL流程管道图展示 -->
    <DvBorderBox
      title="ETL 数据清洗与质量校验管道 (基于 Python Pandas / PySpark 流水线)"
      style="height: 120px; flex-shrink: 0;"
    >
      <div class="etl-flow-wrap">
        <div class="etl-node active-node">
          <div class="etl-node-header">Raw Data</div>
          <div class="etl-node-sub">湖仓原始日志</div>
        </div>
        <div class="etl-arrow">➔</div>
        <div class="etl-node">
          <div class="etl-node-header">数据去重</div>
          <div class="etl-node-sub">Drop Duplicates</div>
        </div>
        <div class="etl-arrow">➔</div>
        <div class="etl-node">
          <div class="etl-node-header">缺失值处理</div>
          <div class="etl-node-sub">Imputation / Fillna</div>
        </div>
        <div class="etl-arrow">➔</div>
        <div class="etl-node">
          <div class="etl-node-header">格式标准化</div>
          <div class="etl-node-sub">Standardization</div>
        </div>
        <div class="etl-arrow">➔</div>
        <div class="etl-node">
          <div class="etl-node-header">质量检测</div>
          <div class="etl-node-sub">Schema Validation</div>
        </div>
        <div class="etl-arrow">➔</div>
        <div class="etl-node active-node">
          <div class="etl-node-header">结构化入库</div>
          <div class="etl-node-sub">DWD 明细事实层</div>
        </div>
      </div>
    </DvBorderBox>

    <!-- 底部：数据质量分析图表 (3个图表) -->
    <div class="clean-bottom">
      <DvBorderBox title="清洗前后各模块数据量对比 (万条)" style="flex: 1; min-height: 270px;">
        <div ref="compareChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox title="数据清洗质量指标比例" style="flex: 1; min-height: 270px;">
        <div ref="qualityChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox title="近7日每日 ETL 清洗处理趋势 (万条)" style="flex: 1; min-height: 270px;">
        <div ref="dailyChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getCleanSummary, getCleanCharts, CleanSummaryData } from '@/api/clean'
import { getChartTheme, onThemeChange } from '@/utils/theme'
import { createLoadState } from '@/utils/loadState'

const compareChartRef = ref<HTMLDivElement | null>(null)
const qualityChartRef = ref<HTMLDivElement | null>(null)
const dailyChartRef = ref<HTMLDivElement | null>(null)

const chartInstances: echarts.ECharts[] = []
let unsubTheme: (() => void) | null = null

// 缓存图表数据以支持动态主题重绘
let cachedCategories: string[] = []
let cachedRaw: number[] = []
let cachedClean: number[] = []
let cachedQuality: Array<{ value: number; name: string }> = []
let cachedDates: string[] = []
let cachedThroughput: number[] = []
const cleanLoad = createLoadState()

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

const summary = ref<CleanSummaryData>({
  rawCount: 0,
  duplicateCount: 0,
  errorCount: 0,
  cleanCount: 0
})

// 1. 清洗前后对比
const initCompareChart = (categories: string[], raw: number[], clean: number[]) => {
  const chart = registerChart(compareChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    grid: { top: 35, right: 20, bottom: 38, left: 45 },
    legend: {
      data: ['原始数据', '清洗后入库'],
      textStyle: { color: ct.textColor },
      top: 5
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      splitLine: { lineStyle: { color: ct.splitLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    series: [
      {
        name: '原始数据',
        type: 'bar',
        data: raw,
        itemStyle: {
          color: ct.isDark ? 'rgba(14, 165, 233, 0.45)' : 'rgba(2, 132, 199, 0.35)',
          borderRadius: [3, 3, 0, 0]
        }
      },
      {
        name: '清洗后入库',
        type: 'bar',
        data: clean,
        itemStyle: {
          color: ct.isDark ? '#00e5ff' : '#0284c7',
          borderRadius: [3, 3, 0, 0]
        }
      }
    ]
  }, true)
}

// 2. 质量占比饼图
const initQualityChart = (data: Array<{ value: number; name: string }>) => {
  const chart = registerChart(qualityChartRef.value)
  if (!chart) return
  const ct = getChartTheme()
  const colors = ['#10b981', '#f59e0b', '#ef4444']

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'item' },
    legend: {
      bottom: '2%',
      textStyle: { color: ct.textColor },
      itemWidth: 10,
      itemHeight: 10
    },
    color: colors,
    series: [{
      name: '数据质量',
      type: 'pie',
      radius: ['38%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: ct.isDark ? '#0d1629' : '#ffffff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        color: ct.textColor,
        fontSize: 11
      },
      data
    }]
  }, true)
}

// 3. 7日吞吐趋势折线图
const initDailyChart = (dates: string[], throughput: number[]) => {
  const chart = registerChart(dailyChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    grid: { top: 35, right: 20, bottom: 25, left: 45 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      splitLine: { lineStyle: { color: ct.splitLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    series: [{
      name: '清洗吞吐量',
      type: 'line',
      data: throughput,
      smooth: true,
      itemStyle: { color: ct.isDark ? '#00ffaa' : '#10b981' },
      symbolSize: 6,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: ct.isDark ? 'rgba(0, 255, 170, 0.45)' : 'rgba(16, 185, 129, 0.35)' },
          { offset: 1, color: ct.isDark ? 'rgba(0, 255, 170, 0.02)' : 'rgba(16, 185, 129, 0.02)' }
        ])
      }
    }]
  }, true)
}

const renderAllCharts = () => {
  initCompareChart(cachedCategories, cachedRaw, cachedClean)
  initQualityChart(cachedQuality)
  initDailyChart(cachedDates, cachedThroughput)
}

const loadData = async () => {
  cleanLoad.start()
  try {
    const resSummary = await getCleanSummary()
    if (!resSummary) {
      cleanLoad.succeed(true)
      return
    }
    summary.value = resSummary
  } catch (error) {
    console.error('Failed to load clean summary:', error)
    cleanLoad.fail('数据清洗摘要加载失败，请确认后端服务可用。')
    return
  }

  try {
    const resCharts = await getCleanCharts()
    if (!resCharts?.compare || !resCharts.quality || !resCharts.daily) {
      cleanLoad.succeed(true)
      return
    }
    cachedCategories = resCharts.compare.categories
    cachedRaw = resCharts.compare.raw
    cachedClean = resCharts.compare.clean
    cachedQuality = resCharts.quality
    cachedDates = resCharts.daily.dates
    cachedThroughput = resCharts.daily.throughput
  } catch (error) {
    console.error('Failed to load clean charts:', error)
    cleanLoad.fail('数据清洗图表加载失败，请稍后重试。')
    return
  }

  renderAllCharts()
  cleanLoad.succeed(false)
}

const handleResize = () => {
  chartInstances.forEach(c => c.resize())
}

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
  if (unsubTheme) unsubTheme()
  chartInstances.forEach(c => c.dispose())
  chartInstances.length = 0
})
</script>

<style scoped lang="scss">
.clean-page {
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

.stat-banner {
  display: flex;
  gap: 12px;
  height: 80px;
  flex-shrink: 0;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 14px;
}

.stat-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  background: rgba(14, 165, 233, 0.12);
  border: 1px solid var(--secondary-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  color: var(--text-sub);
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: var(--primary-color);
  margin-top: 2px;
  span {
    font-size: 11px;
    font-weight: normal;
    color: var(--text-sub);
    margin-left: 4px;
  }
}

.etl-flow-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 16px;
  overflow-x: auto;
  gap: 10px;
}

.etl-node {
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 8px 14px;
  text-align: center;
  position: relative;
  min-width: 115px;
  flex-shrink: 0;
  box-shadow: var(--card-shadow);
  transition: all 0.3s ease;

  &.active-node {
    border-color: var(--primary-color);
    box-shadow: var(--card-shadow-hover);
  }
}

.etl-node-header {
  font-size: 13px;
  font-weight: bold;
  color: var(--primary-color);
}

.etl-node-sub {
  font-size: 11px;
  color: var(--text-sub);
  margin-top: 3px;
}

.etl-arrow {
  color: var(--secondary-color);
  font-weight: bold;
  font-size: 18px;
  flex-shrink: 0;
}

.clean-bottom {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 250px;
}

html.light {
  .etl-node {
    background: #ffffff !important;
    border-color: #cbd5e1 !important;
    box-shadow: 0 2px 6px rgba(15, 23, 42, 0.05) !important;
    &.active-node {
      border-color: #0284c7 !important;
      background: #f0f9ff !important;
      box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15) !important;
    }
  }
}
/* =========================================================
   移动端适配 (≤768px)：统计卡单列 + ETL 节点收紧 + 底部三图堆叠
========================================================= */
@media (max-width: 768px) {
  .clean-page {
    height: auto;
    gap: 10px;
  }

  /* 顶部 4 张清洗指标卡：80px 固定高度塞 4 张会挤压文字，改纵向单列 */
  .stat-banner {
    flex-direction: column;
    height: auto;
    gap: 10px;
  }

  .stat-card {
    width: 100%;
    min-height: 60px;
    padding: 8px 12px;
  }

  .stat-icon {
    width: 38px;
    height: 38px;
    font-size: 20px;
  }

  .stat-value { font-size: 19px; }

  /* ETL 流程节点：收紧内边距，保留原有横向滚动并可惯性滑动 */
  .etl-flow-wrap {
    padding: 0 10px;
    -webkit-overflow-scrolling: touch;
  }

  .etl-node {
    min-width: 100px;
    padding: 6px 10px;
  }

  .etl-node-header { font-size: 12px; }
  .etl-node-sub { font-size: 10.5px; }

  /* 底部三图并排 → 单列，图表容器保留真实高度 */
  .clean-bottom {
    flex-direction: column;
    min-height: auto;
    gap: 10px;
  }

  .clean-bottom > .dv-border-box {
    flex: none !important;
    width: 100%;
    min-height: auto !important;
    padding: 12px;
  }

  .chart-container {
    flex: none;
    height: 240px;
    min-height: 240px;
  }

  :deep(.dv-title) {
    flex-wrap: wrap;
    gap: 6px;

    .title-text {
      font-size: 12.5px;
      line-height: 1.35;
    }
  }
}
</style>
