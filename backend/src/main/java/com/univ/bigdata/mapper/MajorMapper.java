package com.univ.bigdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.bigdata.entity.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    @Select("SELECT major_name as majorName, ROUND(AVG(employment_rate), 1) as employmentRate " +
            "FROM major GROUP BY major_name ORDER BY employmentRate DESC LIMIT 10")
    List<Map<String, Object>> selectTop10Employment();

    @Select("SELECT major_name as name, COUNT(DISTINCT university_id) as value " +
            "FROM major GROUP BY major_name ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> selectHotMajors();

    @Select("SELECT major_name as name, category, COUNT(DISTINCT university_id) as value " +
            "FROM major GROUP BY major_name, category ORDER BY value DESC LIMIT 50")
    List<Map<String, Object>> selectHotWordCloud();

    @Select("SELECT COUNT(DISTINCT major_name) FROM major")
    Long countDistinctMajorNames();

    @Select("SELECT category, COUNT(1) as count FROM major GROUP BY category")
    List<Map<String, Object>> countByCategory();
}
