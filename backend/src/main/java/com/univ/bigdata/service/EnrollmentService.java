package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.entity.Enrollment;

import java.util.List;
import java.util.Map;

public interface EnrollmentService extends IService<Enrollment> {

    Map<String, Object> getCompareStats();

    List<Map<String, Object>> getBatchFunnel();

    Map<String, Object> getMatrixPlan();
}
