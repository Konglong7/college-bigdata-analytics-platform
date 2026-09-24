package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.entity.PredictionResult;
import com.univ.bigdata.vo.PredictionMetricsVo;

import java.util.Map;

public interface PredictionResultService extends IService<PredictionResult> {

    PredictionMetricsVo getModelMetrics();

    Map<String, Object> getTrendsData();
}
