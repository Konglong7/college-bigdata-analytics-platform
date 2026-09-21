package com.univ.bigdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("university")
public class University {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String schoolName;

    private String schoolCode;

    private String province;

    private String city;

    private String schoolType;

    private String schoolLevel;

    private Integer establishYear;

    private String introduction;

    private Integer rawSchoolId;

    private String belong;

    private String natureName;

    private String dualClassName;

    private String schoolSite;

    private String site;

    private String phone;

    private String email;

    private String address;

    private String postcode;

    private Integer ruankeRank;

    private Integer qsRank;

    private Integer xyhRank;

    private Integer numDoctor;

    private Integer numMaster;

    private Integer numAcademician;

    private String numLibrary;

    private Integer numLab;

    private LocalDateTime createTime;
}
