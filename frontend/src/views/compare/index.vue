<template>
  <div class="compare-page">
    <!-- 顶部工作台：高校搜索、槽位状态与快捷对照 -->
    <div class="compare-control-panel dv-border-box">
      <div class="corner-bottom-left"></div>
      <div class="corner-bottom-right"></div>

      <div class="panel-top-row">
        <!-- 搜索添加高校 (支持全国2993所高校模糊检索) -->
        <div class="search-box">
          <span class="search-label">🏛️ 添加对比院校 (2~4所)：</span>
          <el-select
            v-model="searchKeyword"
            filterable
            remote
            reserve-keyword
            placeholder="输入校名或省份搜索全国高校 (如: 中南、清华、湖南)"
            :remote-method="searchUniversities"
            :loading="searchLoading"
            class="school-search-select"
            @change="handleSelectSchool"
          >
            <el-option
              v-for="u in searchResults"
              :key="u.id"
              :label="`${u.schoolName} (${u.province} · ${u.schoolLevel || u.schoolType})`"
              :value="u.id"
            >
              <div class="search-option-item">
                <span class="opt-name">{{ u.schoolName }}</span>
                <span class="opt-tags">
                  <span class="opt-tag tag-prov">{{ u.province }}</span>
                  <span class="opt-tag tag-lvl">{{ u.schoolLevel || u.schoolType }}</span>
                </span>
              </div>
            </el-option>
          </el-select>
        </div>

        <!-- 快捷对比组合 -->
        <div class="presets-group">
          <span class="presets-label">⚡ 经典对标：</span>
          <button class="preset-pill" @click="loadPreset([1, 2])">清华 vs 北大</button>
          <button class="preset-pill" @click="loadPreset([6, 14])">武大 vs 华科</button>
          <button class="preset-pill" @click="loadPreset([19, 44])">中南 vs 湖大</button>
          <button class="preset-pill" @click="loadPreset([9, 15])">哈工大 vs 西交</button>
          <button class="preset-pill" @click="loadPreset([12, 27])">成电 vs 杭电</button>
        </div>

        <!-- 矩阵比对偏好开关与导出 -->
        <div class="compare-switches">
          <label class="switch-item" title="仅展示各高校指标存在差异的行">
            <input type="checkbox" v-model="onlyDiff" />
            <span>只看差异项</span>
          </label>
          <label class="switch-item" title="自动高亮综合排名与科研优势项">
            <input type="checkbox" v-model="highlightBest" />
            <span>高亮优势标杆</span>
          </label>
          <button class="dv-btn btn-export-compare" @click="handleExportCompare" title="导出当前高校对比矩阵为 Excel 表格">
            <span>📊 导出对比表 (Excel)</span>
          </button>
        </div>
      </div>


      <!-- 对比高校 4 槽位卡片栏 -->
      <div class="school-slots-grid">
        <div
          v-for="(s, idx) in currentSchools"
          :key="s.id"
          class="school-slot-card active-card"
          :style="{ borderColor: colorPalette[idx % colorPalette.length] }"
        >
          <div class="card-color-stripe" :style="{ background: colorPalette[idx % colorPalette.length] }"></div>
          <div class="card-avatar" :style="{ background: `${colorPalette[idx % colorPalette.length]}22`, color: colorPalette[idx % colorPalette.length] }">
            {{ s.schoolName.charAt(0) }}
          </div>
          <div class="card-main-info">
            <div class="card-name-row">
              <h4 class="school-title" @click="goToDetail(s.id)" title="点击查看高校画像详情">
                {{ s.schoolName }}
              </h4>
              <span v-if="s.ruankeRank" class="rank-badge">软科#{{ s.ruankeRank }}</span>
            </div>
            <div class="card-tags-row">
              <span class="c-tag tag-primary">{{ s.schoolLevel }}</span>
              <span class="c-tag tag-secondary">{{ s.schoolType }}</span>
              <span class="c-tag tag-muted">{{ s.province }} · {{ s.city }}</span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn btn-view" @click="goToDetail(s.id)" title="进入高校全景画像">详情 ➔</button>
            <button
              v-if="currentSchools.length > 2"
              class="action-btn btn-remove"
              title="移除该校"
              @click="removeSchool(s.id)"
            >
              ✕
            </button>
          </div>
        </div>

        <!-- 空余槽位占位卡片 -->
        <div
          v-for="emptyIdx in Math.max(0, 4 - currentSchools.length)"
          :key="'empty-' + emptyIdx"
          class="school-slot-card placeholder-card"
        >
          <div class="placeholder-content">
            <span class="plus-icon">+</span>
            <span class="placeholder-text">可继续添加对比高校 (最多4所)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 中部可视化图表对比区 (雷达同屏 + 分数线同屏) -->
    <div class="compare-visual-section">
      <DvBorderBox class="chart-box" title="综合办学实力六维评估 · 同屏雷达对比">
        <div ref="radarChartRef" class="chart-render-container"></div>
      </DvBorderBox>

      <DvBorderBox class="chart-box" title="近五年录取调档基准线 · 同屏走势对比">
        <div ref="scoreChartRef" class="chart-render-container"></div>
      </DvBorderBox>
    </div>

    <!-- 底部硬核量化指标矩阵横向对照表 (Sticky 悬浮表头，自适应滚动，杜绝遮挡) -->
    <div class="compare-matrix-section">
      <DvBorderBox class="matrix-box" title="高校核心量化指标与办学实力横向对照矩阵">
        <template #extra>
          <div class="matrix-stats-hint">
            共对比 <strong>{{ currentSchools.length }}</strong> 所高校 · <strong>{{ filteredMatrixRows.length }}</strong> 项关键量化指标
          </div>
        </template>

        <div class="matrix-scroll-wrapper">
          <table class="matrix-table">
            <thead>
              <tr>
                <th class="col-metric-name sticky-col">量化维度 \ 高校</th>
                <th
                  v-for="(s, idx) in currentSchools"
                  :key="s.id"
                  class="col-school-header"
                  :style="{ borderTopColor: colorPalette[idx % colorPalette.length] }"
                >
                  <div class="header-school-cell">
                    <span class="school-dot" :style="{ background: colorPalette[idx % colorPalette.length] }"></span>
                    <span class="school-name-text">{{ s.schoolName }}</span>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in filteredMatrixRows"
                :key="row.metric"
                :class="{ 'row-diff': isRowDifferent(row) }"
              >
                <td class="metric-title-cell sticky-col">
                  <span class="metric-icon">{{ getMetricIcon(row.metric) }}</span>
                  <span>{{ row.metric }}</span>
                </td>
                <td
                  v-for="s in currentSchools"
                  :key="s.id"
                  class="metric-val-cell"
                  :class="{
                    'cell-winner': highlightBest && isBestVal(row, s.schoolName)
                  }"
                >
                  <!-- 官方门户网站 -->
                  <template v-if="row.metric === '官方门户网站'">
                    <a v-if="s.schoolSite" :href="s.schoolSite" target="_blank" class="table-action-link link-official">
                      官网直达 ↗
                    </a>
                    <span v-else class="text-null">-</span>
                  </template>
                  <!-- 阳光高考权威档案 -->
                  <template v-else-if="row.metric === '掌上高考权威档案'">
                    <a :href="s.gaokaoSite" target="_blank" class="table-action-link link-gaokao">
                      档案源站 ↗
                    </a>
                  </template>
                  <!-- 普通指标值 -->
                  <template v-else>
                    <span class="val-text">
                      {{ row[s.schoolName] || '-' }}
                    </span>
                    <span v-if="highlightBest && isBestVal(row, s.schoolName)" class="best-badge" title="该指标在对比高校中占优">★ 优</span>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getUniversityCompare, UnivCompareResult } from '@/api/compare'
