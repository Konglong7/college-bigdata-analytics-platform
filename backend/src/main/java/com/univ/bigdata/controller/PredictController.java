package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.service.PredictionResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
public class PredictController {

    private final PredictionResultService predictionResultService;

    @GetMapping("/model-metrics")
    public Result<Map<String, Object>> getModelMetrics() {
        return Result.success(predictionResultService.getModelMetrics());
    }

    @GetMapping("/trends")
    public Result<Map<String, Object>> getTrends() {
        return Result.success(predictionResultService.getTrendsData());
    }
}
