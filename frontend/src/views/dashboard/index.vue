<template>
  <div class="dashboard-page">
    <!-- 顶部核心指标看板 -->
    <div class="stat-banner">
      <DvBorderBox class="stat-card">
        <div class="stat-icon icon-cyan">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 21h18M3 10h18M5 6l7-3 7 3M4 10v11M20 10v11M8 14v3M12 14v3M16 14v3"></path>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-header-row">
            <span class="stat-label">全国高校总数</span>
            <span class="stat-chip chip-cyan">教育部备案</span>
          </div>
          <div class="stat-value">{{ stats.totalUniversity.toLocaleString() }}<span>所</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon icon-blue">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 10v6M2 10l10-5 10 5-10 5z"></path>
            <path d="M6 12v5c3 3 9 3 12 0v-5"></path>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-header-row">
            <span class="stat-label">本科高校数量</span>
            <span class="stat-chip chip-blue">占比 49.6%</span>
          </div>
          <div class="stat-value">{{ stats.undergraduateCount.toLocaleString() }}<span>所</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon icon-indigo">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            <line x1="9" y1="7" x2="15" y2="7"></line>
            <line x1="9" y1="11" x2="15" y2="11"></line>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-header-row">
            <span class="stat-label">开设学科大类</span>
            <span class="stat-chip chip-indigo">12大学科门类</span>
          </div>
          <div class="stat-value">{{ stats.majorCount.toLocaleString() }}<span>类</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon icon-amber">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-header-row">
            <span class="stat-label">覆盖省份与地区</span>
            <span class="stat-chip chip-amber">全域覆盖</span>
          </div>
          <div class="stat-value">{{ stats.provinceCount.toLocaleString() }}<span>个</span></div>
        </div>
      </DvBorderBox>
    </div>

    <!-- 中部：地图分布与类型占比 -->
    <div class="dashboard-middle">
      <DvBorderBox class="map-wrap" title="全国高校地理分布热力图 (支持滚轮缩放/点击下钻)">
        <div ref="mapChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox class="type-wrap" title="全国高校办学类型结构分析">
        <div ref="typeChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>

    <!-- 底部：4 大统计走势图表 -->
    <div class="dashboard-bottom">
      <DvBorderBox class="bottom-chart-box" :title="trendMode === 'decade' ? '全国高校建校年代演进与办学积淀' : '近十年全国高校数量平滑增长走势'">
        <template #extra>
          <div class="trend-switch-btns">
            <button :class="['switch-btn', { active: trendMode === 'decade' }]" @click="switchTrendMode('decade')">历史演进</button>
            <button :class="['switch-btn', { active: trendMode === 'tenYear' }]" @click="switchTrendMode('tenYear')">近十年</button>
          </div>
        </template>
        <div ref="trendChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox class="bottom-chart-box" title="各省高校数量 TOP10">
        <div ref="rankChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox class="bottom-chart-box" title="全国热门专业 TOP10 (极坐标玫瑰图)">
        <div ref="hotMajorChartRef" class="chart-container"></div>
      </DvBorderBox>

      <DvBorderBox class="bottom-chart-box" title="近五年全国总招生人数趋势 (万人)">
        <div ref="enrollChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { ensureChinaMap } from '@/utils/chinaMap'
import {
  getStatistics,
  getMapDistribution,
  getTypeRatio,
  getGrowthTrend,
  getProvinceTop10,
  getHotMajors,
  getEnrollTrend,
  DashboardStats,
  MapDataItem,
  TypeRatioItem,
  GrowthTrendData,
  ProvinceTop10Data,
  HotMajorItem,
  EnrollTrendData
} from '@/api/dashboard'

const router = useRouter()

// 统计指标
const stats = ref<DashboardStats>({
  totalUniversity: 2993,
  undergraduateCount: 1485,
  majorCount: 29,
  provinceCount: 33
})

