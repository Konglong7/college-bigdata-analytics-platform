package com.univ.bigdata.dto;

import lombok.Data;

@Data
public class UniversityQueryDto {
    private Integer page = 1;
    private Integer size = 12;
    private String schoolName;
    private String province;
    private String schoolLevel;
    private String schoolType;
}
