package com.univ.bigdata.dto;

import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class RecommendQueryDto {
    /** 考生所在省份 (如: 河南, 广东, 四川, 北京) */
    @NotBlank(message = "考生省份不能为空")
    private String province;

    /** 考生预估高考总分 (如: 645.0) */
    @NotNull(message = "预估分数不能为空")
    @DecimalMin(value = "0.0", message = "预估分数不能小于0")
    @DecimalMax(value = "900.0", message = "预估分数不能超过900")
    private Double score;

    /** 考生科类 (物理类 / 历史类 / 综合改革 / 理科 / 文科) */
    @NotBlank(message = "科类不能为空")
    private String subjectType;

    /** 意向高校层次偏好 (全部 / 985 / 211 / 双一流 / 普通本科 / 专科) */
    private String targetLevel;

    /** 意向高校类型偏好 (全部 / 综合类 / 理工类 / 师范类 / 财经类 / 医药类) */
    private String targetType;

    /** 意向就读省份 (空表示全国) */
    private String targetProvince;
}
