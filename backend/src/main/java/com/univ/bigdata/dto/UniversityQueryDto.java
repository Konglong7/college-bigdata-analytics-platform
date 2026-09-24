package com.univ.bigdata.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
public class UniversityQueryDto {
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数必须大于等于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer size = 12;
    private String schoolName;
    private String province;
    private String schoolLevel;
    private String schoolType;
}
