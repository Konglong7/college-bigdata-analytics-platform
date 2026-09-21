package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.service.DataCleanLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/clean")
@RequiredArgsConstructor
public class CleanController {

    private final DataCleanLogService dataCleanLogService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        return Result.success(dataCleanLogService.getSummary());
    }

    @GetMapping("/charts")
    public Result<Map<String, Object>> getCharts() {
        return Result.success(dataCleanLogService.getChartsData());
    }
}