// 图表 DOM 引用
const mapChartRef = ref<HTMLDivElement | null>(null)
const typeChartRef = ref<HTMLDivElement | null>(null)
const trendChartRef = ref<HTMLDivElement | null>(null)
const rankChartRef = ref<HTMLDivElement | null>(null)
const hotMajorChartRef = ref<HTMLDivElement | null>(null)
const enrollChartRef = ref<HTMLDivElement | null>(null)

// 图表实例列表
const chartInstances: echarts.ECharts[] = []

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

// 现代学术智能调色板
const colorPalette = ['#38bdf8', '#818cf8', '#34d399', '#fbbf24', '#f87171', '#c084fc', '#22d3ee', '#fb7185']

const baseChartStyle = {
  textStyle: { color: '#94a3b8' },
  grid: { top: 35, right: 20, bottom: 25, left: 45 },
  tooltip: {
    backgroundColor: 'rgba(13, 22, 41, 0.95)',
    borderColor: 'rgba(56, 189, 248, 0.25)',
    borderWidth: 1,
    textStyle: { color: '#f8fafc', fontSize: 12 },
    extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;'
  }
}

// 省份全称到简称映射表
const provinceNameMap: Record<string, string> = {
  '北京市': '北京',
  '天津市': '天津',
  '河北省': '河北',
  '山西省': '山西',
  '内蒙古自治区': '内蒙古',
  '辽宁省': '辽宁',
  '吉林省': '吉林',
  '黑龙江省': '黑龙江',
  '上海市': '上海',
  '江苏省': '江苏',
  '浙江省': '浙江',
  '安徽省': '安徽',
  '福建省': '福建',
  '江西省': '江西',
  '山东省': '山东',
  '河南省': '河南',
  '湖北省': '湖北',
  '湖南省': '湖南',
  '广东省': '广东',
  '广西壮族自治区': '广西',
  '海南省': '海南',
  '重庆市': '重庆',
  '四川省': '四川',
  '贵州省': '贵州',
  '云南省': '云南',
  '西藏自治区': '西藏',
  '陕西省': '陕西',
  '甘肃省': '甘肃',
  '青海省': '青海',
  '宁夏回族自治区': '宁夏',
  '新疆维吾尔自治区': '新疆',
  '台湾省': '台湾',
  '香港特别行政区': '香港',
  '澳门特别行政区': '澳门'
}

// 1. 地图初始化
const initMapChart = (data: MapDataItem[]) => {
  const chart = registerChart(mapChartRef.value)
  if (!chart) return

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(13, 22, 41, 0.95)',
      borderColor: 'rgba(56, 189, 248, 0.3)',
      textStyle: { color: '#f8fafc' },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;',
      formatter: (params: any) => {
        const val = params.value != null && !isNaN(params.value) ? params.value : '暂无数据'
        return `
          <div style="font-weight:600; color:#38bdf8; margin-bottom:4px; font-size:13px;">${params.name}</div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span style="color:#94a3b8;">高校数量:</span>
            <span style="color:#ffffff; font-weight:bold; font-family:'DIN Alternate',sans-serif; font-size:15px;">${val}</span> 所
          </div>
          <div style="font-size:11px; color:#64748b; margin-top:4px;">点击下钻查看省内高校名录 ↗</div>
        `
      }
    },
    visualMap: {
      min: 0,
      max: 170,
      left: 15,
      bottom: 15,
      itemWidth: 10,
      itemHeight: 75,
      text: ['高', '低'],
      calculable: true,
      inRange: { color: ['#0c1938', '#1e3a8a', '#0284c7', '#38bdf8'] },
      textStyle: { color: '#94a3b8', fontSize: 11 }
    },
    series: [{
      name: '高校分布',
      type: 'map',
      map: 'china',
      roam: true,
      nameMap: provinceNameMap,
      layoutCenter: ['50%', '50%'],
      layoutSize: '95%',
      zoom: 1.15,
      label: { show: true, color: '#94a3b8', fontSize: 10 },
      itemStyle: {
        areaColor: '#0e1d3d',
        borderColor: 'rgba(56, 189, 248, 0.3)',
        borderWidth: 0.8
      },
      emphasis: {
        label: { show: true, color: '#ffffff', fontWeight: 'bold' },
        itemStyle: {
          areaColor: '#3b82f6',
          shadowBlur: 14,
          shadowColor: 'rgba(59, 130, 246, 0.5)'
        }
      },
      data
    }]
  })

  // 地图省份点击下钻
  chart.off('click')
  chart.on('click', (params: any) => {
    if (params.name) {
      const prov = provinceNameMap[params.name] || params.name
      router.push({ path: '/university', query: { province: prov } })
    }
  })
}

