<template>
  <div class="query-page">
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

const defaultMockList: UniversityCard[] = [
  { id: 1, schoolName: '北京大学', province: '北京', city: '北京', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1898, department: '教育部' },
  { id: 2, schoolName: '清华大学', province: '北京', city: '北京', schoolLevel: '985/211', schoolType: '理工类', establishYear: 1911, department: '教育部' },
  { id: 3, schoolName: '浙江大学', province: '浙江', city: '杭州', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1897, department: '教育部' },
  { id: 4, schoolName: '复旦大学', province: '上海', city: '上海', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1905, department: '教育部' },
  { id: 5, schoolName: '南京大学', province: '江苏', city: '南京', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1902, department: '教育部' },
  { id: 6, schoolName: '武汉大学', province: '湖北', city: '武汉', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1893, department: '教育部' },
  { id: 7, schoolName: '四川大学', province: '四川', city: '成都', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1896, department: '教育部' },
  { id: 8, schoolName: '中山大学', province: '广东', city: '广州', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1924, department: '教育部' },
  { id: 9, schoolName: '哈尔滨工业大学', province: '黑龙江', city: '哈尔滨', schoolLevel: '985/211', schoolType: '理工类', establishYear: 1920, department: '工业和信息化部' },
  { id: 10, schoolName: '深圳大学', province: '广东', city: '深圳', schoolLevel: '普通本科', schoolType: '综合类', establishYear: 1983, department: '广东省教育厅' },
  { id: 11, schoolName: '苏州大学', province: '江苏', city: '苏州', schoolLevel: '211工程', schoolType: '综合类', establishYear: 1900, department: '江苏省教育厅' },
  { id: 12, schoolName: '电子科技大学', province: '四川', city: '成都', schoolLevel: '985/211', schoolType: '理工类', establishYear: 1956, department: '教育部' }
]

const loadData = async () => {
  try {
    const res = await pageUniversities(queryParams)
    if (res && res.records !== undefined) {
      univList.value = res.records
      total.value = res.total ?? 0
      return
    }
  } catch (err) {
    console.error('Failed to load universities:', err)
  }
  // 仅在后端接口通信彻底失败时使用本地假数据兜底
  let filtered = defaultMockList.filter(u => {
    let matchName = !queryParams.schoolName || u.schoolName.includes(queryParams.schoolName)
    let matchProv = !queryParams.province || u.province === queryParams.province
    let matchLevel = !queryParams.schoolLevel || (u.schoolLevel && u.schoolLevel.includes(queryParams.schoolLevel))
    let matchType = !queryParams.schoolType || u.schoolType === queryParams.schoolType
    return matchName && matchProv && matchLevel && matchType
  })
  univList.value = filtered
  total.value = filtered.length
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

.query-filter-bar {
  display: flex;
  gap: 18px;
  align-items: center;
  padding: 10px 20px;
  height: 60px;
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
    color: #fff;
  }
}

.query-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 6px 4px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  background: linear-gradient(135deg, rgba(17, 28, 53, 0.85) 0%, rgba(10, 16, 31, 0.75) 100%);
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
  min-height: 140px;
  box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.35), inset 0 1px 0 0 rgba(255, 255, 255, 0.08);

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px -4px rgba(0, 0, 0, 0.5), 0 0 16px rgba(56, 189, 248, 0.15);
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
  color: #f8fafc;
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
      color: #e2e8f0;
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
  border-top: 1px solid rgba(255, 255, 255, 0.06);
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
  color: #94a3b8;
  border: 1px solid rgba(148, 163, 184, 0.2);
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
</style>