import { pageUniversities, UniversityCard, UnivDetail } from '@/api/university'

const route = useRoute()
const router = useRouter()

const radarChartRef = ref<HTMLDivElement | null>(null)
const scoreChartRef = ref<HTMLDivElement | null>(null)
const chartInstances: echarts.ECharts[] = []

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

import { currentTheme, getChartTheme, onThemeChange } from '@/utils/theme'

const getPalette = () => {
  return currentTheme.value === 'dark'
    ? ['#00e5ff', '#ff4d4f', '#00ffaa', '#faad14', '#b37feb']
    : ['#0284c7', '#dc2626', '#059669', '#d97706', '#7c3aed']
}
const colorPalette = computed(() => getPalette())

// 搜索与选校状态
const searchKeyword = ref<number | ''>('')
const searchLoading = ref(false)
const searchResults = ref<UniversityCard[]>([])
const selectedIds = ref<number[]>([1, 2])

const currentSchools = ref<UnivDetail[]>([])
const matrixRows = ref<Array<Record<string, any>>>([])

// 开关：只看差异项 / 高亮优势标杆
const onlyDiff = ref(false)
const highlightBest = ref(true)

// 模糊搜索全国高校
const searchUniversities = async (query: string) => {
  if (!query || query.trim().length === 0) {
    searchResults.value = []
    return
  }
  searchLoading.value = true
  try {
    const res = await pageUniversities({ page: 1, size: 25, schoolName: query.trim() })
    searchResults.value = res.records
  } catch {
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

// 选中高校添加到对比列表
const handleSelectSchool = (id: number) => {
  if (!id) return
  if (selectedIds.value.includes(id)) {
    ElMessage.info('该高校已在对比列表中')
    searchKeyword.value = ''
    return
  }
  if (selectedIds.value.length >= 4) {
    ElMessage.warning('最多同时对比 4 所高校，请先移除其中一所')
    searchKeyword.value = ''
    return
  }
  selectedIds.value.push(id)
  searchKeyword.value = ''
  loadCompareData()
  ElMessage.success('已加入对比')
}

// 移除对比高校
const removeSchool = (id: number) => {
  if (selectedIds.value.length <= 2) {
    ElMessage.warning('对比列表至少保留 2 所高校')
    return
  }
  selectedIds.value = selectedIds.value.filter(i => i !== id)
  loadCompareData()
}

// 切换快捷预设对
const loadPreset = (ids: number[]) => {
  selectedIds.value = [...ids]
  loadCompareData()
}

// 跳转至高校画像详情
const goToDetail = (id: number) => {
  router.push(`/university/${id}`)
}

// 判断当前行在各校间是否数值不同
const isRowDifferent = (row: Record<string, any>) => {
  if (!currentSchools.value || currentSchools.value.length < 2) return false
  const firstVal = row[currentSchools.value[0].schoolName]
  for (let i = 1; i < currentSchools.value.length; i++) {
    if (row[currentSchools.value[i].schoolName] !== firstVal) {
      return true
    }
  }
  return false
}

// 过滤后的矩阵行
const filteredMatrixRows = computed(() => {
  if (!onlyDiff.value) return matrixRows.value
  return matrixRows.value.filter(row => isRowDifferent(row))
})

// 判断数值是否在该行处于领先优势
const isBestVal = (row: Record<string, any>, schoolName: string) => {
  const metric = row.metric
  const val = row[schoolName]
  if (!val || val === '-' || val === '全国前列') return false

  // 1. 排名越小越好
  if (metric.includes('排名')) {
    const match = val.match(/\d+/)
    if (!match) return false
    const currentNum = parseInt(match[0], 10)
    let isMin = true
    for (const s of currentSchools.value) {
      const sVal = row[s.schoolName]
      const sMatch = sVal && sVal.match(/\d+/)
      if (sMatch) {
        const num = parseInt(sMatch[0], 10)
        if (num < currentNum) {
          isMin = false
          break
        }
      }
    }
    return isMin
  }

  // 2. 数量越大越好 (博士点、院士数、重点实验室、建校历史)
  if (metric.includes('院士') || metric.includes('实验室') || metric.includes('博士点') || metric.includes('建校历史')) {
    const match = val.match(/\d+/)
    if (!match) return false
    const currentNum = parseInt(match[0], 10)
    // 历史越悠久越好 (年份越小)
    if (metric.includes('建校历史')) {
      let isEarliest = true
      for (const s of currentSchools.value) {
        const sVal = row[s.schoolName]
        const sMatch = sVal && sVal.match(/\d+/)
        if (sMatch && parseInt(sMatch[0], 10) < currentNum) {
          isEarliest = false
          break
        }
      }
      return isEarliest
    } else {
      let isMax = true
      for (const s of currentSchools.value) {
        const sVal = row[s.schoolName]
        const sMatch = sVal && sVal.match(/\d+/)
        if (sMatch && parseInt(sMatch[0], 10) > currentNum) {
          isMax = false
          break
        }
      }
      return isMax && currentNum > 0
    }
  }

  // 3. 办学层次
  if (metric === '办学层次' && (val.includes('985') || val.includes('双一流'))) {
    return true
  }

  return false
}

const getMetricIcon = (metric: string) => {
  if (metric.includes('层次') || metric.includes('性质')) return '🏛️'
  if (metric.includes('类型')) return '🎓'
  if (metric.includes('省市')) return '📍'
  if (metric.includes('主管部门')) return '🏢'
  if (metric.includes('排名')) return '🏆'
  if (metric.includes('院士')) return '👨‍🏫'
  if (metric.includes('实验室')) return '🔬'
  if (metric.includes('博士点')) return '📚'
  if (metric.includes('建校历史')) return '⏳'
  if (metric.includes('网站') || metric.includes('档案')) return '🌐'
  return '📌'
}

let cachedRadarData: UnivCompareResult['radarComparison'] | null = null
let cachedScoreData: UnivCompareResult['scoreComparison'] | null = null
let unregisterTheme: (() => void) | null = null

// 渲染综合办学实力多维雷达对比
const initRadarChart = (data: UnivCompareResult['radarComparison']) => {
  cachedRadarData = data
  const chart = registerChart(radarChartRef.value)
  if (!chart) return
  const theme = getChartTheme()
  const palette = getPalette()

  const seriesData = data.series.map((item, idx) => ({
    name: item.name,
    value: item.value,
    itemStyle: { color: palette[idx % palette.length] },
    areaStyle: {
      color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
        { offset: 0, color: 'rgba(0,0,0,0)' },
        { offset: 1, color: `${palette[idx % palette.length]}33` }
      ])
    }
  }))

  chart.setOption({
    tooltip: {
      trigger: 'item',
      ...theme.tooltip
    },
    legend: {
      data: data.series.map(s => s.name),
      textStyle: { color: theme.textColor, fontSize: 11 },
      top: 4,
      itemWidth: 10,
      itemHeight: 10
    },
    radar: {
      indicator: data.indicators,
      shape: 'polygon',
      splitNumber: 4,
      center: ['50%', '55%'],
      radius: '68%',
      axisName: { color: theme.textColor, fontSize: 11 },
      splitLine: { lineStyle: { color: theme.splitLineColor } },
      splitArea: {
        show: true,
        areaStyle: {
          color: theme.isDark 
            ? ['rgba(5, 18, 43, 0.8)', 'rgba(8, 28, 68, 0.8)'] 
            : ['rgba(241, 245, 249, 0.8)', 'rgba(255, 255, 255, 0.8)']
        }
      },
      axisLine: { lineStyle: { color: theme.axisLineColor } }
    },
    series: [{
      type: 'radar',
      data: seriesData
    }]
  })
}

