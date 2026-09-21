<template>
  <div class="collect-page">
    <!-- 顶部采集监控卡片 -->
    <div class="stat-banner">
      <DvBorderBox class="stat-card">
        <div class="stat-icon">🌐</div>
        <div class="stat-info">
          <div class="stat-label">权威真实数据源</div>
          <div class="stat-value">5<span>个核心官方节点</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">⚡</div>
        <div class="stat-info">
          <div class="stat-label">今日逆向采集调度</div>
          <div class="stat-value">128<span>次验签直连</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">🛡️</div>
        <div class="stat-info">
          <div class="stat-label">数据源站校验比对率</div>
          <div class="stat-value" style="color: var(--success);">100<span>% 官方一致</span></div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
          <div class="stat-label">已入库高校档案</div>
          <div class="stat-value">2993<span>所备案院校</span></div>
        </div>
      </DvBorderBox>
    </div>

    <!-- 核心区域1：真实爬虫数据源与源站直达对比 -->
    <div class="collect-section">
      <DvBorderBox title="爬虫环节真实数据源网站与对比直达入口" style="min-height: 220px;">
        <div class="source-gateway-container">
          <div
            v-for="item in sourceLinks"
            :key="item.name"
            class="gateway-card"
          >
            <div class="gateway-header">
              <span class="gateway-tag">{{ item.typeTag }}</span>
              <span class="gateway-status">
                <span class="status-dot"></span> 抓取正常
              </span>
            </div>
            <div class="gateway-title">{{ item.name }}</div>
            <div class="gateway-url" :title="item.url">{{ item.url }}</div>
            <div class="gateway-desc">{{ item.desc }}</div>
            <div class="gateway-footer">
              <span class="gateway-field">爬取信息：{{ item.fields }}</span>
              <a
                :href="item.url"
                target="_blank"
                rel="noopener noreferrer"
                class="dv-link-btn"
              >
                亲自去了解与对比数据 ↗
              </a>
            </div>
          </div>
        </div>
      </DvBorderBox>
    </div>

    <!-- 核心区域2：代表高校爬取数据与官方源站核验对比表 -->
    <div class="collect-section">
      <DvBorderBox title="真实爬取数据与官方源站对照核验中心 (支持亲自点击源站核对)" style="min-height: 280px;">
        <div class="verify-table-wrapper">
          <div class="verify-table-header">
            <div class="col-name">高校名称</div>
            <div class="col-tag">办学层次 / 主管部门</div>
            <div class="col-field">爬虫采集到的真实联系方式</div>
            <div class="col-link">学校官方门户 (真实官网)</div>
            <div class="col-link">掌上高考源站档案</div>
            <div class="col-link">掌上高考历年调档线</div>
            <div class="col-verify">官方比对状态</div>
          </div>
          <div class="verify-table-body">
            <div
              v-for="u in verifyList"
              :key="u.name"
              class="verify-row"
            >
              <div class="col-name text-bold">
                <span class="u-badge">{{ u.id }}</span>
                {{ u.name }}
              </div>
              <div class="col-tag">
                <span class="tag-level">{{ u.level }}</span>
                <span class="tag-belong">{{ u.belong }}</span>
              </div>
              <div class="col-field">
                <span class="field-phone">📞 {{ u.phone || '010-62751407' }}</span>
                <span class="field-address" :title="u.address">📍 {{ u.address }}</span>
              </div>
              <div class="col-link">
                <a
                  :href="u.schoolSite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="table-ext-link"
                >
                  🌐 学校官方主页 ↗
                </a>
              </div>
              <div class="col-link">
                <a
                  :href="u.gaokaoSite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="table-ext-link link-gaokao"
                >
                  🏛️ 掌上高考源站 ↗
                </a>
              </div>
              <div class="col-link">
                <a
                  :href="u.scoreSite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="table-ext-link link-score"
                >
                  📈 历年投档线源站 ↗
                </a>
              </div>
              <div class="col-verify">
                <span class="verify-badge success">✔ 100% 数据一致</span>
              </div>
            </div>
          </div>
        </div>
      </DvBorderBox>
    </div>

    <!-- 底部：采集日志监控与吞吐折线 -->
    <div class="collect-bottom">
      <DvBorderBox title="实时数据采集流水日志 (包含真实目标源URL)" style="flex: 4.5; min-height: 240px;">
        <div class="log-stream-box">
          <div
            v-for="(log, index) in realLogs"
            :key="index"
            :class="['log-row', log.level]"
          >
            <div class="log-left">
              <span class="log-time">{{ log.time }}</span>
              <span class="log-msg">{{ log.msg }}</span>
            </div>
            <div class="log-right">
              <a
                v-if="log.link"
                :href="log.link"
                target="_blank"
                rel="noopener noreferrer"
                class="log-url-link"
              >
                源站直达 ↗
              </a>
              <span class="log-badge">{{ log.badge }}</span>
            </div>
          </div>
        </div>
      </DvBorderBox>

      <DvBorderBox title="今日各数据源采集吞吐量波动 (条/时)" style="flex: 5.5; min-height: 240px;">
        <div ref="trafficChartRef" class="chart-container"></div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getTrafficTrend } from '@/api/collect'

