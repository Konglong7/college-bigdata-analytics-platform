package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @GetMapping("/heat-calendar")
    public Result<List<List<Object>>> getHeatCalendar() {
        return Result.success(majorService.getHeatCalendarData());
    }

    @GetMapping("/employment-top10")
    public Result<Map<String, Object>> getEmploymentTop10() {
        return Result.success(majorService.getEmploymentTop10());
    }

    @GetMapping("/new-emerging-trend")
    public Result<Map<String, Object>> getNewEmergingTrend() {
        return Result.success(majorService.getNewEmergingTrend());
    }
}