// 渲染近五年投档分数线对比图 (自适应Y轴)
const initScoreChart = (data: UnivCompareResult['scoreComparison']) => {
  cachedScoreData = data
  const chart = registerChart(scoreChartRef.value)
  if (!chart) return
  const theme = getChartTheme()
  const palette = getPalette()

  // 计算全局最低分与最高分以自适应 Y 轴
  let minScore = 750
  let maxScore = 300
  data.series.forEach(s => {
    s.data.forEach(v => {
      if (v < minScore) minScore = v
      if (v > maxScore) maxScore = v
    })
  })
  const yMin = Math.max(300, Math.floor(minScore / 20) * 20 - 20)
  const yMax = Math.min(750, Math.ceil(maxScore / 20) * 20 + 20)

  const series = data.series.map((s, idx) => ({
    name: s.name,
    type: 'line',
    data: s.data,
    smooth: true,
    symbolSize: 6,
    itemStyle: { color: palette[idx % palette.length] },
    lineStyle: { width: 2.5 }
  }))

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      ...theme.tooltip
    },
    legend: {
      data: data.series.map(s => s.name),
      textStyle: { color: theme.textColor, fontSize: 11 },
      top: 4,
      itemWidth: 10,
      itemHeight: 10
    },
    grid: { top: 35, right: 25, bottom: 25, left: 45 },
    xAxis: {
      type: 'category',
      data: data.years,
      axisLine: { lineStyle: { color: theme.axisLineColor } },
      axisLabel: { color: theme.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: yMin,
      max: yMax,
      splitLine: { lineStyle: { color: theme.splitLineColor } },
      axisLabel: { color: theme.textColor, fontSize: 11 }
    },
    series
  })
}