// 2. 类型占比饼图
const initTypeChart = (data: TypeRatioItem[]) => {
  const chart = registerChart(typeChartRef.value)
  if (!chart) return

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(13, 22, 41, 0.95)',
      borderColor: 'rgba(56, 189, 248, 0.25)',
      textStyle: { color: '#f8fafc' },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;',
      formatter: '{b}: <span style="font-weight:bold;color:#38bdf8;">{c}</span> 所 ({d}%)'
    },
    legend: {
      bottom: '2%',
      textStyle: { color: '#94a3b8', fontSize: 11 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 12
    },
    color: colorPalette,
    series: [{
      name: '院校类型',
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#0d1629', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: {
          show: true,
          fontSize: 15,
          fontWeight: 'bold',
          color: '#ffffff',
          formatter: '{b}\n{d}%'
        }
      },
      data
    }]
  })
}

// 3. 趋势图表模式
const trendMode = ref<'decade' | 'tenYear'>('decade')
let cachedTrendData: GrowthTrendData | null = null

const switchTrendMode = (mode: 'decade' | 'tenYear') => {
  trendMode.value = mode
  if (cachedTrendData) {
    initTrendChart(cachedTrendData)
  }
}

// 3. 渲染历史演进/增长趋势图表
const initTrendChart = (data: GrowthTrendData) => {
  cachedTrendData = data
  const chart = registerChart(trendChartRef.value)
  if (!chart) return

  if (trendMode.value === 'decade') {
    const labels = data.decadeLabels && data.decadeLabels.length
      ? data.decadeLabels
      : ['1920以前', '1920-1949', '1950-1979', '1980-1999', '2000-2014', '2015至今']
    const counts = data.decadeCounts && data.decadeCounts.length
      ? data.decadeCounts
      : [197, 292, 1008, 452, 676, 368]
    const accum = data.decadeAccum && data.decadeAccum.length
      ? data.decadeAccum
      : [197, 489, 1497, 1949, 2625, 2993]

    chart.setOption({
      ...baseChartStyle,
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(13, 22, 41, 0.95)',
        borderColor: 'rgba(56, 189, 248, 0.3)',
        textStyle: { color: '#f8fafc' },
        extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;',
        formatter: (params: any) => {
          if (!Array.isArray(params)) return ''
          let tip = `<div style="font-weight:600;color:#38bdf8;margin-bottom:4px;">${params[0].name}</div>`
          params.forEach((p: any) => {
            tip += `<div>${p.marker} ${p.seriesName}: <span style="font-weight:bold;color:#fff;">${p.value}</span> 所</div>`
          })
          return tip
        }
      },
      legend: {
        data: ['时期新建', '历史累计'],
        textStyle: { color: '#94a3b8', fontSize: 11 },
        top: 4,
        right: 10,
        itemWidth: 10,
        itemHeight: 10
      },
      grid: { top: 32, right: 40, bottom: 25, left: 45 },
      xAxis: {
        type: 'category',
        data: labels,
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.12)' } },
        axisLabel: { color: '#94a3b8', fontSize: 10, interval: 0 }
      },
      yAxis: [
        {
          type: 'value',
          name: '新建',
          nameTextStyle: { color: '#94a3b8', fontSize: 10 },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
          axisLabel: { color: '#94a3b8', fontSize: 10 }
        },
        {
          type: 'value',
          name: '累计',
          nameTextStyle: { color: '#94a3b8', fontSize: 10 },
          splitLine: { show: false },
          axisLabel: { color: '#94a3b8', fontSize: 10 }
        }
      ],
      series: [
        {
          name: '时期新建',
          type: 'bar',
          data: counts,
          barWidth: '36%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#38bdf8' },
              { offset: 1, color: 'rgba(56, 189, 248, 0.1)' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        },
        {
          name: '历史累计',
          type: 'line',
          yAxisIndex: 1,
          data: accum,
          smooth: true,
          symbolSize: 6,
          itemStyle: { color: '#fbbf24' },
          lineStyle: { width: 2 }
        }
      ]
    })
  } else {
    const minVal = Math.floor(Math.min(...data.values) / 50) * 50 - 50
    chart.setOption({
      ...baseChartStyle,
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(13, 22, 41, 0.95)',
        borderColor: 'rgba(56, 189, 248, 0.25)',
        textStyle: { color: '#f8fafc' },
        extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;'
      },
      legend: { show: false },
      grid: { top: 25, right: 20, bottom: 25, left: 48 },
      xAxis: {
        type: 'category',
        data: data.years,
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.12)' } },
        axisLabel: { color: '#94a3b8', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        min: minVal,
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
        axisLabel: { color: '#94a3b8', fontSize: 11 }
      },
      series: [{
        name: '全国高校总量',
        data: data.values,
        type: 'line',
        smooth: true,
        symbolSize: 6,
        itemStyle: { color: '#38bdf8' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(56, 189, 248, 0.35)' },
            { offset: 1, color: 'rgba(56, 189, 248, 0.01)' }
          ])
        }
      }]
    })
  }
}

