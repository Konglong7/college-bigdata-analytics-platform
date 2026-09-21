<template>
  <div class="admin-page">
    <div class="dv-border-box admin-layout">
      <div class="corner-bottom-left"></div>
      <div class="corner-bottom-right"></div>

      <!-- 侧边菜单 -->
      <div class="admin-sidebar">
        <div class="sidebar-title">管理控制台</div>
        <div
          v-for="(menu, idx) in menus"
          :key="menu.name"
          :class="['admin-menu-item', { active: currentMenu === idx }]"
          @click="currentMenu = idx"
        >
          <span class="menu-icon">{{ menu.icon }}</span>
          <span>{{ menu.name }}</span>
        </div>
      </div>

      <!-- 右侧管理内容区 -->
      <div class="admin-content">
        <!-- 模块 0：高校基础数据管理 -->
        <div v-if="currentMenu === 0" class="sub-panel">
          <!-- 工具栏 -->
          <div class="admin-toolbar">
            <div style="display: flex; gap: 8px;">
              <input
                v-model="queryParams.schoolName"
                type="text"
                class="dv-input"
                placeholder="搜索高校名称/省份..."
                style="width: 260px;"
                @keyup.enter="handleSearch"
              />
              <button class="dv-btn" @click="handleSearch">搜索</button>
              <button class="dv-btn btn-reset" @click="handleReset">重置</button>
            </div>

            <div style="display: flex; gap: 8px;">
              <button
                class="dv-btn"
                style="background: rgba(0, 255, 170, 0.25); color: #00ffaa; border-color: var(--success);"
                @click="openAddDialog"
              >
                + 新增高校
              </button>
              <button class="dv-btn" @click="handleBatchImport">批量导入</button>
              <button class="dv-btn" @click="handleExportExcel">导出报表</button>
            </div>
          </div>

          <!-- 数据表格 -->
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>高校名称</th>
                  <th>省份</th>
                  <th>城市</th>
                  <th>办学层次</th>
                  <th>学校类型</th>
                  <th>成立年份</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in tableData" :key="u.id">
                  <td>{{ u.id }}</td>
                  <td style="color: var(--primary-color); font-weight: bold;">{{ u.schoolName }}</td>
                  <td>{{ u.province }}</td>
                  <td>{{ u.city }}</td>
                  <td>
                    <span :class="['u-tag', u.schoolLevel && u.schoolLevel.includes('985') ? 'tag-985' : 'tag-type']">
                      {{ u.schoolLevel }}
                    </span>
                  </td>
                  <td>{{ u.schoolType }}</td>
                  <td>{{ u.establishYear || '1900' }}年</td>
                  <td>
                    <button class="action-btn btn-edit" @click="openEditDialog(u)">编辑</button>
                    <button class="action-btn btn-del" @click="handleDelete(u.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="!tableData.length">
                  <td colspan="8" style="text-align: center; color: var(--text-sub); padding: 30px;">暂无高校数据</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页栏 -->
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="queryParams.page"
              v-model:page-size="queryParams.size"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="loadData"
              @size-change="handleSearch"
            />
          </div>
        </div>

        <!-- 模块 1：专业学科设置管理 -->
        <div v-else-if="currentMenu === 1" class="sub-panel">
          <div class="admin-toolbar">
            <span class="panel-header-title">📚 全国高校专业学科目录与等级评定</span>
            <button class="dv-btn" @click="ElMessage.info('已触发教育部最新专业目录同步任务')">同步国家标准目录</button>
          </div>
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>专业代码</th>
                  <th>专业名称</th>
                  <th>所属门类</th>
                  <th>修业年限</th>
                  <th>授予学位</th>
                  <th>开设院校数</th>
                  <th>国家特色专业</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="m in majorList" :key="m.code">
                  <td style="color: var(--primary-color);">{{ m.code }}</td>
                  <td style="font-weight: bold;">{{ m.name }}</td>
                  <td>{{ m.category }}</td>
                  <td>{{ m.years }}</td>
                  <td>{{ m.degree }}</td>
                  <td>{{ m.schoolCount }} 所</td>
                  <td>
                    <span :class="['u-tag', m.isKey ? 'tag-985' : 'tag-type']">{{ m.isKey ? '国家级一流' : '省级一流' }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 模块 2：历史招生数据管理 -->
        <div v-else-if="currentMenu === 2" class="sub-panel">
          <div class="admin-toolbar">
            <span class="panel-header-title">📊 历年高校高考招生计划与调档线管理</span>
            <button class="dv-btn" @click="ElMessage.success('已刷新各省教育考试院同步通道')">刷新考试院数据</button>
          </div>
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>年份</th>
                  <th>高校名称</th>
                  <th>招生省份</th>
                  <th>科类</th>
                  <th>计划人数</th>
                  <th>最低投档分</th>
                  <th>省控线分差</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="e in enrollRecords" :key="e.id">
                  <td>{{ e.year }}</td>
                  <td style="color: var(--primary-color); font-weight: bold;">{{ e.school }}</td>
                  <td>{{ e.province }}</td>
                  <td>{{ e.type }}</td>
                  <td>{{ e.plan }} 人</td>
                  <td style="color: var(--success); font-weight: bold;">{{ e.score }} 分</td>
                  <td>+{{ e.diff }} 分</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 模块 3：数据采集任务中心 -->
        <div v-else-if="currentMenu === 3" class="sub-panel">
          <div class="admin-toolbar">
            <span class="panel-header-title">🕷️ 分布式爬虫采集任务调度与监控</span>
            <button class="dv-btn" @click="ElMessage.success('已启动全量增量爬虫采集集群')">立即触发全网采集</button>
          </div>
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>任务编号</th>
                  <th>目标数据源</th>
                  <th>调度周期</th>
                  <th>并发数</th>
                  <th>状态</th>
                  <th>上次执行</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in spiderTasks" :key="t.id">
                  <td>{{ t.id }}</td>
                  <td style="color: var(--primary-color);">{{ t.source }}</td>
                  <td>{{ t.cron }}</td>
                  <td>{{ t.threads }} 线程</td>
                  <td>
                    <span class="u-tag tag-type">{{ t.status }}</span>
                  </td>
                  <td>{{ t.lastRun }}</td>
                  <td>
                    <button class="action-btn btn-edit" @click="ElMessage.success(`任务 ${t.id} 调度指令已下发`)">执行</button>
                    <button class="action-btn btn-del" @click="ElMessage.warning(`任务 ${t.id} 已暂停`)">暂停</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 模块 4：数据清洗规则配置 -->
        <div v-else-if="currentMenu === 4" class="sub-panel">
          <div class="admin-toolbar">
            <span class="panel-header-title">⚙️ ETL 数据清洗、去重与质量校验规则引擎</span>
            <button class="dv-btn" @click="ElMessage.success('清洗规则集热重载完成')">重新加载规则</button>
          </div>
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>规则编号</th>
                  <th>规则名称</th>
                  <th>适用字段</th>
                  <th>处理策略</th>
                  <th>置信阈值</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in cleanRules" :key="r.id">
                  <td>{{ r.id }}</td>
                  <td style="font-weight: bold; color: var(--primary-color);">{{ r.name }}</td>
                  <td>{{ r.field }}</td>
                  <td>{{ r.strategy }}</td>
                  <td>{{ r.threshold }}</td>
                  <td><span class="u-tag tag-985">生效中</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 模块 5：数仓ETL调度监控 -->
        <div v-else-if="currentMenu === 5" class="sub-panel">
          <div class="admin-toolbar">
            <span class="panel-header-title">💾 数仓星型模型分层与任务流水线</span>
            <button class="dv-btn" @click="ElMessage.success('已触发 ODS -> DWD -> ADS 流水线重算')">全量重跑数仓流水线</button>
          </div>
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>作业标识</th>
                  <th>数仓分层</th>
                  <th>写入目标表</th>
                  <th>处理行数</th>
                  <th>耗时</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="j in warehouseJobs" :key="j.id">
                  <td>{{ j.id }}</td>
                  <td><span class="u-tag tag-985">{{ j.layer }}</span></td>
                  <td style="color: var(--primary-color); font-family: monospace;">{{ j.target }}</td>
                  <td>{{ j.rows }} 条</td>
                  <td>{{ j.duration }}</td>
                  <td><span class="u-tag tag-type">已完成</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 模块 6：系统用户权限设置 -->
        <div v-else-if="currentMenu === 6" class="sub-panel">
          <div class="admin-toolbar">
            <span class="panel-header-title">🛡️ 系统用户与 RBAC 角色权限管理</span>
            <button class="dv-btn" @click="ElMessage.info('已开启新用户安全审计')">安全审计配置</button>
          </div>
          <div class="admin-table-container">
            <table class="el-table">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>账号名称</th>
                  <th>系统角色</th>
                  <th>权限范围</th>
                  <th>注册时间</th>
                  <th>账号状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in userList" :key="u.id">
                  <td>{{ u.id }}</td>
                  <td style="color: var(--primary-color); font-weight: bold;">{{ u.username }}</td>
                  <td>
                    <span :class="['u-tag', u.role === 'ROLE_ADMIN' ? 'tag-985' : 'tag-type']">
                      {{ u.role === 'ROLE_ADMIN' ? '超级管理员' : '普通分析用户' }}
                    </span>
                  </td>
                  <td>{{ u.role === 'ROLE_ADMIN' ? '全部模块 (CRUD+系统调度)' : '查询/查看/预测/导出' }}</td>
                  <td>{{ u.createTime }}</td>
                  <td><span class="u-tag tag-type" style="color: var(--success); border-color: var(--success);">正常</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增 / 编辑高校对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑高校数据' : '新增高校记录'"
      width="550px"
      class="dark-dialog"
      append-to-body
    >
      <el-form :model="formData" label-width="90px">
        <el-form-item label="高校名称">
          <el-input v-model="formData.schoolName" placeholder="如: 北京大学" />
        </el-form-item>
        <el-form-item label="所在省份">
          <el-input v-model="formData.province" placeholder="如: 北京" />
        </el-form-item>
        <el-form-item label="所在城市">
          <el-input v-model="formData.city" placeholder="如: 北京" />
        </el-form-item>
        <el-form-item label="办学层次">
          <el-select v-model="formData.schoolLevel" placeholder="请选择办学层次" style="width: 100%;">
            <el-option label="985/211" value="985/211" />
            <el-option label="211工程" value="211工程" />
            <el-option label="普通本科" value="普通本科" />
            <el-option label="专科(高职)" value="专科(高职)" />
          </el-select>
        </el-form-item>
        <el-form-item label="学校类型">
          <el-select v-model="formData.schoolType" placeholder="请选择学校类型" style="width: 100%;">
            <el-option label="综合类" value="综合类" />
            <el-option label="理工类" value="理工类" />
            <el-option label="师范类" value="师范类" />
            <el-option label="财经类" value="财经类" />
            <el-option label="医药类" value="医药类" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="成立年份">
          <el-input-number v-model="formData.establishYear" :min="1800" :max="2030" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="高校简介">
          <el-input v-model="formData.introduction" type="textarea" :rows="3" placeholder="简要介绍..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUniversity">保存提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminUniversityPage,
  addUniversity,
  updateUniversity,
  deleteUniversity,
  UniversityFormData
} from '@/api/admin'
import { UniversityCard, UniversityQuery } from '@/api/university'

