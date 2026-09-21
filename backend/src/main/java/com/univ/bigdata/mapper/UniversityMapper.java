package com.univ.bigdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.bigdata.entity.University;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UniversityMapper extends BaseMapper<University> {

    @Select("SELECT province, COUNT(1) as count FROM university GROUP BY province ORDER BY count DESC")
    List<Map<String, Object>> countByProvince();

    @Select("SELECT school_type as type, COUNT(1) as count FROM university GROUP BY school_type")
    List<Map<String, Object>> countByType();

    @Select("SELECT school_level as level, COUNT(1) as count FROM university GROUP BY school_level")
    List<Map<String, Object>> countByLevel();

    @Select("SELECT CASE " +
            "  WHEN establish_year < 1920 THEN '1920以前' " +
            "  WHEN establish_year < 1950 THEN '1920-1949' " +
            "  WHEN establish_year < 1980 THEN '1950-1979' " +
            "  WHEN establish_year < 2000 THEN '1980-1999' " +
            "  WHEN establish_year < 2015 THEN '2000-2014' " +
            "  ELSE '2015至今' END as decade, COUNT(1) as count " +
            "FROM university WHERE establish_year IS NOT NULL AND establish_year > 0 " +
            "GROUP BY decade ORDER BY MIN(establish_year) ASC")
    List<Map<String, Object>> countByEstablishDecade();
}
