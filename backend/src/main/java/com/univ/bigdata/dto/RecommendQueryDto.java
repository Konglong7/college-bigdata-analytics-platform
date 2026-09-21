package com.univ.bigdata.dto;

import lombok.Data;

@Data
public class RecommendQueryDto {
    /** 考生所在省份 (如: 河南, 广东, 四川, 北京) */
    private String province;

    /** 考生预估高考总分 (如: 645.0) */
    private Double score;

    /** 考生科类 (物理类 / 历史类 / 综合改革 / 理科 / 文科) */
    private String subjectType;

    /** 意向高校层次偏好 (全部 / 985 / 211 / 双一流 / 普通本科 / 专科) */
    private String targetLevel;

    /** 意向高校类型偏好 (全部 / 综合类 / 理工类 / 师范类 / 财经类 / 医药类) */
    private String targetType;

    /** 意向就读省份 (空表示全国) */
    private String targetProvince;
}