// 4. 各省高校 TOP10 横向柱状图
const initRankChart = (data: ProvinceTop10Data) => {
  const chart = registerChart(rankChartRef.value)
  if (!chart) return

  const pairs = data.provinces.map((p, i) => ({ province: p, value: data.values[i] }))
  pairs.sort((a, b) => b.value - a.value)
  const top10 = pairs.slice(0, 10)
  const sortedProvinces = top10.map(item => item.province).reverse()
  const sortedValues = top10.map(item => item.value).reverse()

  chart.setOption({
    ...baseChartStyle,
    grid: { top: 15, right: 35, bottom: 20, left: 55 },
    xAxis: { type: 'value', splitLine: { show: false } },
    yAxis: {
      type: 'category',
      data: sortedProvinces,
      axisLine: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [{
      type: 'bar',
      data: sortedValues,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#38bdf8' },
          { offset: 1, color: '#6366f1' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', color: '#f8fafc', fontSize: 11, fontFamily: 'DIN Alternate, monospace' }
    }]
  })
}

// 5. 热门专业玫瑰图 (TOP10)
const initHotMajorChart = (data: HotMajorItem[]) => {
  const chart = registerChart(hotMajorChartRef.value)
  if (!chart) return

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(13, 22, 41, 0.95)',
      borderColor: 'rgba(56, 189, 248, 0.25)',
      textStyle: { color: '#f8fafc' },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;'
    },
    color: colorPalette,
    series: [{
      name: '热门专业',
      type: 'pie',
      radius: [14, '70%'],
      center: ['50%', '50%'],
      roseType: 'area',
      itemStyle: { borderRadius: 4, borderColor: '#0d1629', borderWidth: 1 },
      label: { color: '#94a3b8', fontSize: 10 },
      data
    }]
  })
}

