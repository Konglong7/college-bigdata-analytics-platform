package com.univ.bigdata.service;

import com.univ.bigdata.vo.DashboardEnrollVo;
import com.univ.bigdata.vo.DashboardStatsVo;
import com.univ.bigdata.vo.DashboardTrendVo;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardStatsVo getStatistics();

    List<Map<String, Object>> getMapDistribution();

    List<Map<String, Object>> getTypeRatio();

    DashboardTrendVo getGrowthTrend();

    Map<String, Object> getProvinceTop10();

    List<Map<String, Object>> getHotMajors();

    DashboardEnrollVo getEnrollTrend();
}
