<template>
  <div class="recommend-page">
    <!-- 顶部条件与考分录入栏 -->
    <div class="dv-border-box recommend-filter-bar">
      <div class="corner-bottom-left"></div>
      <div class="corner-bottom-right"></div>

      <div class="filter-item">
        <span class="filter-label">📍 生源省份：</span>
        <select v-model="form.province" class="dv-select">
          <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
        </select>
      </div>

      <div class="filter-item score-item">
        <span class="filter-label">🎯 预估考分：</span>
        <input
          v-model.number="form.score"
          type="number"
          class="dv-input score-input"
          min="200"
          max="750"
          placeholder="如 635"
        />
        <div class="score-quick-tags">
          <span
            v-for="s in [660, 620, 580, 550, 510, 460]"
            :key="s"
            :class="['quick-score-btn', { active: form.score === s }]"
            @click="form.score = s; handleMatch()"
          >
            {{ s }}分
          </span>
        </div>
      </div>

      <div class="filter-item">
        <span class="filter-label">📖 科类：</span>
        <select v-model="form.subjectType" class="dv-select" @change="handleMatch">
          <option value="物理类">物理类 (新高考)</option>
          <option value="历史类">历史类 (新高考)</option>
          <option value="综合改革">综合改革</option>
          <option value="理科">传统理科</option>
          <option value="文科">传统文科</option>
        </select>
      </div>

      <div class="filter-item">
        <span class="filter-label">🏛️ 期望层次：</span>
        <select v-model="form.targetLevel" class="dv-select" @change="handleMatch">
          <option value="全部">全部层次</option>
          <option value="985">985工程</option>
          <option value="211">211工程</option>
          <option value="双一流">双一流建设</option>
          <option value="本科">普通本科</option>
        </select>
      </div>

      <div class="filter-item">
        <span class="filter-label">🗺️ 意向地区：</span>
        <select v-model="form.targetProvince" class="dv-select" @change="handleMatch">
          <option value="全部">全国范围</option>
          <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
        </select>
      </div>

      <button class="dv-btn btn-match" :disabled="loading" @click="handleMatch">
        {{ loading ? '计算推演中...' : '🚀 智能生成志愿方案' }}
      </button>
    </div>

    <!-- 方案概要指引条 -->
    <div class="summary-bar" v-if="result">
      <div class="summary-tag">
        <span>考生档案：</span>
        <strong>{{ result.userProvince }} · {{ result.subjectType }} · {{ result.userScore }} 分</strong>
      </div>
      <div class="summary-chips">
        <span class="chip chip-rush">冲刺梯度：{{ result.rushList.length }} 所</span>
        <span class="chip chip-steady">稳妥梯度：{{ result.steadyList.length }} 所</span>
        <span class="chip chip-safe">保底梯度：{{ result.safeList.length }} 所</span>
      </div>
      <div class="summary-tip">
        💡 推荐原则：按“冲2~3所、稳4~6所、保2~3所”标准梯次填报，规避滑档与高分低就风险。
      </div>
    </div>

    <!-- 冲·稳·保 三列看板泳道 -->
    <div class="lanes-container" v-if="result">
      <!-- 1. 冲刺梯队 (Rush) -->
      <div class="lane-col lane-rush">
        <div class="lane-header">
          <div class="lane-title">
            <span class="lane-badge badge-rush">冲</span>
            <h3>冲刺院校 (博一博)</h3>
          </div>
          <p class="lane-sub">往年线高出考分 1~18 分 · 录取胜率 28%~58%</p>
        </div>

        <div class="lane-scroll">
          <div
            v-for="item in result.rushList"
            :key="item.id"
            class="recommend-card card-rush"
          >
            <div class="rc-header">
              <div class="rc-title" @click="goToDetail(item.id)">
                {{ item.schoolName }}
                <span v-if="item.ruankeRank" class="rc-rank">软科#{{ item.ruankeRank }}</span>
              </div>
              <div class="prob-chip prob-rush">{{ item.probPercent }}% 胜率</div>
            </div>

            <!-- 胜率进度条 -->
            <div class="prob-bar-track">
              <div class="prob-bar-fill fill-rush" :style="{ width: item.probPercent + '%' }"></div>
            </div>

            <div class="rc-tags">
              <span v-for="t in item.tags" :key="t" class="rc-tag">{{ t }}</span>
            </div>

            <div class="rc-metrics">
              <div class="metric-box">
                <span class="m-lbl">预测调档线</span>
                <span class="m-val text-gold">{{ item.predictScore }} 分</span>
              </div>
              <div class="metric-box">
                <span class="m-lbl">考分分差</span>
                <span class="m-val text-diff">{{ item.scoreDiff > 0 ? '+' : '' }}{{ item.scoreDiff }} 分</span>
              </div>
              <div class="metric-box">
                <span class="m-lbl">校区所在地</span>
                <span class="m-val">{{ item.province }} · {{ item.city }}</span>
              </div>
            </div>

            <div class="rc-majors" v-if="item.topMajors && item.topMajors.length">
              <span class="majors-lbl">优势推荐专业：</span>
              <div class="majors-list">
                <span v-for="m in item.topMajors" :key="m" class="major-pill pill-rush">{{ m }}</span>
              </div>
            </div>

            <div class="rc-reason">
              {{ item.recommendReason }}
            </div>

            <div class="rc-actions">
              <button class="rc-btn btn-compare" @click="addToCompare(item.id)">+ 加入对比</button>
              <button class="rc-btn btn-detail" @click="goToDetail(item.id)">画像详情 ➔</button>
              <a :href="item.gaokaoSite" target="_blank" class="rc-btn btn-gaokao">掌上高考 ↗</a>
            </div>
          </div>

          <div v-if="!result.rushList.length" class="empty-lane">
            <div class="empty-icon">🔍</div>
            <p>当前分数或地区条件下未匹配到冲刺高校</p>
            <button class="btn-expand-scope" @click="form.targetProvince = '全部'; form.targetLevel = '全部'; handleMatch()">扩大至全国范围匹配</button>
          </div>
        </div>
      </div>

      <!-- 2. 稳妥梯队 (Steady) -->
      <div class="lane-col lane-steady">
        <div class="lane-header">
          <div class="lane-title">
            <span class="lane-badge badge-steady">稳</span>
            <h3>稳妥院校 (主攻首选)</h3>
          </div>
          <p class="lane-sub">考分处于黄金适中区间 · 录取胜率 68%~88%</p>
        </div>

        <div class="lane-scroll">
          <div
            v-for="item in result.steadyList"
            :key="item.id"
            class="recommend-card card-steady"
          >
            <div class="rc-header">
              <div class="rc-title" @click="goToDetail(item.id)">
                {{ item.schoolName }}
                <span v-if="item.ruankeRank" class="rc-rank">软科#{{ item.ruankeRank }}</span>
              </div>
              <div class="prob-chip prob-steady">{{ item.probPercent }}% 胜率</div>
            </div>

            <!-- 胜率进度条 -->
            <div class="prob-bar-track">
              <div class="prob-bar-fill fill-steady" :style="{ width: item.probPercent + '%' }"></div>
            </div>

            <div class="rc-tags">
              <span v-for="t in item.tags" :key="t" class="rc-tag tag-steady-style">{{ t }}</span>
            </div>

            <div class="rc-metrics">
              <div class="metric-box">
                <span class="m-lbl">预测调档线</span>
                <span class="m-val text-cyan">{{ item.predictScore }} 分</span>
              </div>
              <div class="metric-box">
                <span class="m-lbl">考分分差</span>
                <span class="m-val text-diff-steady">{{ item.scoreDiff > 0 ? '+' : '' }}{{ item.scoreDiff }} 分</span>
              </div>
              <div class="metric-box">
                <span class="m-lbl">校区所在地</span>
                <span class="m-val">{{ item.province }} · {{ item.city }}</span>
              </div>
            </div>

            <div class="rc-majors" v-if="item.topMajors && item.topMajors.length">
              <span class="majors-lbl">优势推荐专业：</span>
              <div class="majors-list">
                <span v-for="m in item.topMajors" :key="m" class="major-pill pill-steady">{{ m }}</span>
              </div>
            </div>

            <div class="rc-reason">
              {{ item.recommendReason }}
            </div>

            <div class="rc-actions">
              <button class="rc-btn btn-compare" @click="addToCompare(item.id)">+ 加入对比</button>
              <button class="rc-btn btn-detail" @click="goToDetail(item.id)">画像详情 ➔</button>
              <a :href="item.gaokaoSite" target="_blank" class="rc-btn btn-gaokao">掌上高考 ↗</a>
            </div>
          </div>

          <div v-if="!result.steadyList.length" class="empty-lane">
            <div class="empty-icon">🎯</div>
            <p>暂无完全匹配的稳妥高校</p>
            <button class="btn-expand-scope" @click="form.targetProvince = '全部'; form.targetLevel = '全部'; handleMatch()">扩大筛选范围</button>
          </div>
        </div>
      </div>

      <!-- 3. 保底梯队 (Safe) -->
      <div class="lane-col lane-safe">
        <div class="lane-header">
          <div class="lane-title">
            <span class="lane-badge badge-safe">保</span>
            <h3>保底院校 (托底防滑)</h3>
          </div>
          <p class="lane-sub">考分优势显著（超出 16 分以上） · 录取胜率 >90%</p>
        </div>

        <div class="lane-scroll">
          <div
            v-for="item in result.safeList"
            :key="item.id"
            class="recommend-card card-safe"
          >
            <div class="rc-header">
              <div class="rc-title" @click="goToDetail(item.id)">
                {{ item.schoolName }}
                <span v-if="item.ruankeRank" class="rc-rank">软科#{{ item.ruankeRank }}</span>
              </div>
              <div class="prob-chip prob-safe">{{ item.probPercent }}% 胜率</div>
            </div>

            <!-- 胜率进度条 -->
            <div class="prob-bar-track">
              <div class="prob-bar-fill fill-safe" :style="{ width: item.probPercent + '%' }"></div>
            </div>

            <div class="rc-tags">
              <span v-for="t in item.tags" :key="t" class="rc-tag tag-safe-style">{{ t }}</span>
            </div>

            <div class="rc-metrics">
              <div class="metric-box">
                <span class="m-lbl">预测调档线</span>
                <span class="m-val text-green">{{ item.predictScore }} 分</span>
              </div>
              <div class="metric-box">
                <span class="m-lbl">考分分差</span>
                <span class="m-val text-diff-safe">+{{ item.scoreDiff }} 分</span>
              </div>
              <div class="metric-box">
                <span class="m-lbl">校区所在地</span>
                <span class="m-val">{{ item.province }} · {{ item.city }}</span>
              </div>
            </div>

            <div class="rc-majors" v-if="item.topMajors && item.topMajors.length">
              <span class="majors-lbl">优势推荐专业：</span>
              <div class="majors-list">
                <span v-for="m in item.topMajors" :key="m" class="major-pill pill-safe">{{ m }}</span>
              </div>
            </div>

            <div class="rc-reason">
              {{ item.recommendReason }}
            </div>

            <div class="rc-actions">
              <button class="rc-btn btn-compare" @click="addToCompare(item.id)">+ 加入对比</button>
              <button class="rc-btn btn-detail" @click="goToDetail(item.id)">画像详情 ➔</button>
              <a :href="item.gaokaoSite" target="_blank" class="rc-btn btn-gaokao">掌上高考 ↗</a>
            </div>
          </div>

          <div v-if="!result.safeList.length" class="empty-lane">
            <div class="empty-icon">🛡️</div>
            <p>暂无匹配的保底高校</p>
            <button class="btn-expand-scope" @click="form.targetLevel = '全部'; handleMatch()">包容更多层次高校</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { matchVolunteers, RecommendQuery, RecommendResult } from '@/api/recommend'