// 导出对比矩阵为 Excel (CSV)
const handleExportCompare = () => {
  if (!currentSchools.value || currentSchools.value.length === 0) {
    ElMessage.warning('暂无高校对比数据可供导出')
    return
  }

  const schoolNames = currentSchools.value.map(s => s.schoolName)
  const headers = ['量化对比维度', ...schoolNames]
  const rows: string[] = [headers.join(',')]

  matrixRows.value.forEach(row => {
    const cells = [
      row.metric,
      ...schoolNames.map(name => `"${(row[name] || '-').replace(/"/g, '""')}"`)
    ]
    rows.push(cells.join(','))
  })

  const csvContent = '\uFEFF' + rows.join('\r\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `高校多维综合对比量化矩阵_${schoolNames.join('_vs_')}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)

  ElMessage.success('高校横向对比矩阵报表已成功导出！')
}

// 加载对比数据
const loadCompareData = async () => {
  try {
    const res = await getUniversityCompare(selectedIds.value)
    currentSchools.value = res.schools
    matrixRows.value = [
      ...res.metricMatrix,
      { metric: '官方门户网站' },
      { metric: '掌上高考权威档案' }
    ]

    nextTick(() => {
      initRadarChart(res.radarComparison)
      initScoreChart(res.scoreComparison)
    })
  } catch (err: any) {
    ElMessage.error(err?.message || '获取多校对比数据失败')
  }
}

const handleResize = () => {
  chartInstances.forEach(c => c.resize())
}

onMounted(() => {
  // 接收路由传参
  const queryIds = route.query.ids as string
  const addId = route.query.addId ? Number(route.query.addId) : null

  if (queryIds) {
    selectedIds.value = queryIds.split(',').map(Number)
  } else if (addId) {
    if (!selectedIds.value.includes(addId)) {
      selectedIds.value.push(addId)
    }
  }

  loadCompareData()
  window.addEventListener('resize', handleResize)
  unregisterTheme = onThemeChange(() => {
    nextTick(() => {
      if (cachedRadarData) initRadarChart(cachedRadarData)
      if (cachedScoreData) initScoreChart(cachedScoreData)
    })
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (unregisterTheme) unregisterTheme()
  chartInstances.forEach(c => c.dispose())
  chartInstances.length = 0
})
</script>


<style scoped lang="scss">
.compare-page {
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 24px;
}

/* =========================================================
   1. 顶部工作台控制面板
========================================================= */
.compare-control-panel {
  padding: 14px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(8, 24, 54, 0.85);
}

.panel-top-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 320px;
}

.search-label {
  font-size: 13px;
  color: var(--text-sub);
  white-space: nowrap;
}

.school-search-select {
  flex: 1;
  max-width: 460px;
}

.search-option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.opt-name {
  font-weight: bold;
  color: #fff;
}

.opt-tags {
  display: flex;
  gap: 4px;
}

.opt-tag {
  font-size: 10.5px;
  padding: 1px 5px;
  border-radius: 2px;
}
.tag-prov { background: rgba(0, 229, 255, 0.15); color: #00e5ff; }
.tag-lvl { background: rgba(250, 173, 20, 0.15); color: #faad14; }

.presets-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.presets-label {
  font-size: 12px;
  color: #8ba2d4;
}

.preset-pill {
  font-size: 11.5px;
  padding: 3px 8px;
  border-radius: 3px;
  background: rgba(16, 137, 255, 0.15);
  border: 1px solid rgba(16, 137, 255, 0.35);
  color: #82c0ff;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: rgba(0, 229, 255, 0.25);
    border-color: #00e5ff;
    color: #00e5ff;
    transform: translateY(-1px);
  }
}

.compare-switches {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #c0ccda;
  cursor: pointer;
  user-select: none;
  input[type="checkbox"] {
    cursor: pointer;
    accent-color: #00e5ff;
  }
}

/* =========================================================
   2. 高校 4 槽位卡片网格
========================================================= */
.school-slots-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.school-slot-card {
  position: relative;
  background: rgba(13, 35, 74, 0.7);
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 72px;
  transition: all 0.3s;
}

.card-color-stripe {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: 6px 0 0 6px;
}

.card-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.card-main-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.card-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.school-title {
  margin: 0;
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  &:hover {
    color: #00e5ff;
    text-decoration: underline;
  }
}

.rank-badge {
  font-size: 10.5px;
  color: #faad14;
  background: rgba(250, 173, 20, 0.15);
  padding: 1px 5px;
  border-radius: 2px;
  flex-shrink: 0;
}

.card-tags-row {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.c-tag {
  font-size: 10.5px;
  padding: 1px 5px;
  border-radius: 2px;
}
.tag-primary { background: rgba(0, 229, 255, 0.15); color: #00e5ff; }
.tag-secondary { background: rgba(16, 137, 255, 0.15); color: #82c0ff; }
.tag-muted { background: rgba(255, 255, 255, 0.08); color: #8ba2d4; }

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  border: none;
  cursor: pointer;
  border-radius: 3px;
  font-size: 11px;
  padding: 2px 6px;
  transition: all 0.2s;
}

.btn-view {
  background: rgba(0, 229, 255, 0.15);
  color: #00e5ff;
  border: 1px solid rgba(0, 229, 255, 0.3);
  &:hover {
    background: rgba(0, 229, 255, 0.3);
  }
}

.btn-remove {
  background: rgba(255, 77, 79, 0.15);
  color: #ff7875;
  border: 1px solid rgba(255, 77, 79, 0.3);
  &:hover {
    background: rgba(255, 77, 79, 0.3);
  }
}

.placeholder-card {
  border-style: dashed;
  border-color: rgba(0, 229, 255, 0.15);
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #5f75a6;
  font-size: 12px;
}

.plus-icon {
  font-size: 16px;
  color: #5f75a6;
}

/* =========================================================
   3. 中部可视化图表对比区
========================================================= */
.compare-visual-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  min-height: 310px;
}

.chart-box {
  min-height: 310px;
}

.chart-render-container {
  width: 100%;
  height: 100%;
  min-height: 260px;
}

/* =========================================================
   4. 底部硬核量化指标对照矩阵 (Sticky 表头防遮挡)
========================================================= */
.compare-matrix-section {
  width: 100%;
}

.matrix-box {
  width: 100%;
}

.matrix-stats-hint {
  font-size: 12px;
  color: #8ba2d4;
  strong {
    color: #00e5ff;
  }
}

.matrix-scroll-wrapper {
  width: 100%;
  overflow-x: auto;
  max-height: 480px;
  overflow-y: auto;
  border: 1px solid rgba(0, 229, 255, 0.15);
  border-radius: 4px;
}

.matrix-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12.5px;
  color: #c0ccda;

  th, td {
    padding: 9px 14px;
    border: 1px solid rgba(0, 229, 255, 0.12);
    text-align: center;
  }

  /* 表头置顶 (Sticky Header) */
  thead th {
    position: sticky;
    top: 0;
    z-index: 10;
    background: rgba(10, 30, 68, 0.96);
    backdrop-filter: blur(8px);
    border-top: 3px solid transparent;
    font-size: 13px;
  }

  /* 首列置顶 (Sticky Column) */
  .sticky-col {
    position: sticky;
    left: 0;
    z-index: 5;
    background: rgba(8, 24, 56, 0.95);
    backdrop-filter: blur(8px);
    text-align: left;
    font-weight: bold;
    width: 200px;
    min-width: 180px;
  }

  thead th.sticky-col {
    z-index: 15;
  }

  .header-school-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .school-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  .school-name-text {
    font-size: 13.5px;
    font-weight: bold;
    color: #fff;
  }

  tbody tr {
    transition: background 0.2s;
    &:nth-child(even) {
      background: rgba(255, 255, 255, 0.015);
    }
    &:hover {
      background: rgba(0, 229, 255, 0.05);
    }
  }

  .row-diff {
    background: rgba(0, 229, 255, 0.025);
  }

  .metric-title-cell {
    color: #8ba2d4;
    display: flex;
    align-items: center;
    gap: 6px;
    border-right: 2px solid rgba(0, 229, 255, 0.2);
  }

  .metric-icon {
    font-size: 13px;
  }

  .metric-val-cell {
    position: relative;
  }

  .cell-winner {
    background: rgba(250, 173, 20, 0.08) !important;
    border-color: rgba(250, 173, 20, 0.3) !important;
    .val-text {
      color: #faad14 !important;
      font-weight: bold;
    }
  }

  .best-badge {
    position: absolute;
    right: 6px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 10px;
    color: #faad14;
    background: rgba(250, 173, 20, 0.15);
    padding: 1px 4px;
    border-radius: 2px;
  }

  .table-action-link {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 3px;
    font-size: 11.5px;
    text-decoration: none;
    transition: all 0.2s;
  }

  .link-official {
    color: #00e5ff;
    background: rgba(0, 229, 255, 0.1);
    border: 1px solid rgba(0, 229, 255, 0.3);
    &:hover {
      background: rgba(0, 229, 255, 0.25);
      text-decoration: underline;
    }
  }

  .link-gaokao {
    color: #faad14;
    background: rgba(250, 173, 20, 0.1);
    border: 1px solid rgba(250, 173, 20, 0.3);
    &:hover {
      background: rgba(250, 173, 20, 0.25);
      text-decoration: underline;
    }
  }

  .text-null {
    color: #556987;
  }
}

/* 响应式断点适配 */
@media (max-width: 1200px) {
  .school-slots-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .compare-visual-section {
    grid-template-columns: 1fr;
  }
}

/* 浅色主题全面深度适配 */
html.light {
  .compare-control-panel {
    background: #ffffff !important;
    border-color: #cbd5e1 !important;
    box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06) !important;
  }

  .search-label, .presets-label, .switch-item {
    color: #334155 !important;
  }

  .opt-name {
    color: #0f172a !important;
  }

  .preset-pill {
    background: #f0f9ff !important;
    border: 1px solid #bae6fd !important;
    color: #0284c7 !important;
    &:hover {
      background: #e0f2fe !important;
      border-color: #38bdf8 !important;
      color: #0369a1 !important;
    }
  }

  .school-slot-card.active-card {
    background: #ffffff !important;
    box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06) !important;
  }

  .school-slot-card.placeholder-card {
    background: #f8fafc !important;
    border: 1.5px dashed #cbd5e1 !important;
    .placeholder-content, .plus-icon {
      color: #64748b !important;
      font-weight: 500;
    }
  }

  .school-title {
    color: #0f172a !important;
    &:hover {
      color: #0284c7 !important;
    }
  }

  .rank-badge {
    color: #b45309 !important;
    background: #fef3c7 !important;
    border: 1px solid #fde68a !important;
  }

  .tag-primary {
    background: #e0f2fe !important;
    color: #0284c7 !important;
  }
  .tag-secondary {
    background: #ede9fe !important;
    color: #6366f1 !important;
  }
  .tag-muted {
    background: #f1f5f9 !important;
    color: #64748b !important;
  }

  .btn-view {
    background: #e0f2fe !important;
    color: #0284c7 !important;
    border-color: #bae6fd !important;
    &:hover {
      background: #bae6fd !important;
    }
  }

  .btn-remove {
    background: #fee2e2 !important;
    color: #dc2626 !important;
    border-color: #fca5a5 !important;
    &:hover {
      background: #fecaca !important;
    }
  }

  .matrix-stats-hint {
    color: #475569 !important;
    strong {
      color: #0284c7 !important;
    }
  }

  .matrix-scroll-wrapper {
    border-color: #cbd5e1 !important;
  }

  .matrix-table {
    color: #0f172a !important;

    th, td {
      border-color: #e2e8f0 !important;
    }

    thead th {
      background: #f8fafc !important;
      color: #0f172a !important;
      border-bottom: 2px solid #cbd5e1 !important;
    }

    .sticky-col {
      background: #f8fafc !important;
      color: #0f172a !important;
      border-right: 2px solid #cbd5e1 !important;
    }

    thead th.sticky-col {
      background: #f1f5f9 !important;
    }

    .school-name-text {
      color: #0f172a !important;
    }

    .metric-title-cell {
      color: #0f172a !important;
      font-weight: 600;
      border-right-color: #cbd5e1 !important;
    }

    tbody tr:nth-child(even) {
      background: #f8fafc !important;
    }

    tbody tr:hover {
      background: #f0f9ff !important;
    }

    .row-diff {
      background: rgba(14, 165, 233, 0.05) !important;
    }

    .cell-winner {
      background: #fffbeb !important;
      border-color: #fde68a !important;
      .val-text {
        color: #b45309 !important;
        font-weight: 700;
      }
    }

    .best-badge {
      color: #b45309 !important;
      background: #fef3c7 !important;
      border: 1px solid #fde68a !important;
    }

    .link-official {
      color: #0284c7 !important;
      background: #e0f2fe !important;
      border: 1px solid #bae6fd !important;
      &:hover {
        background: #bae6fd !important;
      }
    }

    .link-gaokao {
      color: #b45309 !important;
      background: #fef3c7 !important;
      border: 1px solid #fde68a !important;
      &:hover {
        background: #fde68a !important;
      }
    }

    .text-null {
      color: #94a3b8 !important;
    }
  }

  .btn-export-compare {
    background: #f0f9ff !important;
    color: #0284c7 !important;
    border-color: #7dd3fc !important;
    &:hover {
      background: #e0f2fe !important;
    }
  }
}
</style>

