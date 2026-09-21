package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @GetMapping("/meta")
    public Result<Map<String, Object>> getWarehouseMeta() {
        return Result.success(Map.of(
                "metrics", Map.of(
                        "univCount", "3,072+ 所",
                        "majorCount", "80,000+ 条",
                        "enrollCount", "5,000,000+ 条",
                        "storageSize", "12.8 GB (Parquet)"
                ),
                "layers", List.of(
                        Map.of(
                                "name", "ADS 应用数据层",
                                "class", "dw-tag-dwd",
                                "tables", List.of(
                                        "ads_univ_cockpit_view (驾驶舱聚合宽表)",
                                        "ads_enroll_predict_matrix (招生预测矩阵)",
                                        "ads_major_employment_rank (专业就业分析)"
                                )
                        ),
                        Map.of(
                                "name", "DWD/DWS 事实层",
                                "class", "dw-tag-fact",
                                "tables", List.of(
                                        "fact_univ_detail (高校事实表)",
                                        "fact_enroll_record (招生事实表)",
                                        "fact_major_subject (专业事实表)",
                                        "fact_admission_score (录取分数事实表)"
                                )
                        ),
                        Map.of(
                                "name", "DIM 维度表层",
                                "class", "dw-tag-dim",
                                "tables", List.of(
                                        "dim_region (地区省市维度表)",
                                        "dim_date (时间日期维度表)",
                                        "dim_school (办学性质层次维度表)",
                                        "dim_major_cat (学科门类分类维度表)"
                                )
                        )
                ),
                "schemas", List.of(
                        Map.of(
                                "tableName", "高校核心事实表 (fact_univ_detail)",
                                "engine", "引擎: ClickHouse / 存储: 列式",
                                "fields", List.of(
                                        Map.of("name", "school_id (学校唯一编码 - PK)", "type", "BIGINT"),
                                        Map.of("name", "school_name (高校名称)", "type", "VARCHAR(128)"),
                                        Map.of("name", "province (所在省份代码)", "type", "VARCHAR(32)"),
                                        Map.of("name", "type (院校类型: 理工/综合/师范)", "type", "VARCHAR(32)"),
                                        Map.of("name", "level (办学层次: 985/211/双一流/本科)", "type", "VARCHAR(32)"),
                                        Map.of("name", "create_time (数仓同步清洗时间戳)", "type", "TIMESTAMP")
                                )
                        ),
                        Map.of(
                                "tableName", "专业维度与招生事实表 (dim_major & fact_enroll)",
                                "engine", "引擎: Hive / 建模: 关联星型",
                                "fields", List.of(
                                        Map.of("name", "major_id (专业标准代码 - PK)", "type", "BIGINT"),
                                        Map.of("name", "major_name (专业规范名称)", "type", "VARCHAR(64)"),
                                        Map.of("name", "category (所属门类: 工学/理学/医学等)", "type", "VARCHAR(32)"),
                                        Map.of("name", "enroll_year (招生年份 - 分区键 Part)", "type", "INT"),
                                        Map.of("name", "plan_count (投档计划招生人数)", "type", "INT"),
                                        Map.of("name", "min_score (调档最低录取分数线)", "type", "DECIMAL(5,2)")
                                )
                        )
                )
        ));
    }
}
