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
public class RecommendResultVo {
    /** 考生所在省份 */
    private String userProvince;

    /** 考生预估总分 */
    private Double userScore;

    /** 科类 */
    private String subjectType;

    /** 冲刺梯队高校 (3~6 所) */
    private List<RecommendSchoolVo> rushList;

    /** 稳妥梯队高校 (5~8 所) */
    private List<RecommendSchoolVo> steadyList;

    /** 保底梯队高校 (3~6 所) */
    private List<RecommendSchoolVo> safeList;
}
