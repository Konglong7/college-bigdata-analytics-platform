package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/compare-stats")
    public Result<Map<String, Object>> getCompareStats() {
        return Result.success(enrollmentService.getCompareStats());
    }

    @GetMapping("/batch-funnel")
    public Result<List<Map<String, Object>>> getBatchFunnel() {
        return Result.success(enrollmentService.getBatchFunnel());
    }

    @GetMapping("/matrix-plan")
    public Result<Map<String, Object>> getMatrixPlan() {
        return Result.success(enrollmentService.getMatrixPlan());
    }
}
