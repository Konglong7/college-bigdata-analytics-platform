package com.univ.bigdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.bigdata.entity.DataCleanLog;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Select;
import java.util.Map;

@Mapper
public interface DataCleanLogMapper extends BaseMapper<DataCleanLog> {

    @Select("SELECT COALESCE(SUM(raw_count), 0) as totalRaw, " +
            "COALESCE(SUM(clean_count), 0) as totalClean, " +
            "COALESCE(SUM(error_count), 0) as totalError " +
            "FROM data_clean_log")
    Map<String, Object> selectCleanSummary();
}