const router = useRouter()

const provinces = [
  '北京', '江苏', '广东', '山东', '河南', '四川', '湖北', '湖南', '浙江', '安徽',
  '河北', '辽宁', '江西', '陕西', '福建', '广西', '云南', '山西', '黑龙江', '贵州',
  '重庆', '吉林', '上海', '新疆', '天津', '内蒙古', '甘肃', '海南', '宁夏', '青海', '西藏'
]

const form = reactive<RecommendQuery>({
  province: '湖南',
  score: 550,
  subjectType: '物理类',
  targetLevel: '全部',
  targetType: '全部',
  targetProvince: '湖南'
})

const loading = ref(false)
const result = ref<RecommendResult | null>(null)

const handleMatch = async () => {
  if (!form.score || form.score < 200 || form.score > 750) {
    ElMessage.warning('请输入合理的高考总分 (200 ~ 750分)')
    return
  }
  loading.value = true
  try {
    const res = await matchVolunteers(form)
    result.value = res
    ElMessage.success(`智能推演完成：已为您成功规划 ${res.rushList.length + res.steadyList.length + res.safeList.length} 所梯次目标高校`)
  } catch (err: any) {
    ElMessage.error(err?.message || '智能匹配计算失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/university/${id}`)
}

const addToCompare = (id: number) => {
  ElMessage.success('已添加至对比，正在跳转高校对比工作台...')
  router.push({ path: '/compare', query: { addId: id } })
}

onMounted(() => {
  handleMatch()
})
</script>

<style scoped lang="scss">
.recommend-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.recommend-filter-bar {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 10px 20px;
  height: 64px;
  flex-shrink: 0;
  flex-wrap: wrap;
  background: rgba(10, 29, 64, 0.7);
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-sub);
  font-size: 13px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-input {
  width: 90px;
  font-size: 15px;
  font-weight: bold;
  color: #00ffaa !important;
  text-align: center;
}

.score-quick-tags {
  display: flex;
  gap: 5px;
}

.quick-score-btn {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.08);
  color: #8ba2d4;
  cursor: pointer;
  transition: all 0.2s;
  &:hover, &.active {
    background: rgba(0, 229, 255, 0.25);
    color: #00e5ff;
    border: 1px solid #00e5ff;
  }
}

.dv-input, .dv-select {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 5px 10px;
  border-radius: 4px;
  outline: none;
  font-size: 12.5px;
  &:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 6px rgba(0, 229, 255, 0.5);
  }
}

