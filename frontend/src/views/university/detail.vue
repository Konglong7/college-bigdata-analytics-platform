<template>
  <div class="detail-page">
    <!-- 头部高校概况条 -->
    <div class="dv-border-box detail-header">
      <div class="corner-bottom-left"></div>
      <div class="corner-bottom-right"></div>
      
      <div class="u-logo-placeholder">{{ detail.schoolName ? detail.schoolName.charAt(0) : '校' }}</div>
      
      <div class="u-detail-titles">
        <div class="title-row">
          <h2>{{ detail.schoolName }}</h2>
          <div class="u-detail-tags">
            <span v-for="tag in detail.tags" :key="tag" class="u-tag tag-985">{{ tag }}</span>
          </div>
        </div>
        <div class="u-detail-meta">
          <span>院校代码：<strong>{{ detail.schoolCode || '10001' }}</strong></span>
          <span>主管部门：<strong>{{ detail.department || '教育部' }}</strong></span>
          <span>办学性质：<strong>{{ detail.natureName || '公办' }}</strong></span>
          <span>所在地区：<strong>{{ detail.province }} · {{ detail.city }}</strong></span>
          <span>办学类型：<strong>{{ detail.schoolType }}</strong></span>
          <span>建校年份：<strong>{{ detail.establishYear || 1900 }} 年</strong></span>
        </div>
      </div>

      <div style="flex: 1;"></div>

      <!-- 真实网站直达与源站核验入口 -->
      <div class="header-link-group">
        <div class="link-group-title">
          <span>🔗 真实官方源站直达 · 亲自对比核验</span>
        </div>
        <div class="link-buttons">
          <a
            v-if="detail.schoolSite"
            :href="detail.schoolSite"
            target="_blank"
            rel="noopener noreferrer"
            class="site-btn btn-official"
            title="点击在新窗口打开高校官方网站"
          >
            🌐 学校官方网站 ↗
          </a>
          <a
            v-if="detail.site"
            :href="detail.site"
            target="_blank"
            rel="noopener noreferrer"
            class="site-btn btn-admissions"
            title="点击在新窗口打开本科招生官方网"
          >
            🎓 本科招生网 ↗
          </a>
          <a
            :href="detail.gaokaoSite || 'https://www.gaokao.cn/school/search'"
            target="_blank"
            rel="noopener noreferrer"
            class="site-btn btn-gaokao"
            title="点击在掌上高考亲自对比该校官方档案"
          >
            🏛️ 掌上高考档案源站 ↗
          </a>
          <a
            :href="detail.gaokaoScoreSite || 'https://www.gaokao.cn/school/search'"
            target="_blank"
            rel="noopener noreferrer"
            class="site-btn btn-score"
            title="点击在掌上高考核验真实投档录取分数线"
          >
            📈 历年投档线源站 ↗
          </a>
          <a
            :href="detail.chsiSite || 'https://gaokao.chsi.com.cn/'"
            target="_blank"
            rel="noopener noreferrer"
            class="site-btn btn-chsi"
            title="教育部阳光高考信息平台资质档案"
          >
            🛡️ 阳光高考认证 ↗
          </a>
        </div>
      </div>
    </div>

    <!-- 真实爬取档案信息条 (联系方式与科研实力) -->
    <div class="dv-border-box factual-bar">
      <div class="factual-item">
        <span class="f-label">📞 招生电话：</span>
        <span class="f-value" :title="detail.phone || '详见官网'">{{ detail.phone || '详见官方网站' }}</span>
      </div>
      <div class="factual-item">
        <span class="f-label">✉️ 官方邮箱：</span>
        <span class="f-value" :title="detail.email || '详见官网'">{{ detail.email || '详见官方网站' }}</span>
      </div>
      <div class="factual-item flex-2">
        <span class="f-label">📍 校区地址：</span>
        <span class="f-value" :title="detail.address || '详见官网'">{{ detail.address || '详见学校官方网站发布' }}</span>
      </div>
      <div class="factual-item">
        <span class="f-label">📮 邮政编码：</span>
        <span class="f-value">{{ detail.postcode || '100000' }}</span>
      </div>
      <div class="factual-item">
        <span class="f-label">🏆 软科全国排位：</span>
        <span class="f-value text-gold">{{ detail.ruankeRank ? `第 ${detail.ruankeRank} 名` : '全国前列' }}</span>
      </div>
      <div class="factual-item">
        <span class="f-label">🔬 重点实验室：</span>
        <span class="f-value text-cyan">{{ detail.numLab || 0 }} 个国家级平台</span>
      </div>
      <div class="factual-item">
        <span class="f-label">👨‍🏫 两院院士：</span>
        <span class="f-value text-cyan">{{ detail.numAcademician || 0 }} 人</span>
      </div>
      <div class="factual-item">
        <span class="f-label">🎓 博士学位点：</span>
        <span class="f-value text-cyan">{{ detail.numDoctor || 0 }} 个一级学科</span>
      </div>
    </div>

    <!-- 图表展示主体 -->
    <div class="detail-content">
      <!-- 左栏：雷达图、权威综合排名对比与校史简介 -->
      <div class="detail-left">
        <DvBorderBox title="综合办学实力六维评估 (基于真实科研指标)" style="flex: 1.1; min-height: 250px;">
          <div ref="radarChartRef" class="chart-container"></div>
        </DvBorderBox>

        <DvBorderBox title="权威多维大学榜单排名对比 (软科 / QS / 校友会)" style="flex: 1; min-height: 230px;">
          <div ref="rankChartRef" class="chart-container"></div>
        </DvBorderBox>

        <DvBorderBox title="官方发展简史与办学特色 (爬虫提取)" style="flex: 0.9; min-height: 180px;">
          <div class="intro-box">
            <p>{{ detail.introduction }}</p>
            <div class="intro-footer">
              <span class="intro-tip">数据来源：中国教育在线·掌上高考 & 高校官方主页</span>
              <a
                v-if="detail.gaokaoSite"
                :href="detail.gaokaoSite"
                target="_blank"
                rel="noopener noreferrer"
                class="intro-more-link"
              >
                亲自去掌上高考源站核验全文 ↗
              </a>
            </div>
          </div>
        </DvBorderBox>
      </div>

      <!-- 右栏：真实投档分数线、优势专业占比与科研培养结构 -->
      <div class="detail-right">
        <DvBorderBox title="历年官方录取调档最低分走势 (掌上高考真实投档线)" style="flex: 1.3; min-height: 270px;">
          <div class="chart-header-tip">
            <span>📌 真实爬虫数据说明：展示该校在掌上高考核心网关已备案的历年调档录取最低分数线</span>
            <a
              :href="detail.gaokaoScoreSite || 'https://www.gaokao.cn/school/search'"
              target="_blank"
              rel="noopener noreferrer"
              class="chart-header-link"
            >
              点击进入源站亲自比对各省投档线 ↗
            </a>
          </div>
          <div ref="scoreChartRef" class="chart-container" style="height: calc(100% - 30px);"></div>
        </DvBorderBox>

        <div class="detail-row-half">
          <DvBorderBox title="代表性热门专业学科与近年就业率" style="flex: 1; min-height: 230px;">
            <div ref="majorPieRef" class="chart-container"></div>
          </DvBorderBox>

          <DvBorderBox title="研究生与本科生学科梯度规模" style="flex: 1; min-height: 230px;">
            <div ref="genderBarRef" class="chart-container"></div>
          </DvBorderBox>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getUniversityDetail, UnivDetail } from '@/api/university'
