<template>
  <div class="predict-page">
    <!-- 顶部机器学习模型状态栏 -->
    <div class="model-spec-panel">
      <div class="spec-group">
        <div class="spec-item">
          <span class="label">核心算法模型：</span>
          <span class="val">{{ metrics?.modelName || '-' }}</span>
        </div>
        <div class="spec-item">
          <span class="label">历史训练窗口：</span>
          <span class="val">{{ metrics?.trainWindow || '-' }}</span>
        </div>
        <div class="spec-item">
          <span class="label">预测推演周期：</span>
          <span class="val">{{ metrics?.predictPeriod || '-' }}</span>
        </div>
      </div>
      <div class="metrics-chips-wrap">
        <div class="metric-chip">MAE: <strong>{{ metrics?.mae ?? '-' }}</strong></div>
        <div class="metric-chip">RMSE: <strong>{{ metrics?.rmse ?? '-' }}</strong></div>
        <div class="metric-chip chip-success">
          置信拟合度 R²: <strong>{{ metrics?.r2 ?? '-' }}</strong>
        </div>
      </div>
    </div>

    <div v-if="predictionLoad.state.value === 'error'" class="data-status error-status">
      {{ predictionLoad.errorMessage.value }}
      <button class="dv-btn" @click="loadData">重试</button>
    </div>
    <div v-else-if="predictionLoad.state.value === 'empty'" class="data-status">
      当前没有可用的预测结果，请先运行模型训练脚本并写入 prediction_result 表。
    </div>
    <div v-else-if="metrics" class="data-source-note">
      数据来源：{{ metrics.dataSource || 'prediction_result' }}
      <span v-if="metrics.generatedAt">· 生成时间：{{ metrics.generatedAt }}</span>
      · 页面中的虚线为模型预测值，不代表已发生的统计结果
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
import { getChartTheme, onThemeChange } from '@/utils/theme'
import { createLoadState } from '@/utils/loadState'

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

const metrics = ref<ModelMetricsData | null>(null)
const predictionLoad = createLoadState()

// 1. 高校数量增长预测
const initUnivChart = (years: string[], history: Array<number | null>, predict: Array<number | null>) => {
  const chart = registerChart(univChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    legend: { data: ['历史统计值', '线性推演预测'], textStyle: { color: ct.textColor }, top: 5 },
    xAxis: {
      type: 'category',
      data: years,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: 2800,
      max: 3400,
      splitLine: { lineStyle: { color: ct.splitLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
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
  }, true)
}

// 2. 热门专业预测
const initMajorChart = (years: string[], ai: Array<number | null>, bigdata: Array<number | null>, se: Array<number | null>) => {
  const chart = registerChart(majorChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    legend: { data: ['人工智能', '大数据技术', '软件工程'], textStyle: { color: ct.textColor }, top: 5 },
    xAxis: {
      type: 'category',
      data: years,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: ct.splitLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    series: [
      { name: '人工智能', type: 'line', smooth: true, data: ai, itemStyle: { color: '#00e5ff' } },
      { name: '大数据技术', type: 'line', smooth: true, data: bigdata, itemStyle: { color: '#00ffaa' } },
      { name: '软件工程', type: 'line', smooth: true, data: se, itemStyle: { color: '#faad14' } }
    ]
  }, true)
}

// 3. 招生规模置信带预测
const initEnrollChart = (years: string[], enrollTotal: Array<number | null>, populationLimit: Array<number | null>) => {
  const chart = registerChart(enrollChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    legend: { data: ['高考录取总数预测', '适龄生源上限'], textStyle: { color: ct.textColor }, top: 5 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: years,
      axisLine: { lineStyle: { color: ct.axisLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '万人',
      min: 800,
      splitLine: { lineStyle: { color: ct.splitLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11 }
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
  }, true)
}

const loadData = async () => {
  predictionLoad.start()
  chartInstances.forEach(chart => chart.clear())

  try {
    const [resMetrics, resTrends] = await Promise.all([getModelMetrics(), getPredictionTrends()])
    if (!resMetrics || !resTrends?.univ || !resTrends.major || !resTrends.enroll) {
      predictionLoad.succeed(true)
      return
    }

    metrics.value = resMetrics
    initUnivChart(resTrends.univ.years, resTrends.univ.history, resTrends.univ.predict)
    initMajorChart(resTrends.major.years, resTrends.major.ai, resTrends.major.bigdata, resTrends.major.se)
    initEnrollChart(resTrends.enroll.years, resTrends.enroll.enrollTotal, resTrends.enroll.populationLimit)
    predictionLoad.succeed(false)
  } catch (error) {
    console.error('Failed to load prediction data:', error)
    predictionLoad.fail('预测数据加载失败，请确认后端服务和 prediction_result 数据是否可用。')
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
    loadData()
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
  background: var(--bg-panel);
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
  background: rgba(16, 137, 255, 0.15);
  border: 1px solid var(--border-color);
  border-radius: 3px;
  font-size: 12px;
  color: var(--text-main);
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

.data-status,
.data-source-note {
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

.data-source-note {
  font-size: 12px;
  color: var(--text-muted);
}
/* =========================================================
   移动端适配 (≤768px)：预测图表行由并排改为单列
========================================================= */
@media (max-width: 768px) {
  .predict-page {
    height: auto;
    gap: 10px;
  }

  /* 模型状态栏：模型参数与指标芯片换行排列 */
  .model-spec-panel {
    align-items: flex-start;
    padding: 10px 12px;
    gap: 8px;
  }

  .spec-group {
    gap: 4px 12px;
    font-size: 12px;
  }

  .metrics-chips-wrap {
    flex-wrap: wrap;
    gap: 6px;
  }

  .metric-chip {
    padding: 3px 8px;
    font-size: 11px;
  }

  /* 双图并排会各被压到 ~165px；底部行还写死了 270px 高度，需一并覆盖 */
  .predict-row {
    flex: none;
    flex-direction: column;
    height: auto !important;
    gap: 10px;
  }

  .predict-row > .dv-border-box {
    flex: none !important;
    width: 100%;
    min-height: auto !important;
    padding: 12px;
  }

  /* ECharts 容器保留真实高度 */
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

  .data-status,
  .data-source-note {
    padding: 8px 12px;
    font-size: 11.5px;
    line-height: 1.5;
  }

  .error-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
