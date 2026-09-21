<template>
  <div class="predict-page">
    <!-- 顶部机器学习模型状态栏 -->
    <div class="model-spec-panel">
      <div class="spec-group">
        <div class="spec-item">
          <span class="label">核心算法模型：</span>
          <span class="val">{{ metrics.modelName }}</span>
        </div>
        <div class="spec-item">
          <span class="label">历史训练窗口：</span>
          <span class="val">{{ metrics.trainWindow }}</span>
        </div>
        <div class="spec-item">
          <span class="label">预测推演周期：</span>
          <span class="val">{{ metrics.predictPeriod }}</span>
        </div>
      </div>
      <div class="metrics-chips-wrap">
        <div class="metric-chip">MAE: <strong>{{ metrics.mae }}</strong></div>
        <div class="metric-chip">RMSE: <strong>{{ metrics.rmse }}</strong></div>
        <div class="metric-chip chip-success">
          置信拟合度 R²: <strong>{{ metrics.r2 }}</strong>
        </div>
      </div>
    </div>

    <!-- 中间：高校总量增长与热门专业新增预测 -->
    <div class="predict-row">
      <DvBorderBox
        title="全国高校数量增长与未来 5 年预测 (实线历史 / 虚线推演)"
        style="flex: 1; min-height: 270px;"
      >
        <div ref="univChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox
        title="热门前沿专业 (AI / 大数据 / 软件工程) 增量布点预测模型"
        style="flex: 1; min-height: 270px;"
      >
        <div ref="majorChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>

    <!-- 底部：全国高考适龄人口与招生规模预测趋势 -->
    <div class="predict-row" style="height: 270px; flex-shrink: 0;">
      <DvBorderBox
        title="全国高考适龄人口与招生规模预测趋势 (置信上限与录取拟合)"
        style="flex: 1;"
      >
        <div ref="enrollChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getModelMetrics, getPredictionTrends, ModelMetricsData } from '@/api/predict'

const univChartRef = ref<HTMLDivElement | null>(null)
const majorChartRef = ref<HTMLDivElement | null>(null)
const enrollChartRef = ref<HTMLDivElement | null>(null)

const chartInstances: echarts.ECharts[] = []

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

const metrics = ref<ModelMetricsData>({
  modelName: 'Linear Regression & Polynomial Ridge (Scikit-learn)',
  trainWindow: '2015 - 2025',
  predictPeriod: '未来5年 (2026 - 2030)',
  mae: 3.14,
  rmse: 4.82,
  r2: '96.8%'
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

// 1. 高校数量增长预测
const initUnivChart = (years: string[], history: Array<number | null>, predict: Array<number | null>) => {
  const chart = registerChart(univChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    legend: { data: ['历史统计值', '线性推演预测'], textStyle: { color: '#8ba2d4' }, top: 5 },
    xAxis: {
      type: 'category',
      data: years,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: 2800,
      max: 3400,
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [
      {
        name: '历史统计值',
        type: 'line',
        data: history,
        itemStyle: { color: '#00e5ff' },
        symbolSize: 6
      },
      {
        name: '线性推演预测',
        type: 'line',
        data: predict,
        lineStyle: { type: 'dashed', width: 2, color: '#faad14' },
        itemStyle: { color: '#faad14' },
        symbolSize: 6
      }
    ]
  })
}

// 2. 热门专业预测
const initMajorChart = (years: string[], ai: number[], bigdata: number[], se: number[]) => {
  const chart = registerChart(majorChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    legend: { data: ['人工智能', '大数据技术', '软件工程'], textStyle: { color: '#8ba2d4' }, top: 5 },
    xAxis: {
      type: 'category',
      data: years,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [
      { name: '人工智能', type: 'line', smooth: true, data: ai, itemStyle: { color: '#00e5ff' } },
      { name: '大数据技术', type: 'line', smooth: true, data: bigdata, itemStyle: { color: '#00ffaa' } },
      { name: '软件工程', type: 'line', smooth: true, data: se, itemStyle: { color: '#faad14' } }
    ]
  })
}

// 3. 招生规模置信带预测
const initEnrollChart = (years: string[], enrollTotal: number[], populationLimit: number[]) => {
  const chart = registerChart(enrollChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    legend: { data: ['高考录取总数预测', '适龄生源上限'], textStyle: { color: '#8ba2d4' }, top: 5 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: years,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '万人',
      min: 800,
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: [
      {
        name: '高考录取总数预测',
        type: 'line',
        data: enrollTotal,
        areaStyle: { color: 'rgba(0, 229, 255, 0.25)' },
        itemStyle: { color: '#00e5ff' }
      },
      {
        name: '适龄生源上限',
        type: 'line',
        data: populationLimit,
        lineStyle: { type: 'dashed', color: '#ff4d4f' },
        itemStyle: { color: '#ff4d4f' }
      }
    ]
  })
}

const loadData = async () => {
  try {
    const resMetrics = await getModelMetrics()
    if (resMetrics) metrics.value = resMetrics
  } catch {}

  let univYears = ['2018', '2020', '2022', '2024', '2026(E)', '2028(E)', '2030(E)']
  let history: Array<number | null> = [2956, 3005, 3054, 3072, null, null, null]
  let predict: Array<number | null> = [null, null, null, 3072, 3125, 3180, 3240]

  let majorYears = ['2023', '2024', '2025(E)', '2026(E)', '2027(E)', '2028(E)']
  let ai = [80, 60, 52, 45, 40, 38]
  let bigdata = [120, 90, 75, 62, 55, 50]
  let se = [30, 25, 22, 20, 18, 18]

  let enrollYears = ['2022', '2023', '2024', '2025(E)', '2026(E)', '2027(E)', '2028(E)', '2029(E)', '2030(E)']
  let enrollTotal = [1014, 1042, 1090, 1085, 1070, 1050, 1020, 990, 960]
  let populationLimit = [1193, 1291, 1342, 1310, 1280, 1220, 1180, 1120, 1080]

  try {
    const resTrends = await getPredictionTrends()
    if (resTrends) {
      if (resTrends.univ) {
        univYears = resTrends.univ.years
        history = resTrends.univ.history
        predict = resTrends.univ.predict
      }
      if (resTrends.major) {
        majorYears = resTrends.major.years
        ai = resTrends.major.ai
        bigdata = resTrends.major.bigdata
        se = resTrends.major.se
      }
      if (resTrends.enroll) {
        enrollYears = resTrends.enroll.years
        enrollTotal = resTrends.enroll.enrollTotal
        populationLimit = resTrends.enroll.populationLimit
      }
    }
  } catch {}

  initUnivChart(univYears, history, predict)
  initMajorChart(majorYears, ai, bigdata, se)
  initEnrollChart(enrollYears, enrollTotal, populationLimit)
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
.predict-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100%;
}

.model-spec-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 18px;
  background: rgba(8, 24, 54, 0.7);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-group {
  display: flex;
  gap: 20px;
  font-size: 13px;
  flex-wrap: wrap;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 6px;
  .label { color: var(--text-sub); }
  .val { color: var(--primary-color); font-weight: bold; }
}

.metrics-chips-wrap {
  display: flex;
  gap: 10px;
}

.metric-chip {
  padding: 3px 10px;
  background: rgba(16, 137, 255, 0.2);
  border: 1px solid var(--secondary-color);
  border-radius: 3px;
  font-size: 12px;
  color: #fff;
  &.chip-success {
    border-color: var(--success);
    color: var(--success);
    background: rgba(0, 255, 170, 0.15);
  }
}

.predict-row {
  display: flex;
  gap: 12px;
  flex: 1;
}
</style>