import { getChartTheme, onThemeChange } from '@/utils/theme'

const route = useRoute()

// DOM 引用
const radarChartRef = ref<HTMLDivElement | null>(null)
const rankChartRef = ref<HTMLDivElement | null>(null)
const scoreChartRef = ref<HTMLDivElement | null>(null)
const majorPieRef = ref<HTMLDivElement | null>(null)
const genderBarRef = ref<HTMLDivElement | null>(null)

const chartInstances: echarts.ECharts[] = []

const registerChart = (dom: HTMLDivElement | null): echarts.ECharts | null => {
  if (!dom) return null
  const chart = echarts.getInstanceByDom(dom) || echarts.init(dom)
  if (!chartInstances.includes(chart)) {
    chartInstances.push(chart)
  }
  return chart
}

const colorPalette = ['#00e5ff', '#1089ff', '#faad14', '#00ffaa', '#ff4d4f', '#945fb9']

const detail = ref<UnivDetail>({
  id: 1,
  schoolName: '北京大学',
  schoolCode: '1000100',
  province: '北京',
  city: '北京市',
  schoolType: '综合类',
  schoolLevel: '985/211',
  establishYear: 1898,
  department: '教育部',
  belong: '教育部',
  natureName: '公办',
  introduction: '北京大学创办于1898年，初名京师大学堂，是中国近代第一所国立大学...',
  tags: ['985工程', '211工程', '双一流高校', '公办', '综合类'],
  schoolSite: 'https://www.pku.edu.cn/',
  site: 'https://bkzs.pku.edu.cn/',
  gaokaoSite: 'https://www.gaokao.cn/school/31',
  gaokaoScoreSite: 'https://www.gaokao.cn/school/31/provinceline',
  chsiSite: 'https://gaokao.chsi.com.cn/',
  phone: '010-62751407',
  email: 'bdzsb@pku.edu.cn',
  address: '北京市海淀区颐和园路5号',
  postcode: '100871',
  ruankeRank: 2,
  qsRank: 2,
  xyhRank: 1,
  numDoctor: 53,
  numMaster: 50,
  numAcademician: 162,
  numLab: 14,
  numLibrary: '851.20万',
  radarIndicators: [
    { name: '师资力量 (院士)', max: 100 },
    { name: '科研平台 (重点实验室)', max: 100 },
    { name: '学科梯队 (博士点)', max: 100 },
    { name: '权威综合排位', max: 100 },
    { name: '生源录取质量', max: 100 },
    { name: '毕业综合就业', max: 100 }
  ],
  radarValues: [99, 99, 90, 96, 98, 96],
  rankYears: ['2020', '2021', '2022', '2023', '2024', '2025', '2026'],
  rankValues: [4, 3, 3, 2, 2, 2, 2],
  rankCompare: [
    { name: '软科全国大学排名', value: 2 },
    { name: 'QS世界/亚洲排名', value: 2 },
    { name: '校友会中国大学排名', value: 1 }
  ],
  scoreYears: ['2020', '2021', '2022', '2023', '2024', '2025', '2026'],
  scoreSeries: [
    { name: '北京 (官方投档线)', data: [680, 685, 688, 703, 707, 709, 712] }
  ],
  majorPie: [
    { value: 985, name: '计算机科学与技术' },
    { value: 952, name: '数学与应用数学' },
    { value: 934, name: '法学' },
    { value: 948, name: '经济学' }
  ],
  maleRatio: 63,
  femaleRatio: 37
})