.btn-match {
  background: linear-gradient(90deg, #1089ff 0%, #00e5ff 100%);
  color: #05122b;
  font-weight: bold;
  font-size: 13.5px;
  padding: 6px 18px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 0 12px rgba(0, 229, 255, 0.4);
  transition: all 0.3s;
  margin-left: auto;
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 0 18px rgba(0, 229, 255, 0.7);
  }
}

.summary-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: rgba(16, 42, 85, 0.6);
  border-left: 3px solid #00e5ff;
  font-size: 12.5px;
  flex-shrink: 0;
  border-radius: 0 4px 4px 0;
}

.summary-tag strong {
  color: #00ffaa;
}

.summary-chips {
  display: flex;
  gap: 8px;
}

.chip {
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11.5px;
  font-weight: bold;
}
.chip-rush { background: rgba(255, 77, 79, 0.2); color: #ff7875; border: 1px solid rgba(255, 77, 79, 0.4); }
.chip-steady { background: rgba(16, 137, 255, 0.2); color: #1089ff; border: 1px solid rgba(16, 137, 255, 0.4); }
.chip-safe { background: rgba(0, 255, 170, 0.2); color: #00ffaa; border: 1px solid rgba(0, 255, 170, 0.4); }

.summary-tip {
  margin-left: auto;
  color: #8ba2d4;
  font-size: 12px;
}

.lanes-container {
  flex: 1;
  display: flex;
  gap: 14px;
  min-height: 0;
}

.lane-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(6, 20, 48, 0.75);
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 6px;
  overflow: hidden;
}

.lane-header {
  padding: 10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.lane-title {
  display: flex;
  align-items: center;
  gap: 8px;
  h3 {
    margin: 0;
    font-size: 15px;
    color: #fff;
  }
}

.lane-badge {
  display: inline-block;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}
.badge-rush { background: #ff4d4f; color: #fff; }
.badge-steady { background: #1089ff; color: #fff; }
.badge-safe { background: #00ffaa; color: #05122b; }

.lane-sub {
  margin: 4px 0 0 0;
  font-size: 11.5px;
  color: #8ba2d4;
}

.lane-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recommend-card {
  background: rgba(13, 35, 74, 0.85);
  border: 1px solid rgba(0, 229, 255, 0.18);
  border-radius: 6px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: all 0.25s;
  &:hover {
    border-color: #00e5ff;
    box-shadow: 0 4px 14px rgba(0, 229, 255, 0.2);
    transform: translateY(-2px);
  }
}

.card-rush { border-left: 3px solid #ff4d4f; }
.card-steady { border-left: 3px solid #1089ff; }
.card-safe { border-left: 3px solid #00ffaa; }

.rc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rc-title {
  font-size: 15px;
  font-weight: bold;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  &:hover {
    color: #00e5ff;
  }
}

.rc-rank {
  font-size: 11px;
  color: #faad14;
  background: rgba(250, 173, 20, 0.15);
  padding: 1px 5px;
  border-radius: 2px;
}

.prob-chip {
  font-size: 11.5px;
  font-weight: bold;
  padding: 2px 7px;
  border-radius: 3px;
}
.prob-rush { background: rgba(255, 77, 79, 0.2); color: #ff7875; }
.prob-steady { background: rgba(16, 137, 255, 0.2); color: #00e5ff; }
.prob-safe { background: rgba(0, 255, 170, 0.2); color: #00ffaa; }

.rc-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.rc-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.08);
  color: #8ba2d4;
}

.rc-metrics {
  display: flex;
  background: rgba(0, 0, 0, 0.25);
  padding: 6px 10px;
  border-radius: 4px;
  justify-content: space-between;
}

.metric-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.m-lbl { font-size: 10.5px; color: #8ba2d4; }
.m-val { font-size: 12.5px; font-weight: bold; color: #fff; }
.text-gold { color: #faad14; }
.text-cyan { color: #00e5ff; }
.text-green { color: #00ffaa; }
.text-diff { color: #ff7875; }
.text-diff-steady { color: #00e5ff; }
.text-diff-safe { color: #00ffaa; }

.rc-majors {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.majors-lbl { font-size: 11px; color: #8ba2d4; }
.majors-list { display: flex; gap: 5px; flex-wrap: wrap; }
.major-pill {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  background: rgba(16, 137, 255, 0.15);
  color: #82c0ff;
}

.rc-reason {
  font-size: 11.5px;
  line-height: 1.4;
  color: #a0aec0;
  background: rgba(255, 255, 255, 0.03);
  padding: 5px 8px;
  border-radius: 4px;
}

.rc-actions {
  display: flex;
  gap: 6px;
  margin-top: 2px;
}

.rc-btn {
  flex: 1;
  padding: 4px 6px;
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  font-size: 11.5px;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  transition: all 0.2s;
  &:hover {
    background: rgba(0, 229, 255, 0.2);
    border-color: #00e5ff;
    color: #00e5ff;
  }
}

.btn-compare {
  background: rgba(16, 137, 255, 0.25);
  border-color: #1089ff;
  color: #82c0ff;
}

.btn-detail {
  background: rgba(0, 255, 170, 0.2);
  border-color: #00ffaa;
  color: #00ffaa;
}

.empty-lane {
  text-align: center;
  color: #8ba2d4;
  font-size: 12px;
  padding: 30px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.empty-icon {
  font-size: 28px;
  opacity: 0.8;
}

.btn-expand-scope {
  background: rgba(0, 229, 255, 0.15);
  border: 1px solid #00e5ff;
  color: #00e5ff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
  margin-top: 4px;
  transition: all 0.2s;
  &:hover {
    background: rgba(0, 229, 255, 0.3);
  }
}

.prob-bar-track {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  overflow: hidden;
  margin-top: -2px;
}

.prob-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.4s ease;
}
.fill-rush { background: linear-gradient(90deg, #ff7875, #ff4d4f); }
.fill-steady { background: linear-gradient(90deg, #1089ff, #00e5ff); }
.fill-safe { background: linear-gradient(90deg, #00ffaa, #52c41a); }

.pill-rush {
  background: rgba(255, 77, 79, 0.15);
  color: #ff9c6e;
}
</style>
