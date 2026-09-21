package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.service.DashboardService;
import com.univ.bigdata.vo.DashboardEnrollVo;
import com.univ.bigdata.vo.DashboardStatsVo;
import com.univ.bigdata.vo.DashboardTrendVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public Result<DashboardStatsVo> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }

    @GetMapping("/map-distribution")
    public Result<List<Map<String, Object>>> getMapDistribution() {
        return Result.success(dashboardService.getMapDistribution());
    }

    @GetMapping("/type-ratio")
    public Result<List<Map<String, Object>>> getTypeRatio() {
        return Result.success(dashboardService.getTypeRatio());
    }

    @GetMapping("/growth-trend")
    public Result<DashboardTrendVo> getGrowthTrend() {
        return Result.success(dashboardService.getGrowthTrend());
    }

    @GetMapping("/province-top10")
    public Result<Map<String, Object>> getProvinceTop10() {
        return Result.success(dashboardService.getProvinceTop10());
    }

    @GetMapping("/hot-majors")
    public Result<List<Map<String, Object>>> getHotMajors() {
        return Result.success(dashboardService.getHotMajors());
    }

    @GetMapping("/enroll-trend")
    public Result<DashboardEnrollVo> getEnrollTrend() {
        return Result.success(dashboardService.getEnrollTrend());
    }
}
