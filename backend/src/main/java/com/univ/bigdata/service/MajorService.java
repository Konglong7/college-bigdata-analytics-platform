package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.entity.Major;

import java.util.List;
import java.util.Map;

public interface MajorService extends IService<Major> {

    List<List<Object>> getHeatCalendarData();

    Map<String, Object> getEmploymentTop10();

    Map<String, Object> getNewEmergingTrend();
}
