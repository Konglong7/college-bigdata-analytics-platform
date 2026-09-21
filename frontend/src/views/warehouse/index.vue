<template>
  <div class="warehouse-page">
    <!-- 顶部数据规模概览卡片 -->
    <div class="stat-banner">
      <DvBorderBox class="stat-card">
        <div class="stat-icon">🏫</div>
        <div class="stat-info">
          <div class="stat-label">高校全量事实记录</div>
          <div class="stat-value">{{ meta.metrics.univCount }}</div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">📖</div>
        <div class="stat-info">
          <div class="stat-label">专业维度覆盖数据</div>
          <div class="stat-value">{{ meta.metrics.majorCount }}</div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-label">历年招生行为事实库</div>
          <div class="stat-value">{{ meta.metrics.enrollCount }}</div>
        </div>
      </DvBorderBox>

      <DvBorderBox class="stat-card">
        <div class="stat-icon">💾</div>
        <div class="stat-info">
          <div class="stat-label">数仓分层存储总量</div>
          <div class="stat-value">{{ meta.metrics.storageSize }}</div>
        </div>
      </DvBorderBox>
    </div>

    <!-- 中部：数据仓库星型建模架构层级设计 -->
    <DvBorderBox
      title="数据仓库层次拓扑结构 (Star Schema / Hive / ClickHouse)"
      style="flex: 1.1; min-height: 220px;"
    >
      <div class="dw-arch-grid">
        <div v-for="layer in meta.layers" :key="layer.name" class="dw-layer">
          <div class="dw-layer-title">{{ layer.name }}</div>
          <div class="dw-layer-content">
            <span
              v-for="tbl in layer.tables"
              :key="tbl"
              :class="['dw-table-tag', layer.class]"
            >
              {{ tbl }}
            </span>
          </div>
        </div>
      </div>
    </DvBorderBox>

    <!-- 底部：数据实体表与字段Schema结构展示 -->
    <div style="flex: 1.1; display: flex; gap: 12px; min-height: 220px;">
      <DvBorderBox
        v-for="schema in meta.schemas"
        :key="schema.tableName"
        :title="schema.tableName"
        style="flex: 1;"
      >
        <template #extra>
          <span style="font-size: 12px; color: var(--text-sub);">{{ schema.engine }}</span>
        </template>
        <div class="schema-card" style="height: 100%;">
          <div class="schema-field-list">
            <div
              v-for="field in schema.fields"
              :key="field.name"
              class="schema-field-item"
            >
              <span>{{ field.name }}</span>
              <span class="type">{{ field.type }}</span>
            </div>
          </div>
        </div>
      </DvBorderBox>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DvBorderBox from '@/components/DvBorderBox/index.vue'
import { getWarehouseMeta, WarehouseMetaData } from '@/api/warehouse'

const meta = ref<WarehouseMetaData>({
  metrics: {
    univCount: '3,072+ 所',
    majorCount: '80,000+ 条',
    enrollCount: '5,000,000+ 条',
    storageSize: '12.8 GB (Parquet)'
  },
  layers: [
    {
      name: 'ADS 应用数据层',
      class: 'dw-tag-dwd',
      tables: [
        'ads_univ_cockpit_view (驾驶舱聚合宽表)',
        'ads_enroll_predict_matrix (招生预测矩阵)',
        'ads_major_employment_rank (专业就业分析)'
      ]
    },
    {
      name: 'DWD/DWS 事实层',
      class: 'dw-tag-fact',
      tables: [
        'fact_univ_detail (高校事实表)',
        'fact_enroll_record (招生事实表)',
        'fact_major_subject (专业事实表)',
        'fact_admission_score (录取分数事实表)'
      ]
    },
    {
      name: 'DIM 维度表层',
      class: 'dw-tag-dim',
      tables: [
        'dim_region (地区省市维度表)',
        'dim_date (时间日期维度表)',
        'dim_school (办学性质层次维度表)',
        'dim_major_cat (学科门类分类维度表)'
      ]
    }
  ],
  schemas: [
    {
      tableName: '高校核心事实表 (fact_univ_detail)',
      engine: '引擎: ClickHouse / 列式存储',
      fields: [
        { name: 'school_id (学校唯一编码 - PK)', type: 'BIGINT' },
        { name: 'school_name (高校名称)', type: 'VARCHAR(128)' },
        { name: 'province (所在省份代码)', type: 'VARCHAR(32)' },
        { name: 'type (院校类型: 理工/综合/师范)', type: 'VARCHAR(32)' },
        { name: 'level (办学层次: 985/211/双一流/本科)', type: 'VARCHAR(32)' },
        { name: 'create_time (数仓同步清洗时间戳)', type: 'TIMESTAMP' }
      ]
    },
    {
      tableName: '专业维度与招生事实表 (dim_major & fact_enroll)',
      engine: '引擎: Hive / 关联星型',
      fields: [
        { name: 'major_id (专业标准代码 - PK)', type: 'BIGINT' },
        { name: 'major_name (专业规范名称)', type: 'VARCHAR(64)' },
        { name: 'category (所属门类: 工学/理学/医学等)', type: 'VARCHAR(32)' },
        { name: 'enroll_year (招生年份 - 分区键 Part)', type: 'INT' },
        { name: 'plan_count (投档计划招生人数)', type: 'INT' },
        { name: 'min_score (调档最低录取分数线)', type: 'DECIMAL(5,2)' }
      ]
    }
  ]
})

const loadData = async () => {
  try {
    const res = await getWarehouseMeta()
    if (res && res.metrics) {
      meta.value = res
    }
  } catch {}
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.warehouse-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dw-arch-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  justify-content: space-around;
  padding: 8px 0;
}

.dw-layer {
  display: flex;
  align-items: center;
  background: rgba(10, 30, 60, 0.4);
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 4px;
  padding: 8px 15px;
}

.dw-layer-title {
  width: 140px;
  font-weight: bold;
  font-size: 13px;
  color: var(--primary-color);
  border-right: 2px solid rgba(0, 229, 255, 0.3);
  padding-right: 10px;
}

.dw-layer-content {
  display: flex;
  flex: 1;
  gap: 12px;
  padding-left: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.dw-table-tag {
  background: rgba(0, 229, 255, 0.1);
  border: 1px solid var(--border-color);
  padding: 4px 10px;
  border-radius: 3px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dw-tag-fact {
  border-color: #ff4d4f;
  color: #ff7875;
  background: rgba(255, 77, 79, 0.1);
}

.dw-tag-dim {
  border-color: #52c41a;
  color: #95de64;
  background: rgba(82, 196, 26, 0.1);
}

.dw-tag-dwd {
  border-color: #faad14;
  color: #ffe58f;
  background: rgba(250, 173, 20, 0.1);
}

.schema-card {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 10px;
  overflow-y: auto;
}

.schema-field-list {
  font-size: 12px;
  color: var(--text-sub);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.schema-field-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 6px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 2px;
  span.type {
    color: #8ba2d4;
    font-family: monospace;
  }
}
</style>
