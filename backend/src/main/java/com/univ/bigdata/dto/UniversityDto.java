package com.univ.bigdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UniversityDto {
    private Long id;

    @NotBlank(message = "高校名称不能为空")
    private String schoolName;

    private String schoolCode;

    @NotBlank(message = "所在省份不能为空")
    private String province;

    @NotBlank(message = "所在城市不能为空")
    private String city;

    @NotBlank(message = "学校类型不能为空")
    private String schoolType;

    @NotBlank(message = "办学层次不能为空")
    private String schoolLevel;

    private Integer establishYear;

    private String introduction;
}