// 6. 近五年总招生走势
const initEnrollChart = (data: EnrollTrendData) => {
  const chart = registerChart(enrollChartRef.value)
  if (!chart) return

  chart.setOption({
    ...baseChartStyle,
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(13, 22, 41, 0.95)',
      borderColor: 'rgba(56, 189, 248, 0.25)',
      textStyle: { color: '#f8fafc' },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5); backdrop-filter: blur(12px); border-radius: 6px;'
    },
    legend: {
      data: ['本科生', '专科生'],
      textStyle: { color: '#94a3b8' },
      top: 5
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.years,
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.12)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '万人',
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [
      {
        name: '本科生',
        type: 'line',
        stack: 'Total',
        smooth: true,
        itemStyle: { color: '#38bdf8' },
        areaStyle: { color: 'rgba(56, 189, 248, 0.25)' },
        data: data.undergraduate
      },
      {
        name: '专科生',
        type: 'line',
        stack: 'Total',
        smooth: true,
        itemStyle: { color: '#818cf8' },
        areaStyle: { color: 'rgba(129, 140, 248, 0.2)' },
        data: data.juniorCollege
      }
    ]
  })
}

// 统一数据加载与图表启动
const loadAllData = async () => {
  await ensureChinaMap()

  // 1. 顶部指标
  try {
    const res = await getStatistics()
    if (res) stats.value = res
  } catch {}

  // 2. 地图数据
  let mapData: MapDataItem[] = [
    { name: '北京', value: 92 }, { name: '天津', value: 56 }, { name: '河北', value: 124 },
    { name: '山西', value: 82 }, { name: '内蒙古', value: 54 }, { name: '辽宁', value: 114 },
    { name: '吉林', value: 66 }, { name: '黑龙江', value: 78 }, { name: '上海', value: 64 },
    { name: '江苏', value: 167 }, { name: '浙江', value: 109 }, { name: '安徽', value: 121 },
    { name: '福建', value: 89 }, { name: '江西', value: 106 }, { name: '山东', value: 153 },
    { name: '河南', value: 156 }, { name: '湖北', value: 130 }, { name: '湖南', value: 130 },
    { name: '广东', value: 160 }, { name: '广西', value: 85 }, { name: '海南', value: 21 },
    { name: '重庆', value: 69 }, { name: '四川', value: 134 }, { name: '贵州', value: 75 },
    { name: '云南', value: 82 }, { name: '西藏', value: 7 }, { name: '陕西', value: 97 },
    { name: '甘肃', value: 49 }, { name: '青海', value: 12 }, { name: '宁夏', value: 20 },
    { name: '新疆', value: 60 }, { name: '台湾', value: 0 }, { name: '香港', value: 0 }, { name: '澳门', value: 0 }
  ]
  try {
    const res = await getMapDistribution()
    if (res && res.length) mapData = res
  } catch {}
  initMapChart(mapData)

  // 3. 高校类型
  let typeData: TypeRatioItem[] = [
    { value: 980, name: '理工类' }, { value: 760, name: '综合类' },
    { value: 340, name: '师范类' }, { value: 280, name: '财经类' },
    { value: 150, name: '医药类' }, { value: 562, name: '其他类' }
  ]
  try {
    const res = await getTypeRatio()
    if (res && res.length) typeData = res
  } catch {}
  initTypeChart(typeData)

  // 4. 增长趋势
  let trendData: GrowthTrendData = {
    years: ['2015','2016','2017','2018','2019','2020','2021','2022','2023','2024'],
    values: [2852, 2879, 2914, 2956, 2983, 3005, 3012, 3054, 3069, 3072]
  }
  try {
    const res = await getGrowthTrend()
    if (res && res.years) trendData = res
  } catch {}
  initTrendChart(trendData)

  // 5. 各省排行 TOP10
  let rankData: ProvinceTop10Data = {
    provinces: ['北京', '江苏', '广东', '山东', '河南', '四川', '湖北', '湖南', '浙江', '安徽'],
    values: [92, 167, 160, 153, 156, 134, 130, 130, 109, 121]
  }
  try {
    const res = await getProvinceTop10()
    if (res && res.provinces) rankData = res
  } catch {}
  initRankChart(rankData)

  // 6. 热门专业 (TOP10)
  let hotMajorData: HotMajorItem[] = [
    { value: 40, name: '计算机' }, { value: 38, name: '软件工程' },
    { value: 32, name: '人工智能' }, { value: 30, name: '临床医学' },
    { value: 28, name: '电子信息' }, { value: 26, name: '数据科学' },
    { value: 24, name: '自动化' }, { value: 22, name: '法学' },
    { value: 20, name: '微电子' }, { value: 18, name: '金融学' }
  ]
  try {
    const res = await getHotMajors()
    if (res && res.length) hotMajorData = res
  } catch {}
  initHotMajorChart(hotMajorData)

  // 7. 招生趋势
  let enrollData: EnrollTrendData = {
    years: ['2019', '2020', '2021', '2022', '2023'],
    undergraduate: [431, 443, 444, 467, 478],
    juniorCollege: [483, 524, 552, 538, 564]
  }
  try {
    const res = await getEnrollTrend()
    if (res && res.years) enrollData = res
  } catch {}
  initEnrollChart(enrollData)
}

