package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendSchoolVo {
    private Long id;
    private String schoolName;
    private String province;
    private String city;
    private String schoolLevel;
    private String schoolType;
    private Integer ruankeRank;

    /** 预估基准录取线 */
    private Double predictScore;

    /** 考生考分与预测录取线分差 (考生分 - 预测线) */
    private Double scoreDiff;

    /** 预估录取概率 (30% ~ 98%) */
    private Integer probPercent;

    /** 志愿梯队标签 (冲 / 稳 / 保) */
    private String tier;

    /** 智能推荐理由 */
    private String recommendReason;

    /** 王牌或特色优势专业 */
    private List<String> topMajors;

    /** 院校标签 */
    private List<String> tags;

    /** 掌上高考源站直达档案链接 */
    private String gaokaoSite;
}