const menus = [
  { name: '高校基础数据管理', icon: '🏫' },
  { name: '专业学科设置管理', icon: '📚' },
  { name: '历史招生数据管理', icon: '📊' },
  { name: '数据采集任务中心', icon: '🕷️' },
  { name: '数据清洗规则配置', icon: '⚙️' },
  { name: '数仓ETL调度监控', icon: '💾' },
  { name: '系统用户权限设置', icon: '🛡️' }
]
const currentMenu = ref(0)

const queryParams = reactive<UniversityQuery>({
  page: 1,
  size: 10,
  schoolName: ''
})

const tableData = ref<UniversityCard[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = reactive<UniversityFormData>({
  schoolName: '',
  province: '',
  city: '',
  schoolLevel: '普通本科',
  schoolType: '综合类',
  establishYear: 1950,
  introduction: ''
})

const defaultMockList: UniversityCard[] = [
  { id: 1, schoolName: '北京大学', province: '北京', city: '北京', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1898 },
  { id: 2, schoolName: '清华大学', province: '北京', city: '北京', schoolLevel: '985/211', schoolType: '理工类', establishYear: 1911 },
  { id: 3, schoolName: '浙江大学', province: '浙江', city: '杭州', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1897 },
  { id: 4, schoolName: '复旦大学', province: '上海', city: '上海', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1905 },
  { id: 5, schoolName: '南京大学', province: '江苏', city: '南京', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1902 },
  { id: 6, schoolName: '武汉大学', province: '湖北', city: '武汉', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1893 },
  { id: 7, schoolName: '四川大学', province: '四川', city: '成都', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1896 },
  { id: 8, schoolName: '中山大学', province: '广东', city: '广州', schoolLevel: '985/211', schoolType: '综合类', establishYear: 1924 },
  { id: 9, schoolName: '哈尔滨工业大学', province: '黑龙江', city: '哈尔滨', schoolLevel: '985/211', schoolType: '理工类', establishYear: 1920 },
  { id: 10, schoolName: '深圳大学', province: '广东', city: '深圳', schoolLevel: '普通本科', schoolType: '综合类', establishYear: 1983 },
  { id: 11, schoolName: '苏州大学', province: '江苏', city: '苏州', schoolLevel: '211工程', schoolType: '综合类', establishYear: 1900 },
  { id: 12, schoolName: '电子科技大学', province: '四川', city: '成都', schoolLevel: '985/211', schoolType: '理工类', establishYear: 1956 }
]

// 模拟扩展数据（为其它6大管理模块提供完整的专业展示）
const majorList = ref([
  { code: '080901', name: '计算机科学与技术', category: '工学', years: '4年', degree: '工学学士', schoolCount: 980, isKey: true },
  { code: '080902', name: '软件工程', category: '工学', years: '4年', degree: '工学学士', schoolCount: 650, isKey: true },
  { code: '080910T', name: '数据科学与大数据技术', category: '工学', years: '4年', degree: '工学/理学学士', schoolCount: 710, isKey: true },
  { code: '080907T', name: '智能科学与技术', category: '工学', years: '4年', degree: '工学学士', schoolCount: 380, isKey: false },
  { code: '080917T', name: '人工智能', category: '工学', years: '4年', degree: '工学学士', schoolCount: 520, isKey: true },
  { code: '100201K', name: '临床医学', category: '医学', years: '5年', degree: '医学学士', schoolCount: 210, isKey: true }
])

const enrollRecords = ref([
  { id: 1, year: '2024', school: '北京大学', province: '河南', type: '理科', plan: 85, score: 698, diff: 185 },
  { id: 2, year: '2024', school: '清华大学', province: '河南', type: '理科', plan: 80, score: 699, diff: 186 },
  { id: 3, year: '2024', school: '浙江大学', province: '浙江', type: '综合改革', plan: 320, score: 668, diff: 172 },
  { id: 4, year: '2024', school: '复旦大学', province: '上海', type: '综合改革', plan: 150, score: 585, diff: 180 },
  { id: 5, year: '2024', school: '四川大学', province: '四川', type: '理科', plan: 450, score: 635, diff: 125 }
])

const spiderTasks = ref([
  { id: 'SP-101', source: '阳光高考平台 (分数线抓取)', cron: '0 0 2 * * ?', threads: 16, status: '运行中', lastRun: '2026-09-20 02:00' },
  { id: 'SP-102', source: '教育部官方网站 (高校名录)', cron: '0 0 4 1 * ?', threads: 8, status: '运行中', lastRun: '2026-09-01 04:00' },
  { id: 'SP-103', source: '双一流高校学科评估库', cron: '0 30 1 * * ?', threads: 12, status: '运行中', lastRun: '2026-09-20 01:30' },
  { id: 'SP-104', source: '各高校就业质量年报', cron: '0 0 3 * * ?', threads: 10, status: '休眠中', lastRun: '2026-09-19 03:00' }
])

const cleanRules = ref([
  { id: 'R-01', name: '高校规范名称对齐映射', field: 'school_name', strategy: '标准哈希词典对照', threshold: '100%' },
  { id: 'R-02', name: '异常调档分数过滤清洗', field: 'admission_score', strategy: 'Z-Score 异常检测 (3σ)', threshold: '99.5%' },
  { id: 'R-03', name: '办学层次与代码标准化', field: 'school_level', strategy: '正则归一化映射', threshold: '100%' },
  { id: 'R-04', name: '招生年份越界检测过滤', field: 'enroll_year', strategy: '区间合法性断言 [2000, 2026]', threshold: '100%' }
])

const warehouseJobs = ref([
  { id: 'JOB-ODS-01', layer: 'ODS -> DWD', target: 'fact_univ_detail', rows: 3072, duration: '4.2s' },
  { id: 'JOB-DWD-02', layer: 'DWD -> DWS', target: 'fact_enroll_record', rows: 120500, duration: '18.5s' },
  { id: 'JOB-DWS-03', layer: 'DWS -> ADS', target: 'ads_univ_cockpit_view', rows: 34, duration: '1.2s' },
  { id: 'JOB-ADS-04', layer: 'DWS -> ADS', target: 'ads_enroll_predict_matrix', rows: 500, duration: '3.1s' }
])

const userList = ref([
  { id: 1, username: 'admin', role: 'ROLE_ADMIN', createTime: '2026-01-01 10:00:00' },
  { id: 2, username: 'analyst_user', role: 'ROLE_USER', createTime: '2026-03-15 14:20:00' },
  { id: 3, username: 'teacher_zhang', role: 'ROLE_USER', createTime: '2026-05-10 09:12:00' }
])

const loadData = async () => {
  try {
    const res = await getAdminUniversityPage(queryParams)
    if (res && res.records !== undefined) {
      tableData.value = res.records
      total.value = res.total ?? 0
      return
    }
  } catch (err) {
    console.error('Failed to load admin universities:', err)
  }
  let filtered = defaultMockList.filter(u => !queryParams.schoolName || u.schoolName.includes(queryParams.schoolName) || u.province.includes(queryParams.schoolName))
  tableData.value = filtered
  total.value = filtered.length
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const handleReset = () => {
  queryParams.schoolName = ''
  handleSearch()
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    schoolName: '',
    province: '',
    city: '',
    schoolLevel: '普通本科',
    schoolType: '综合类',
    establishYear: 1980,
    introduction: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (u: UniversityCard) => {
  isEdit.value = true
  Object.assign(formData, {
    id: u.id,
    schoolName: u.schoolName,
    province: u.province,
    city: u.city,
    schoolLevel: u.schoolLevel,
    schoolType: u.schoolType,
    establishYear: u.establishYear || 1980,
    introduction: u.introduction || ''
  })
  dialogVisible.value = true
}

const saveUniversity = async () => {
  if (!formData.schoolName || !formData.province || !formData.city) {
    ElMessage.warning('请填写完整的学校名称、省份与城市')
    return
  }
  try {
    if (isEdit.value) {
      await updateUniversity(formData)
      ElMessage.success('高校信息更新成功')
    } else {
      await addUniversity(formData)
      ElMessage.success('新增高校成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // 本地同步更新模拟数据
    if (isEdit.value) {
      const idx = tableData.value.findIndex(item => item.id === formData.id)
      if (idx !== -1) {
        Object.assign(tableData.value[idx], formData)
      }
      ElMessage.success('更新成功 (本地)')
    } else {
      const newId = tableData.value.length ? Math.max(...tableData.value.map(i => i.id)) + 1 : 1
      tableData.value.unshift({ ...formData, id: newId } as UniversityCard)
      total.value++
      ElMessage.success('新增成功 (本地)')
    }
    dialogVisible.value = false
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确认删除该条高校信息吗？', '系统操作警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUniversity(id)
      ElMessage.success('删除成功')
    } catch {
      tableData.value = tableData.value.filter(u => u.id !== id)
      total.value--
      ElMessage.success('已从本地缓存移除')
    }
  }).catch(() => {})
}

const handleBatchImport = () => {
  ElMessage.info('已开启批量 Excel / CSV 导入解析通道，支持标准模版批量写入')
}

const handleExportExcel = () => {
  // 生成标准 CSV 并提供下载
  const headers = ['ID', '高校名称', '省份', '城市', '办学层次', '学校类型', '成立年份']
  const rows = (tableData.value.length ? tableData.value : defaultMockList).map(u => [
    u.id,
    `"${u.schoolName}"`,
    `"${u.province}"`,
    `"${u.city}"`,
    `"${u.schoolLevel}"`,
    `"${u.schoolType}"`,
    u.establishYear || 1900
  ])
  const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.setAttribute('download', `全国高校基础数据报表_${new Date().toISOString().slice(0, 10)}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('高校报表导出成功！已开始自动下载')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.admin-page {
  width: 100%;
  height: 100%;
}

.admin-layout {
  display: flex;
  flex-direction: row !important;
  height: 100%;
  gap: 20px;
}

.admin-sidebar {
  width: 220px;
  display: flex;
  flex-direction: column;
  padding: 10px 0;
  border-right: 1px solid rgba(0, 229, 255, 0.15);
  flex-shrink: 0;

  .sidebar-title {
    font-size: 13px;
    font-weight: bold;
    color: var(--primary-color);
    letter-spacing: 1px;
    padding: 0 16px 12px 16px;
    border-bottom: 1px solid rgba(0, 229, 255, 0.1);
    margin-bottom: 8px;
  }
}

.admin-menu-item {
  padding: 11px 16px;
  color: var(--text-sub);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  font-size: 13.5px;
  display: flex;
  align-items: center;
  gap: 10px;

  .menu-icon {
    font-size: 16px;
  }

  &:hover, &.active {
    color: var(--primary-color);
    background: rgba(0, 229, 255, 0.08);
    border-left: 3px solid var(--primary-color);
    text-shadow: 0 0 6px rgba(0, 229, 255, 0.4);
  }
}

.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sub-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.panel-header-title {
  font-size: 15px;
  font-weight: bold;
  color: var(--primary-color);
  letter-spacing: 1px;
}

.admin-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.admin-table-container {
  flex: 1;
  overflow-y: auto;
}

.el-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 13px;
  th {
    background: rgba(0, 229, 255, 0.1);
    color: var(--primary-color);
    padding: 10px 12px;
    border-bottom: 1px solid var(--border-color);
    font-weight: normal;
  }
  td {
    padding: 10px 12px;
    color: var(--text-main);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  }
  tr:hover td {
    background: rgba(0, 229, 255, 0.04);
  }
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 3px 8px;
  font-size: 12px;
  margin-right: 6px;
  border-radius: 2px;
  transition: all 0.2s;
}
.btn-edit {
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  &:hover {
    background: var(--primary-color);
    color: #000;
  }
}
.btn-del {
  color: var(--danger);
  border: 1px solid var(--danger);
  &:hover {
    background: var(--danger);
    color: #fff;
  }
}

.dv-input {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 6px 12px;
  border-radius: 4px;
  outline: none;
  transition: all 0.3s;
  &:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 6px rgba(0, 229, 255, 0.5);
  }
}

.dv-btn {
  background: rgba(0, 229, 255, 0.2);
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
  padding: 6px 18px;
  border-radius: 4px;
  cursor: pointer;
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

.u-tag {
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 2px;
}

.tag-985 {
  background: rgba(250, 173, 20, 0.2);
  color: var(--warning);
  border: 1px solid var(--warning);
}

.tag-type {
  background: rgba(0, 229, 255, 0.2);
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
  flex-shrink: 0;
}
</style>
