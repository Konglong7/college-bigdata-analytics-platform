package com.univ.bigdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.bigdata.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    @Select("SELECT year, SUM(plan_number) as totalPlan, SUM(admission_number) as totalAdmission, ROUND(AVG(score), 1) as avgScore " +
            "FROM enrollment GROUP BY year ORDER BY year ASC")
    List<Map<String, Object>> selectYearlyEnrollmentTrend();

    @Select("SELECT u.school_name as schoolName, SUM(e.plan_number) as planCount, SUM(e.admission_number) as admitCount, ROUND(AVG(e.score), 1) as score " +
            "FROM enrollment e JOIN university u ON e.university_id = u.id " +
            "GROUP BY u.school_name ORDER BY admitCount DESC LIMIT 8")
    List<Map<String, Object>> selectTopSchoolsEnrollment();
}