// 窗口 Resize 自适应
const handleResize = () => {
  chartInstances.forEach(chart => chart.resize())
}

onMounted(() => {
  nextTick(() => {
    loadAllData()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(chart => chart.dispose())
  chartInstances.length = 0
})
</script>

<style scoped lang="scss">
.dashboard-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 顶部指标卡片 */
.stat-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.stat-chip {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 10px;
  font-weight: 500;
  letter-spacing: 0.2px;

  &.chip-cyan {
    background: rgba(56, 189, 248, 0.12);
    color: #38bdf8;
    border: 1px solid rgba(56, 189, 248, 0.3);
  }
  &.chip-blue {
    background: rgba(59, 130, 246, 0.12);
    color: #60a5fa;
    border: 1px solid rgba(59, 130, 246, 0.3);
  }
  &.chip-indigo {
    background: rgba(99, 102, 241, 0.12);
    color: #818cf8;
    border: 1px solid rgba(99, 102, 241, 0.3);
  }
  &.chip-amber {
    background: rgba(245, 158, 11, 0.12);
    color: #fbbf24;
    border: 1px solid rgba(245, 158, 11, 0.3);
  }
}

.stat-icon {
  &.icon-cyan {
    background: rgba(56, 189, 248, 0.12);
    color: #38bdf8;
    border-color: rgba(56, 189, 248, 0.3);
  }
  &.icon-blue {
    background: rgba(59, 130, 246, 0.12);
    color: #60a5fa;
    border-color: rgba(59, 130, 246, 0.3);
  }
  &.icon-indigo {
    background: rgba(99, 102, 241, 0.12);
    color: #818cf8;
    border-color: rgba(99, 102, 241, 0.3);
  }
  &.icon-amber {
    background: rgba(245, 158, 11, 0.12);
    color: #fbbf24;
    border-color: rgba(245, 158, 11, 0.3);
  }
}

.dashboard-middle {
  display: flex;
  gap: 12px;
  flex: 1.25;
  min-height: 340px;
}
.map-wrap {
  flex: 6.5;
  position: relative;
}
.type-wrap {
  flex: 3.5;
}

.dashboard-bottom {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 240px;
}
.bottom-chart-box {
  flex: 1;
}

.trend-switch-btns {
  display: flex;
  gap: 4px;
}

.switch-btn {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid rgba(255, 255, 255, 0.08);

  &:hover {
    color: #f8fafc;
    background: rgba(255, 255, 255, 0.1);
  }

  &.active {
    background: rgba(56, 189, 248, 0.18);
    color: #38bdf8;
    border-color: rgba(56, 189, 248, 0.4);
    font-weight: 600;
    box-shadow: 0 0 8px rgba(56, 189, 248, 0.2);
  }
}
</style>
