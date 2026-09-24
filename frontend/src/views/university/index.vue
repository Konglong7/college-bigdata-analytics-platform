<template>
  <div class="query-page">
    <div v-if="universityLoad.state.value === 'error'" class="data-status error-status">
      {{ universityLoad.errorMessage.value }}
      <button class="dv-btn" @click="loadData">重试</button>
    </div>
    <div v-else-if="universityLoad.state.value === 'empty'" class="data-status">
      当前筛选条件没有匹配的高校数据。
    </div>
    <!-- 顶部条件筛选栏 -->
    <div class="dv-border-box query-filter-bar">
      
      <div class="filter-item">
        <span>学校名称：</span>
        <input
          v-model="queryParams.schoolName"
          type="text"
          class="dv-input"
          placeholder="请输入高校名称"
          @keyup.enter="handleSearch"
        />
      </div>

      <div class="filter-item">
        <span>所在省份：</span>
        <select v-model="queryParams.province" class="dv-select" @change="handleSearch">
          <option value="">全部省份</option>
          <option v-for="prov in provinces" :key="prov" :value="prov">{{ prov }}</option>
        </select>
      </div>

      <div class="filter-item">
        <span>办学层次：</span>
        <select v-model="queryParams.schoolLevel" class="dv-select" @change="handleSearch">
          <option value="">全部层次</option>
          <option value="985">985/211工程</option>
          <option value="211">211工程</option>
          <option value="双一流">双一流建设</option>
          <option value="本科">普通本科</option>
          <option value="专科">专科(高职)</option>
        </select>
      </div>

      <div class="filter-item">
        <span>学校类型：</span>
        <select v-model="queryParams.schoolType" class="dv-select" @change="handleSearch">
          <option value="">全部类型</option>
          <option value="综合类">综合类</option>
          <option value="理工类">理工类</option>
          <option value="师范类">师范类</option>
          <option value="财经类">财经类</option>
          <option value="医药类">医药类</option>
        </select>
      </div>

      <button class="dv-btn" @click="handleSearch">检索数据</button>
      <button class="dv-btn btn-reset" @click="handleReset">重置条件</button>
      <button class="dv-btn btn-export" @click="handleExportList" title="导出当前筛选结果为 Excel / CSV 表格">📊 导出高校名单</button>
    </div>

    <!-- 高校卡片瀑布流列表 -->
    <div class="query-list-container">
      <div
        v-for="u in univList"
        :key="u.id"
        class="univ-card"
        @click="goToDetail(u.id)"
      >
        <div class="u-card-header">
          <div class="u-card-title" :title="u.schoolName">{{ u.schoolName }}</div>
          <div class="u-card-tags">
            <span :class="['u-tag', u.schoolLevel && u.schoolLevel.includes('985') ? 'tag-985' : 'tag-type']">
              {{ u.schoolLevel || '普通本科' }}
            </span>
            <span class="u-tag tag-type">{{ u.schoolType || '综合类' }}</span>
          </div>
        </div>
        <div class="u-card-info">
          <div class="info-row"><span class="info-label">所在地区</span><span class="info-value">{{ u.province }} · {{ u.city }}</span></div>
          <div class="info-row"><span class="info-label">成立年份</span><span class="info-value">{{ u.establishYear || 1900 }} 年</span></div>
          <div class="info-row"><span class="info-label">主管部门</span><span class="info-value" :title="u.department || '教育部 / 地方教育厅'">{{ u.department || '教育部 / 地方教育厅' }}</span></div>
        </div>
        <div class="u-card-actions" @click.stop>
          <a
            v-if="u.schoolSite"
            :href="u.schoolSite"
            target="_blank"
            rel="noopener noreferrer"
            class="card-link-btn btn-official-mini"
            title="点击打开学校官方网站"
          >
            官网 ↗
          </a>
          <a
            :href="u.gaokaoSite || 'https://www.gaokao.cn/school/search'"
            target="_blank"
            rel="noopener noreferrer"
            class="card-link-btn btn-gaokao-mini"
            title="点击打开掌上高考官方档案"
          >
            掌上高考 ↗
          </a>
          <span class="card-link-btn btn-compare-mini" @click="addToCompare(u.id)">
            + 对比
          </span>
          <span class="card-link-btn btn-detail-mini" @click="goToDetail(u.id)">
            画像详情 ➔
          </span>
        </div>
      </div>
      <div v-if="!univList.length" class="empty-tip">
        <div class="empty-icon">🏛️</div>
        <div class="empty-text">未找到匹配的高校信息</div>
        <div class="empty-sub">请尝试调整高校名称或重置筛选条件</div>
      </div>
    </div>

    <!-- 分页条 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[12, 16, 24, 32, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="handleSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageUniversities, UniversityCard, UniversityQuery } from '@/api/university'
import { createLoadState } from '@/utils/loadState'

const route = useRoute()
const router = useRouter()

const provinces = [
  '北京', '江苏', '广东', '山东', '河南', '四川', '湖北', '湖南', '浙江', '安徽',
  '河北', '辽宁', '江西', '陕西', '福建', '广西', '云南', '山西', '黑龙江', '贵州',
  '重庆', '吉林', '上海', '新疆', '天津', '内蒙古', '甘肃', '海南', '宁夏', '青海', '西藏'
]

const queryParams = reactive<UniversityQuery>({
  page: 1,
  size: 12,
  schoolName: '',
  province: '',
  schoolLevel: '',
  schoolType: ''
})

const univList = ref<UniversityCard[]>([])
const total = ref(0)
const universityLoad = createLoadState()

const loadData = async () => {
  universityLoad.start()
  try {
    const res = await pageUniversities(queryParams)
    if (res && res.records !== undefined) {
      univList.value = res.records
      total.value = res.total ?? 0
      universityLoad.succeed(univList.value.length === 0)
      return
    }
    universityLoad.succeed(true)
  } catch (err) {
    console.error('Failed to load universities:', err)
    univList.value = []
    total.value = 0
    universityLoad.fail('高校查询接口加载失败，请确认后端服务可用。')
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const handleReset = () => {
  queryParams.schoolName = ''
  queryParams.province = ''
  queryParams.schoolLevel = ''
  queryParams.schoolType = ''
  handleSearch()
}

const goToDetail = (id: number) => {
  router.push(`/university/${id}`)
}

const addToCompare = (id: number) => {
  ElMessage.success('已加入对比，正在跳转高校对比工作台...')
  router.push({ path: '/compare', query: { addId: id } })
}

// 导出当前筛选的高校列表为 Excel (CSV)
const handleExportList = () => {
  if (!univList.value.length) {
    ElMessage.warning('当前暂无高校数据可供导出')
    return
  }

  const rows: string[] = [
    ['高校编号', '高校名称', '所在省份', '城市', '办学层次', '办学类型', '成立年份', '主管部门', '官方网址', '掌上高考档案'].join(',')
  ]

  univList.value.forEach(u => {
    rows.push([
      u.id,
      `"${(u.schoolName || '').replace(/"/g, '""')}"`,
      u.province || '-',
      u.city || '-',
      u.schoolLevel || '普通本科',
      u.schoolType || '综合类',
      u.establishYear || '-',
      `"${(u.department || '地方教育厅').replace(/"/g, '""')}"`,
      u.schoolSite || '-',
      u.gaokaoSite || '-'
    ].join(','))
  })

  const csvContent = '\uFEFF' + rows.join('\r\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const provTag = queryParams.province ? `_${queryParams.province}` : ''
  const levelTag = queryParams.schoolLevel ? `_${queryParams.schoolLevel}` : ''
  a.download = `全国高校数据名录${provTag}${levelTag}_第${queryParams.page}页.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)

  ElMessage.success(`已成功导出 ${univList.value.length} 所高校名录 CSV 报表！`)
}

onMounted(() => {
  if (route.query.province) {
    queryParams.province = route.query.province as string
  }
  loadData()
})
</script>

<style scoped lang="scss">
.query-page {
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

.query-filter-bar {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 10px 18px;
  min-height: 54px;
  height: auto;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-sub);
  font-size: 13.5px;
}

.dv-input, .dv-select {
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 6px 12px;
  border-radius: 4px;
  outline: none;
  font-size: 13px;
  transition: all 0.3s;
  &:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 6px rgba(0, 229, 255, 0.5);
  }
}

.dv-select {
  cursor: pointer;
  option {
    background: #081836;
    color: #fff;
  }
}

.dv-btn {
  background: rgba(0, 229, 255, 0.2);
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  &:hover {
    background: var(--primary-color);
    color: #000;
  }
}

.btn-reset {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--text-sub);
  color: var(--text-sub);
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    color: var(--text-main);
  }
}

.btn-export {
  background: rgba(16, 185, 129, 0.12);
  border-color: rgba(16, 185, 129, 0.4);
  color: #10b981;
  &:hover {
    background: #10b981;
    color: #fff;
  }
}

.query-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 6px 4px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: minmax(185px, auto);
  gap: 16px;
  align-content: start;

  @media (max-width: 1560px) {
    grid-template-columns: repeat(3, 1fr);
  }
  @media (max-width: 1080px) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media (max-width: 680px) {
    grid-template-columns: 1fr;
  }
}

.univ-card {
  background: var(--bg-card-gradient);
  border: 1px solid var(--border-color);
  border-radius: var(--card-radius);
  padding: 16px 18px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 185px;
  box-shadow: var(--card-shadow);

  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--card-shadow-hover);
    border-color: var(--primary-light);
  }
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 3px;
    height: 100%;
    background: linear-gradient(180deg, #38bdf8, #6366f1);
    box-shadow: 0 0 8px rgba(56, 189, 248, 0.5);
  }
}

.u-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
}

.u-card-title {
  font-size: 15.5px;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  letter-spacing: 0.3px;
}

.u-card-tags {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  align-items: center;
}

.u-tag {
  padding: 2px 7px;
  font-size: 11px;
  border-radius: 4px;
  white-space: nowrap;
  font-weight: 500;
}

.tag-985 {
  background: rgba(245, 158, 11, 0.12);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.35);
}

.tag-type {
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
  border: 1px solid rgba(56, 189, 248, 0.25);
}

.u-card-info {
  font-size: 12.5px;
  color: var(--text-sub);
  display: flex;
  flex-direction: column;
  gap: 6px;

  .info-row {
    display: flex;
    align-items: center;
    line-height: 1.5;

    .info-label {
      color: var(--text-muted);
      margin-right: 12px;
      font-size: 12px;
      width: 56px;
      flex-shrink: 0;
    }

    .info-value {
      color: var(--text-main);
      font-size: 12.5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.u-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--border-color-subtle);
}

.card-link-btn {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-official-mini {
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
  border: 1px solid rgba(56, 189, 248, 0.3);
  &:hover {
    background: #38bdf8;
    color: #070c18;
  }
}

.btn-gaokao-mini {
  background: rgba(245, 158, 11, 0.1);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.3);
  &:hover {
    background: #fbbf24;
    color: #070c18;
  }
}

.btn-compare-mini {
  background: rgba(99, 102, 241, 0.12);
  color: #a5b4fc;
  border: 1px solid rgba(99, 102, 241, 0.3);
  &:hover {
    background: rgba(99, 102, 241, 0.3);
    border-color: #818cf8;
    color: #fff;
  }
}

.btn-detail-mini {
  margin-left: auto;
  color: var(--text-sub);
  border: 1px solid var(--border-color);
  &:hover {
    color: #ffffff;
    border-color: var(--primary-light);
    background: rgba(56, 189, 248, 0.12);
  }
}

.empty-tip {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: var(--text-sub);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.empty-text {
  font-size: 16px;
  color: var(--text-main);
  margin-bottom: 6px;
}

.empty-sub {
  font-size: 13px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 8px 10px;
  flex-shrink: 0;
}

html.light {
  .tag-985 {
    background: #fef3c7 !important;
    color: #b45309 !important;
    border-color: #fde68a !important;
  }
  .tag-type {
    background: #e0f2fe !important;
    color: #0284c7 !important;
    border-color: #bae6fd !important;
  }
  .btn-official-mini {
    background: #f0f9ff !important;
    color: #0284c7 !important;
    border-color: #bae6fd !important;
    &:hover {
      background: #0284c7 !important;
      color: #ffffff !important;
    }
  }
  .btn-gaokao-mini {
    background: #fffbeb !important;
    color: #b45309 !important;
    border-color: #fde68a !important;
    &:hover {
      background: #b45309 !important;
      color: #ffffff !important;
    }
  }
  .btn-compare-mini {
    background: #f5f3ff !important;
    color: #6366f1 !important;
    border-color: #ddd6fe !important;
    &:hover {
      background: #6366f1 !important;
      color: #ffffff !important;
    }
  }
  .btn-detail-mini {
    background: #f8fafc !important;
    color: #475569 !important;
    border-color: #cbd5e1 !important;
    &:hover {
      background: #e0f2fe !important;
      color: #0284c7 !important;
      border-color: #0284c7 !important;
    }
  }
  .btn-reset {
    background: #f1f5f9 !important;
    color: #475569 !important;
    border-color: #cbd5e1 !important;
    &:hover {
      background: #e2e8f0 !important;
      color: #0f172a !important;
    }
  }
  .btn-export {
    background: #ecfdf5 !important;
    color: #059669 !important;
    border-color: #a7f3d0 !important;
    &:hover {
      background: #059669 !important;
      color: #ffffff !important;
    }
  }
}
</style>
