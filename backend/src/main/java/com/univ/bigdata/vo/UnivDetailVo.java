package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnivDetailVo {
    // 基础信息
    private Long id;
    private String schoolName;
    private String schoolCode;
    private String province;
    private String city;
    private String schoolType;
    private String schoolLevel;
    private Integer establishYear;
    private String department;
    private String introduction;
    private List<String> tags;

    // 官方网站与源站核验直达链接
    private String schoolSite;
    private String site;
    private String gaokaoSite;
    private String gaokaoScoreSite;
    private String chsiSite;

    // 官方联系与办学资质事实
    private String belong;
    private String natureName;
    private String dualClassName;
    private String phone;
    private String email;
    private String address;
    private String postcode;

    // 权威排名与科研实力指标 (真实爬虫提取)
    private Integer ruankeRank;
    private Integer qsRank;
    private Integer xyhRank;
    private Integer numDoctor;
    private Integer numMaster;
    private Integer numAcademician;
    private String numLibrary;
    private Integer numLab;

    // 学科评估六维雷达图数据
    private List<Map<String, Object>> radarIndicators;
    private List<Integer> radarValues;

    // 权威榜单综合排名对比
    private List<String> rankYears;
    private List<Integer> rankValues;
    private List<Map<String, Object>> rankCompare;

    // 近五年录取分数线真实趋势
    private List<String> scoreYears;
    private List<Map<String, Object>> scoreSeries;

    // 优势专业招生与就业分析
    private List<Map<String, Object>> majorPie;

    // 研究生与本专科结构/科研师资规模
    private Integer maleRatio;
    private Integer femaleRatio;
}
