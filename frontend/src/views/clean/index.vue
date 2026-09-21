<template>
  <div class="clean-page">
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

const compareChartRef = ref<HTMLDivElement | null>(null)
const qualityChartRef = ref<HTMLDivElement | null>(null)
const dailyChartRef = ref<HTMLDivElement | null>(null)

const chartInstances: echarts.ECharts[] = []

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

const summary = ref<CleanSummaryData>({
  rawCount: 1250000,
  duplicateCount: 35000,
  errorCount: 8200,
  cleanCount: 1206800
})

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

// 1. 清洗前后对比
const initCompareChart = (categories: string[], raw: number[], clean: number[]) => {
  const chart = registerChart(compareChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    grid: { top: 35, right: 20, bottom: 38, left: 45 },
    tooltip: { trigger: 'axis' },
    legend: { data: ['原始数据', '清洗后入库'], textStyle: { color: '#8ba2d4' }, top: 5 },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [
      {
        name: '原始数据',
        type: 'bar',
        data: raw,
        itemStyle: { color: 'rgba(16, 137, 255, 0.45)', borderRadius: [3, 3, 0, 0] }
      },
      {
        name: '清洗后入库',
        type: 'bar',
        data: clean,
        itemStyle: { color: '#00e5ff', borderRadius: [3, 3, 0, 0] }
      }
    ]
  })
}

// 2. 质量占比饼图
const initQualityChart = (data: Array<{ value: number; name: string }>) => {
  const chart = registerChart(qualityChartRef.value)
  if (!chart) return

  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', textStyle: { color: '#8ba2d4' }, itemWidth: 10, itemHeight: 10 },
    color: ['#00ffaa', '#faad14', '#ff4d4f'],
    series: [{
      name: '数据质量',
      type: 'pie',
      radius: ['38%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#05122b', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#fff', fontSize: 11 },
      data
    }]
  })
}

// 3. 7日吞吐趋势折线图
const initDailyChart = (dates: string[], throughput: number[]) => {
  const chart = registerChart(dailyChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [{
      name: '清洗吞吐量',
      type: 'line',
      data: throughput,
      smooth: true,
      itemStyle: { color: '#00ffaa' },
      symbolSize: 6,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 255, 170, 0.45)' },
          { offset: 1, color: 'rgba(0, 255, 170, 0.02)' }
        ])
      }
    }]
  })
}

const loadData = async () => {
  try {
    const resSummary = await getCleanSummary()
    if (resSummary) summary.value = resSummary
  } catch {}

  let categories = ['高校名录', '招生计划', '分数线', '专业目录', '就业指标']
  let raw = [35, 52, 48, 12, 18]
  let clean = [33, 49, 45, 11.5, 17.2]
  let quality = [
    { value: 1206800, name: '有效标准数据' },
    { value: 35000, name: '重复过滤数据' },
    { value: 8200, name: '异常脏数据修复' }
  ]
  let dates = ['09-14', '09-15', '09-16', '09-17', '09-18', '09-19', '09-20']
  let throughput = [15.2, 16.8, 14.5, 18.2, 20.1, 19.5, 21.3]

  try {
    const resCharts = await getCleanCharts()
    if (resCharts) {
      if (resCharts.compare) {
        categories = resCharts.compare.categories
        raw = resCharts.compare.raw
        clean = resCharts.compare.clean
      }
      if (resCharts.quality) quality = resCharts.quality
      if (resCharts.daily) {
        dates = resCharts.daily.dates
        throughput = resCharts.daily.throughput
      }
    }
  } catch {}

  initCompareChart(categories, raw, clean)
  initQualityChart(quality)
  initDailyChart(dates, throughput)
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
.clean-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  background: rgba(8, 24, 54, 0.85);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 8px 14px;
  text-align: center;
  position: relative;
  min-width: 115px;
  flex-shrink: 0;

  &.active-node {
    border-color: var(--primary-color);
    box-shadow: 0 0 10px rgba(0, 229, 255, 0.35);
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
</style>
