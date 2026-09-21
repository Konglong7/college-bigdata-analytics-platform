package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.entity.PredictionResult;

import java.util.Map;

public interface PredictionResultService extends IService<PredictionResult> {

    Map<String, Object> getModelMetrics();

    Map<String, Object> getTrendsData();
}