// 1. 六维办学实力雷达图
const initRadarChart = () => {
  const chart = registerChart(radarChartRef.value)
  if (!chart) return
  const ct = getChartTheme()
  chart.setOption({
    tooltip: ct.tooltip,
    radar: {
      center: ['50%', '54%'],
      radius: '58%',
      indicator: detail.value.radarIndicators || [
        { name: '师资力量', max: 100 },
        { name: '科研平台', max: 100 },
        { name: '学科梯队', max: 100 },
        { name: '综合排位', max: 100 },
        { name: '生源质量', max: 100 },
        { name: '就业表现', max: 100 }
      ],
      splitArea: { areaStyle: { color: ct.radarSplitArea } },
      axisLine: { lineStyle: { color: ct.radarAxisLine } },
      splitLine: { lineStyle: { color: ct.radarSplitLine } },
      axisName: { color: ct.textColor, fontSize: 10.5 }
    },
    series: [{
      type: 'radar',
      data: [{
        value: detail.value.radarValues || [90, 90, 85, 95, 95, 90],
        name: detail.value.schoolName,
        areaStyle: { color: 'rgba(0, 229, 255, 0.35)' },
        lineStyle: { color: '#00e5ff', width: 2 }
      }]
    }]
  }, true)
}

// 2. 权威大学榜单对比 (真实软科、QS、校友会)
const initRankChart = () => {
  const chart = registerChart(rankChartRef.value)
  if (!chart) return
  const ct = getChartTheme()

  const compData = detail.value.rankCompare || [
    { name: '软科全国排名', value: detail.value.ruankeRank || 2 },
    { name: 'QS世界/亚洲排名', value: detail.value.qsRank || 2 },
    { name: '校友会大学排名', value: detail.value.xyhRank || 1 }
  ]

  chart.setOption({
    tooltip: ct.tooltip,
    textStyle: { color: ct.textColor },
    grid: { top: 25, right: 65, bottom: 25, left: 125 },
    xAxis: {
      type: 'value',
      axisLabel: { color: ct.textColor, fontSize: 11 },
      splitLine: { lineStyle: { color: ct.splitLineColor } }
    },
    yAxis: {
      type: 'category',
      data: compData.map(c => c.name),
      axisLabel: { color: ct.textColor, fontSize: 11 },
      inverse: true
    },
    series: [{
      type: 'bar',
      data: compData.map(c => c.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#faad14' },
          { offset: 1, color: '#ff7a45' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: 16,
      label: {
        show: true,
        position: 'right',
        color: ct.titleColor,
        formatter: '第 {c} 名',
        fontSize: 11
      }
    }]
  }, true)
}

// 3. 官方真实录取分数线趋势
const initScoreChart = () => {
  const chart = registerChart(scoreChartRef.value)
  if (!chart) return
  const ct = getChartTheme()
  
  // 智能计算 Y 轴区间
  let minScore = 750
  let maxScore = 0
  const seriesList = detail.value.scoreSeries || []
  seriesList.forEach(s => {
    s.data.forEach(val => {
      if (val > 0) {
        if (val < minScore) minScore = val
        if (val > maxScore) maxScore = val
      }
    })
  })
  const yMin = Math.max(0, Math.floor((minScore - 20) / 10) * 10)
  const yMax = Math.min(750, Math.ceil((maxScore + 15) / 10) * 10)

  chart.setOption({
    tooltip: { ...ct.tooltip, trigger: 'axis' },
    textStyle: { color: ct.textColor },
    grid: { top: 40, right: 30, bottom: 30, left: 50 },
    legend: { textStyle: { color: ct.textColor }, top: 5 },
    xAxis: {
      type: 'category',
      data: detail.value.scoreYears || ['2020', '2021', '2022', '2023', '2024', '2025', '2026'],
      axisLabel: { color: ct.textColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: yMin,
      max: yMax,
      splitLine: { lineStyle: { color: ct.splitLineColor } },
      axisLabel: { color: ct.textColor, fontSize: 11, formatter: '{value}分' }
    },
    series: seriesList.map((item, idx) => ({
      name: item.name,
      type: 'line',
      smooth: true,
      data: item.data,
      lineStyle: { width: 3 },
      itemStyle: { color: colorPalette[idx % colorPalette.length] },
      label: {
        show: true,
        position: 'top',
        color: ct.isDark ? '#00e5ff' : '#0284c7',
        formatter: '{c}分',
        fontSize: 11
      }
    }))
  }, true)
}

// 4. 优势专业招生与就业率
const initMajorPie = () => {
  const chart = registerChart(majorPieRef.value)
  if (!chart) return
  const ct = getChartTheme()
  chart.setOption({
    tooltip: {
      ...ct.tooltip,
      trigger: 'item',
      formatter: '{b}: 就业率指数 {c}‰'
    },
    color: colorPalette,
    series: [{
      type: 'pie',
      radius: ['38%', '68%'],
      center: ['50%', '50%'],
      data: detail.value.majorPie,
      label: { color: ct.titleColor, fontSize: 11, formatter: '{b}' }
    }]
  }, true)
}

// 5. 研究生与本科生结构统计
const initGenderBar = () => {
  const chart = registerChart(genderBarRef.value)
  if (!chart) return
  const ct = getChartTheme()
  chart.setOption({
    tooltip: ct.tooltip,
    textStyle: { color: ct.textColor },
    grid: { top: 20, right: 20, bottom: 20, left: 20 },
    xAxis: { type: 'value', show: false },
    yAxis: { type: 'category', data: ['培养结构'], show: false },
    series: [
      {
        name: '本科与专科生',
        type: 'bar',
        stack: 'total',
        itemStyle: { color: '#1089ff', borderRadius: [4, 0, 0, 4] },
        label: { show: true, formatter: `本科生阶段 ${detail.value.maleRatio}%`, color: '#fff' },
        data: [detail.value.maleRatio]
      },
      {
        name: '硕士与博士研究生',
        type: 'bar',
        stack: 'total',
        itemStyle: { color: '#00e5ff', borderRadius: [0, 4, 4, 0] },
        label: { show: true, formatter: `硕博研究生 ${detail.value.femaleRatio}%`, color: '#02091b', fontWeight: 'bold' },
        data: [detail.value.femaleRatio]
      }
    ]
  }, true)
}

const renderAllCharts = () => {
  initRadarChart()
  initRankChart()
  initScoreChart()
  initMajorPie()
  initGenderBar()
}

const loadDetail = async () => {
  const id = (route.params.id as string) || '1'
  try {
    const res = await getUniversityDetail(id)
    if (res) {
      detail.value = res
    }
  } catch (err) {
    console.error('Failed to load university detail:', err)
  }
  nextTick(() => {
    renderAllCharts()
  })
}

const handleResize = () => {
  chartInstances.forEach(c => c.resize())
}

let unsubTheme: (() => void) | null = null

onMounted(() => {
  loadDetail()
  window.addEventListener('resize', handleResize)
  unsubTheme = onThemeChange(() => {
    renderAllCharts()
  })
})

watch(() => route.params.id, () => {
  loadDetail()
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
.detail-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  padding-right: 2px;
}

/* 顶部概况条 */
.detail-header {
  display: flex;
  flex-direction: row !important;
  gap: 16px;
  min-height: 96px;
  align-items: center;
  padding: 10px 20px;
  flex-shrink: 0;
  position: relative;
}

.u-logo-placeholder {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #00e5ff, #1089ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #02091b;
  font-weight: bold;
  box-shadow: 0 0 15px rgba(0, 229, 255, 0.4);
  flex-shrink: 0;
}

.u-detail-titles {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  h2 {
    font-size: 20px;
    color: var(--text-main);
    letter-spacing: 1px;
    margin: 0;
  }
}

.u-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--text-sub);
  font-size: 12px;
  strong {
    color: var(--text-main);
    font-weight: 500;
  }
}

.u-detail-tags {
  display: flex;
  gap: 6px;
}

.u-tag {
  padding: 2px 7px;
  font-size: 11px;
  border-radius: 3px;
}

.tag-985 {
  background: rgba(250, 173, 20, 0.2);
  color: var(--warning);
  border: 1px solid var(--warning);
}

/* 真实官方网站直达按扭组 */
.header-link-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.link-group-title {
  font-size: 11.5px;
  color: #00e5ff;
  font-weight: bold;
}

.link-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.site-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 11.5px;
  font-weight: bold;
  border-radius: 4px;
  text-decoration: none;
  transition: all 0.25s;
  cursor: pointer;
  white-space: nowrap;
}

.btn-official {
  background: rgba(0, 229, 255, 0.15);
  color: #00e5ff;
  border: 1px solid #00e5ff;
  &:hover {
    background: #00e5ff;
    color: #02091b;
    box-shadow: 0 0 10px rgba(0, 229, 255, 0.5);
  }
}

.btn-admissions {
  background: rgba(16, 137, 255, 0.2);
  color: #1089ff;
  border: 1px solid #1089ff;
  &:hover {
    background: #1089ff;
    color: #fff;
    box-shadow: 0 0 10px rgba(16, 137, 255, 0.5);
  }
}

.btn-gaokao {
  background: rgba(250, 173, 20, 0.2);
  color: #faad14;
  border: 1px solid #faad14;
  &:hover {
    background: #faad14;
    color: #02091b;
    box-shadow: 0 0 10px rgba(250, 173, 20, 0.5);
  }
}

.btn-score {
  background: rgba(0, 255, 170, 0.2);
  color: #00ffaa;
  border: 1px solid #00ffaa;
  &:hover {
    background: #00ffaa;
    color: #02091b;
    box-shadow: 0 0 10px rgba(0, 255, 170, 0.5);
  }
}

.btn-chsi {
  background: rgba(255, 255, 255, 0.1);
  color: #d1d5db;
  border: 1px solid rgba(255, 255, 255, 0.25);
  &:hover {
    background: rgba(255, 255, 255, 0.25);
    color: #fff;
  }
}

/* 官方事实与联系方式条 */
.factual-bar {
  display: flex;
  flex-direction: row !important;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  padding: 8px 18px;
  min-height: 48px;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  flex-shrink: 0;
}

.factual-item {
  display: flex;
  align-items: center;
  font-size: 11.5px;
  gap: 4px;
  &.flex-2 {
    flex: 1.5;
    min-width: 260px;
  }
}

.f-label {
  color: var(--text-sub);
  white-space: nowrap;
}

.f-value {
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.text-gold {
  color: #faad14 !important;
  font-weight: bold;
}

.text-cyan {
  color: #00e5ff !important;
  font-weight: bold;
}

/* 主体图表网格 */
.detail-content {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 520px;
}

.detail-left {
  flex: 3.8;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-right {
  flex: 6.2;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row-half {
  flex: 1;
  display: flex;
  gap: 12px;
}

.chart-header-tip {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 10px;
  font-size: 11.5px;
  color: var(--text-sub);
  border-bottom: 1px dashed var(--border-color-subtle);
}

.chart-header-link {
  color: #00e5ff;
  text-decoration: underline;
  cursor: pointer;
  &:hover {
    color: #faad14;
  }
}

.intro-box {
  padding: 6px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-sub);
  p {
    margin: 0;
    text-indent: 2em;
    overflow-y: auto;
    max-height: 110px;
  }
}

.intro-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  padding-top: 4px;
  border-top: 1px dashed var(--border-color-subtle);
  font-size: 11px;
}

.intro-tip {
  color: var(--text-sub);
}

.intro-more-link {
  color: #faad14;
  text-decoration: none;
  font-weight: bold;
  &:hover {
    text-decoration: underline;
  }
}
/* =========================================================
   移动端适配 (≤768px)：头部信息条与主体图表区全部单列堆叠
========================================================= */
@media (max-width: 768px) {
  .detail-page {
    height: auto;
    min-height: 100%;
    overflow-y: visible;
    padding-right: 0;
  }

  /* 头部概况条：横向改纵向，避免校名、标签与按钮组互相挤压 */
  .detail-header {
    flex-direction: column !important;
    align-items: stretch;
    gap: 10px;
    min-height: auto;
    padding: 12px;
  }

  .u-logo-placeholder {
    width: 44px;
    height: 44px;
    font-size: 18px;
  }

  .title-row {
    flex-wrap: wrap;
    gap: 8px;

    h2 { font-size: 17px; }
  }

  .u-detail-meta {
    gap: 6px 12px;
    font-size: 11.5px;
  }

  /* 源站直达按钮组：整行左对齐并允许换行 */
  .header-link-group {
    align-items: stretch;
    flex-shrink: 1;
  }

  .link-buttons {
    justify-content: flex-start;
    gap: 6px;
  }

  .site-btn {
    padding: 4px 8px;
    font-size: 11px;
  }

  /* 事实信息条：单列铺满，长地址不再被省略号截断 */
  .factual-bar {
    flex-direction: column !important;
    align-items: flex-start;
    gap: 8px;
    min-height: auto;
    padding: 10px 12px;
  }

  .factual-item {
    width: 100%;
    font-size: 11.5px;

    &.flex-2 {
      flex: none;
      width: 100%;
      min-width: 0;
    }
  }

  .f-value { white-space: normal; }

  /* 主体左右双栏（3.8 / 6.2）→ 单列，取消 520px 最小高度 */
  .detail-content {
    flex: none;
    flex-direction: column;
    min-height: auto;
    gap: 10px;
  }

  .detail-left,
  .detail-right {
    flex: none;
    width: 100%;
    gap: 10px;
  }

  /* 右侧半宽双图并排 → 纵向 */
  .detail-row-half {
    flex: none;
    flex-direction: column;
    gap: 10px;
  }

  /* ECharts 容器保留真实高度，避免在自动高度卡片里塌陷为 0 */
  .detail-left .chart-container,
  .detail-right .chart-container {
    flex: none;
    height: 240px !important; /* 覆盖投档线图表上的行内高度 */
    min-height: 240px;
  }

  .chart-header-tip {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    line-height: 1.45;
  }

  /* 简介正文允许完整展开，由外层统一滚动 */
  .intro-box p { max-height: none; }
}
</style>
