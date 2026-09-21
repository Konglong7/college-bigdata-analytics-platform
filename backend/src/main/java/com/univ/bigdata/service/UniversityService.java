package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.dto.UniversityDto;
import com.univ.bigdata.dto.UniversityQueryDto;
import com.univ.bigdata.entity.University;
import com.univ.bigdata.vo.UnivDetailVo;
import com.univ.bigdata.vo.UniversityCardVo;

public interface UniversityService extends IService<University> {

    Page<UniversityCardVo> pageUniversities(UniversityQueryDto queryDto);

    UnivDetailVo getUniversityDetail(Long id);

    com.univ.bigdata.vo.UnivCompareVo getCompareData(java.util.List<Long> ids);

    void addUniversity(UniversityDto dto);

    void updateUniversity(UniversityDto dto);

    void deleteUniversity(Long id);
}
