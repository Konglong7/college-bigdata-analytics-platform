package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.service.DataCollectLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class CollectController {

    private final DataCollectLogService dataCollectLogService;

    @GetMapping("/nodes")
    public Result<List<Map<String, Object>>> getNodes() {
        return Result.success(dataCollectLogService.getNodes());
    }

    @GetMapping("/logs")
    public Result<List<Map<String, Object>>> getLogs() {
        return Result.success(dataCollectLogService.getRecentLogs());
    }

    @GetMapping("/traffic-trend")
    public Result<Map<String, Object>> getTrafficTrend() {
        return Result.success(dataCollectLogService.getTrafficTrend());
    }
}