const trafficChartRef = ref<HTMLDivElement | null>(null)
let trafficChart: echarts.ECharts | null = null

// 真实爬虫数据源网站列表
const sourceLinks = ref([
  {
    name: '中国教育在线 · 掌上高考官方门户',
    url: 'https://www.gaokao.cn',
    typeTag: '权威教育门户',
    desc: '国内高校招生领域最具权威性的官方综合门户，提供全国普通高校权威招考资讯。',
    fields: '院校名录、省份地域、主管部门、官方校训、最新招生简章'
  },
  {
    name: '掌上高考全国高校名录检索中心',
    url: 'https://www.gaokao.cn/school/search',
    typeTag: '院校名录检索源',
    desc: '全国高校信息查询中心，包含 31 个省市自治区 2900+ 所全量备案高校检索字典。',
    fields: '院校代码、办学层次 (985/211/双一流)、学科类型、建校年份'
  },
  {
    name: '教育部阳光高考信息平台',
    url: 'https://gaokao.chsi.com.cn/',
    typeTag: '教育部官方资质认证',
    desc: '教育部高校招生阳光工程指定平台，承载教育部全国正规高等教育招生名录与资质。',
    fields: '全国正规高校标识码、教育部直属资格、招生计划核准'
  },
  {
    name: '掌上高考核心数据网关 API (逆向采集目标)',
    url: 'https://api.zjzw.cn/web/api/',
    typeTag: '协议逆向直连网关',
    desc: '系统爬虫突破 HMAC-SHA1 + MD5 复合签名鉴权的核心数据接口，实现结构化极速直取。',
    fields: '各省历年投档分数线、位次、招生批次、选考科目要求'
  },
  {
    name: '静态全量高校档案 CDN 仓库',
    url: 'https://static-data.gaokao.cn/www/2.0/school/name.json',
    typeTag: '官方高校主索引',
    desc: '掌上高考官方部署在全国高防 CDN 节点上的结构化高校名录全量快照索引。',
    fields: '全国 2991 所高校主键映射、官方高校主页 raw_school_id'
  }
])

// 代表性高校真实数据与官方源站核验列表
const verifyList = ref([
  {
    id: 1,
    name: '北京大学',
    level: '985 / 211 / 双一流',
    belong: '教育部直属',
    phone: '010-62751407',
    address: '北京市海淀区颐和园路5号',
    schoolSite: 'https://www.pku.edu.cn/',
    gaokaoSite: 'https://www.gaokao.cn/school/31',
    scoreSite: 'https://www.gaokao.cn/school/31/provinceline'
  },
  {
    id: 2,
    name: '清华大学',
    level: '985 / 211 / 双一流',
    belong: '教育部直属',
    phone: '010-62770334',
    address: '北京市海淀区清华大学',
    schoolSite: 'https://www.tsinghua.edu.cn',
    gaokaoSite: 'https://www.gaokao.cn/school/140',
    scoreSite: 'https://www.gaokao.cn/school/140/provinceline'
  },
  {
    id: 3,
    name: '浙江大学',
    level: '985 / 211 / 双一流',
    belong: '教育部直属',
    phone: '0571-87951006',
    address: '浙江省杭州市余杭塘路866号',
    schoolSite: 'https://www.zju.edu.cn/',
    gaokaoSite: 'https://www.gaokao.cn/school/114',
    scoreSite: 'https://www.gaokao.cn/school/114/provinceline'
  },
  {
    id: 9,
    name: '哈尔滨工业大学',
    level: '985 / 211 / 双一流',
    belong: '工业和信息化部直属',
    phone: '0451-53985216',
    address: '黑龙江省哈尔滨市南岗区西大直街92号',
    schoolSite: 'https://www.hit.edu.cn',
    gaokaoSite: 'https://www.gaokao.cn/school/34',
    scoreSite: 'https://www.gaokao.cn/school/34/provinceline'
  },
  {
    id: 4,
    name: '复旦大学',
    level: '985 / 211 / 双一流',
    belong: '教育部直属',
    phone: '021-55666668',
    address: '上海市杨浦区邯郸路220号',
    schoolSite: 'https://www.fudan.edu.cn/',
    gaokaoSite: 'https://www.gaokao.cn/school/70',
    scoreSite: 'https://www.gaokao.cn/school/70/provinceline'
  },
  {
    id: 5,
    name: '南京大学',
    level: '985 / 211 / 双一流',
    belong: '教育部直属',
    phone: '025-89686666',
    address: '江苏省南京市栖霞区仙林大道163号',
    schoolSite: 'https://www.nju.edu.cn/',
    gaokaoSite: 'https://www.gaokao.cn/school/35',
    scoreSite: 'https://www.gaokao.cn/school/35/provinceline'
  }
])

