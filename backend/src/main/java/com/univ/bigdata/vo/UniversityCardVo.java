package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityCardVo {
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
    private String schoolSite;
    private String gaokaoSite;
    private String belong;
    private String dualClassName;
    private String natureName;
}
