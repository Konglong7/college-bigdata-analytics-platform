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
      title="数据仓库四层层次拓扑结构 (Star Schema / Hive / MySQL 8.0 规范建模)"
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
    univCount: '2,993 所',
    majorCount: '1,611 条',
    enrollCount: '20,951 条',
    storageSize: '18.6 MB (数仓 InnoDB)'
  },
  layers: [
    {
      name: 'ADS 应用数据层 (业务决策宽表)',
      class: 'dw-tag-dwd',
      tables: [
        'ads_univ_cockpit_view (驾驶舱聚合宽表 · 33 省级节点)',
        'ads_enroll_predict_matrix (招生预测矩阵 · 30 项推演)',
        'ads_major_employment_rank (专业就业分析 · 10 大门类)'
      ]
    },
    {
      name: 'DWD/DWS 事实层 (清洗规范事实)',
      class: 'dw-tag-fact',
      tables: [
        'fact_univ_detail (高校基础事实表 · 2,993 条)',
        'fact_enroll_record (招生调档事实表 · 20,951 条)',
        'fact_major_subject (专业学科设置表 · 1,611 条)',
        'fact_admission_score (录取分数事实表 · 覆盖 31 省市)'
      ]
    },
    {
      name: 'DIM 维度表层 (标准化分析维度)',
      class: 'dw-tag-dim',
      tables: [
        'dim_region (全国 34 省市地区维度表)',
        'dim_school_type (高校 6 大办学门类维度)',
        'dim_school_level (985/211/双一流层次维度)',
        'dim_major_cat (教育部 12 大学科门类维度)'
      ]
    },
    {
      name: 'ODS 原始湖仓日志层 (采集与清洗流水线)',
      class: 'dw-tag-dwd',
      tables: [
        'ods_spider_raw_log (采集调度监控日志 · 18 批次)',
        'ods_etl_quality_log (ETL 质量校验日志 · 13 批次)'
      ]
    }
  ],
  schemas: [
    {
      tableName: '高校核心事实表 (university · fact_univ_detail)',
      engine: '存储引擎: MySQL 8 InnoDB / 行数: 2,993',
      fields: [
        { name: 'id (高校唯一自增主键 - PK)', type: 'BIGINT' },
        { name: 'school_name (全国高校官方备案名称)', type: 'VARCHAR(128)' },
        { name: 'province / city (所在省份与城市)', type: 'VARCHAR(32)' },
        { name: 'school_type (办学类型: 理工/综合/师范等)', type: 'VARCHAR(32)' },
        { name: 'school_level (办学层次: 985/211/双一流/普通本科)', type: 'VARCHAR(32)' },
        { name: 'ruanke_rank / qs_rank (软科综合排名 / QS 国际排名)', type: 'INT' },
        { name: 'num_doctor / num_master (一级博士点 / 硕士点授权数)', type: 'INT' },
        { name: 'create_time (数仓 ETL 清洗入库时间戳)', type: 'DATETIME' }
      ]
    },
    {
      tableName: '历年招生与录取事实表 (enrollment · fact_enroll)',
      engine: '存储引擎: MySQL 8 InnoDB / 行数: 20,951',
      fields: [
        { name: 'id (招生投档记录唯一主键 - PK)', type: 'BIGINT' },
        { name: 'university_id (关联高校实体主键 - FK)', type: 'BIGINT' },
        { name: 'year (招生录取年份 · 2020-2024)', type: 'INT' },
        { name: 'province / subject_type (生源省份 / 科类)', type: 'VARCHAR(32)' },
        { name: 'plan_number / admission_number (计划招生 / 实际录取)', type: 'INT' },
        { name: 'score (调档最低录取投档线)', type: 'DECIMAL(5,2)' }
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
  } catch (e) {
    console.warn('Using baseline warehouse metadata', e)
  }
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
  padding: 6px 0;
}

.dw-layer {
  display: flex;
  align-items: center;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 8px 15px;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.dw-layer-title {
  width: 160px;
  font-weight: 600;
  font-size: 13px;
  color: var(--primary-light);
  border-right: 2px solid var(--border-color);
  padding-right: 12px;
  flex-shrink: 0;
}

.dw-layer-content {
  display: flex;
  flex: 1;
  gap: 10px;
  padding-left: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.dw-table-tag {
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid var(--border-color);
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-main);
}

.dw-tag-fact {
  border-color: rgba(239, 68, 68, 0.4);
  color: var(--danger);
  background: rgba(239, 68, 68, 0.08);
}

.dw-tag-dim {
  border-color: rgba(16, 185, 129, 0.4);
  color: var(--success);
  background: rgba(16, 185, 129, 0.08);
}

.dw-tag-dwd {
  border-color: rgba(245, 158, 11, 0.4);
  color: var(--warning);
  background: rgba(245, 158, 11, 0.08);
}

.schema-card {
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
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
  padding: 5px 8px;
  background: var(--table-stripe);
  border-radius: 4px;
  border-bottom: 1px solid var(--border-color-subtle);
  color: var(--text-main);

  span.type {
    color: var(--primary-light);
    font-family: var(--font-mono);
    font-size: 11.5px;
  }
}
</style>