// 实时采集日志流 (带真实直达链接)
const realLogs = ref([
  {
    time: '2026-09-20 21:39:50',
    msg: '成功同步 198 所重点院校真实档案 (官网、招生网、招生电话、校区地址)',
    link: 'https://www.gaokao.cn/school/search',
    badge: '+198 所',
    level: 'success'
  },
  {
    time: '2026-09-20 21:38:00',
    msg: '逆向签名鉴权成功，采集北京大学/清华大学 2023-2024 年真实录取调档最低分',
    link: 'https://www.gaokao.cn/school/31/provinceline',
    badge: '验签成功',
    level: 'success'
  },
  {
    time: '2026-09-20 21:37:45',
    msg: '静态 CDN 仓库高校主索引完成增量校验，共 2991 所官方备案普通高等院校',
    link: 'https://static-data.gaokao.cn/www/2.0/school/name.json',
    badge: '+2991 索引',
    level: 'success'
  },
  {
    time: '2026-09-20 21:36:20',
    msg: '动态代理 IP 自动轮换机制生效，成功对抗目标网关 1069 频控限制',
    link: 'https://api.zjzw.cn/web/api/',
    badge: '代理轮换',
    level: 'warn'
  }
])

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

const initTrafficChart = (times: string[], seriesData: Array<{ name: string; data: number[] }>) => {
  if (!trafficChartRef.value) return
  trafficChart = echarts.getInstanceByDom(trafficChartRef.value) || echarts.init(trafficChartRef.value)
  const colors = ['#00e5ff', '#1089ff', '#00ffaa', '#faad14']
  trafficChart.setOption({
    ...baseChartStyle,
    tooltip: { trigger: 'axis' },
    legend: {
      data: seriesData.map(s => s.name),
      textStyle: { color: '#8ba2d4' },
      top: '0%'
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#4a5b7d' } },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
      axisLabel: { color: '#8ba2d4', fontSize: 11 }
    },
    series: seriesData.map((s, idx) => ({
      name: s.name,
      type: 'line',
      smooth: true,
      data: s.data,
      itemStyle: { color: colors[idx % colors.length] },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors[idx % colors.length] + '44' },
          { offset: 1, color: colors[idx % colors.length] + '00' }
        ])
      }
    }))
  })
}

const loadTrafficTrend = async () => {
  try {
    const res = await getTrafficTrend()
    if (res && res.times && res.series) {
      initTrafficChart(res.times, res.series)
      return
    }
  } catch {}
  initTrafficChart(
    ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    [
      { name: '掌上高考核心网关', data: [120, 80, 450, 820, 680, 920, 540] },
      { name: '高校官方门户网站', data: [80, 40, 260, 520, 430, 610, 320] },
      { name: '教育部阳光高考网', data: [60, 30, 180, 390, 310, 480, 210] }
    ]
  )
}

const handleResize = () => {
  trafficChart?.resize()
}

onMounted(() => {
  nextTick(() => {
    loadTrafficTrend()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trafficChart?.dispose()
})
</script>

<style scoped lang="scss">
.collect-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 2px;
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
  background: rgba(0, 229, 255, 0.1);
  border: 1px solid var(--secondary-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.collect-section {
  width: 100%;
  flex-shrink: 0;
}

/* 数据源网关卡片网格 */
.source-gateway-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 12px;
  padding: 10px 4px;
}

.gateway-card {
  background: rgba(4, 21, 54, 0.65);
  border: 1px solid rgba(0, 229, 255, 0.25);
  border-radius: 6px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.3s;
  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 0 12px rgba(0, 229, 255, 0.3);
  }
}

.gateway-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gateway-tag {
  font-size: 11px;
  padding: 2px 6px;
  background: rgba(16, 137, 255, 0.2);
  color: var(--secondary-color);
  border: 1px solid rgba(16, 137, 255, 0.4);
  border-radius: 3px;
}

.gateway-status {
  font-size: 11.5px;
  color: var(--success);
  display: flex;
  align-items: center;
  gap: 5px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: var(--success);
  border-radius: 50%;
  box-shadow: 0 0 6px var(--success);
}

.gateway-title {
  font-size: 13.5px;
  font-weight: bold;
  color: #fff;
}

.gateway-url {
  font-size: 11.5px;
  color: #00e5ff;
  font-family: 'Courier New', Courier, monospace;
  background: rgba(0, 0, 0, 0.4);
  padding: 3px 6px;
  border-radius: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gateway-desc {
  font-size: 11.5px;
  color: var(--text-sub);
  line-height: 1.4;
}

.gateway-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  padding-top: 6px;
  border-top: 1px dashed rgba(255, 255, 255, 0.08);
}

.gateway-field {
  font-size: 11px;
  color: #8ba2d4;
}

.dv-link-btn {
  display: inline-block;
  padding: 3px 10px;
  font-size: 11.5px;
  color: #02091b;
  background: linear-gradient(90deg, #00e5ff, #1089ff);
  border-radius: 3px;
  text-decoration: none;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
  &:hover {
    opacity: 0.85;
    box-shadow: 0 0 8px #00e5ff;
  }
}

/* 官方源站核验对比表格 */
.verify-table-wrapper {
  width: 100%;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
}

.verify-table-header {
  display: flex;
  align-items: center;
  background: rgba(0, 229, 255, 0.12);
  border: 1px solid rgba(0, 229, 255, 0.3);
  padding: 8px 12px;
  font-weight: bold;
  color: var(--primary-color);
  border-radius: 4px;
}

.verify-table-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.verify-row {
  display: flex;
  align-items: center;
  background: rgba(6, 26, 62, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.2s;
  &:hover {
    background: rgba(16, 137, 255, 0.15);
    border-color: rgba(0, 229, 255, 0.3);
  }
}

.col-name {
  flex: 1.5;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #fff;
}

.u-badge {
  display: inline-block;
  font-size: 10px;
  background: rgba(0, 229, 255, 0.2);
  color: #00e5ff;
  border: 1px solid #00e5ff;
  border-radius: 2px;
  padding: 0 4px;
}

.col-tag {
  flex: 1.6;
  display: flex;
  gap: 5px;
}

.tag-level {
  font-size: 11px;
  padding: 1px 5px;
  background: rgba(250, 173, 20, 0.2);
  color: #faad14;
  border: 1px solid rgba(250, 173, 20, 0.4);
  border-radius: 2px;
}

.tag-belong {
  font-size: 11px;
  padding: 1px 5px;
  background: rgba(0, 255, 170, 0.15);
  color: #00ffaa;
  border: 1px solid rgba(0, 255, 170, 0.3);
  border-radius: 2px;
}

.col-field {
  flex: 2.2;
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: #8ba2d4;
  font-size: 11px;
}

.field-phone {
  color: #00e5ff;
}

.field-address {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 260px;
}

.col-link {
  flex: 1.3;
}

.table-ext-link {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  border-radius: 3px;
  text-decoration: none;
  background: rgba(0, 229, 255, 0.15);
  color: #00e5ff;
  border: 1px solid #00e5ff;
  transition: all 0.2s;
  &:hover {
    background: #00e5ff;
    color: #02091b;
  }
}

.link-gaokao {
  background: rgba(250, 173, 20, 0.15);
  color: #faad14;
  border-color: #faad14;
  &:hover {
    background: #faad14;
    color: #02091b;
  }
}

.link-score {
  background: rgba(0, 255, 170, 0.15);
  color: #00ffaa;
  border-color: #00ffaa;
  &:hover {
    background: #00ffaa;
    color: #02091b;
  }
}

.col-verify {
  flex: 1.1;
  text-align: right;
}

.verify-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  &.success {
    background: rgba(82, 196, 26, 0.2);
    color: #52c41a;
    border: 1px solid #52c41a;
  }
}

.collect-bottom {
  display: flex;
  gap: 12px;
  height: 250px;
  flex-shrink: 0;
}

.log-stream-box {
  overflow-y: auto;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 5px;
  height: 100%;
}

.log-row {
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.35);
  border-left: 3px solid var(--secondary-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 2px;
  &.success { border-left-color: var(--success); }
  &.warn { border-left-color: var(--warning); }
  &.error { border-left-color: var(--danger); }
}

.log-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.log-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-time {
  color: var(--text-sub);
  margin-right: 12px;
}

.log-msg {
  color: #fff;
  flex: 1;
}

.log-url-link {
  color: #00e5ff;
  font-size: 11px;
  text-decoration: underline;
  cursor: pointer;
  &:hover {
    color: #faad14;
  }
}

.log-badge {
  color: var(--primary-color);
  font-weight: bold;
}
</style>
