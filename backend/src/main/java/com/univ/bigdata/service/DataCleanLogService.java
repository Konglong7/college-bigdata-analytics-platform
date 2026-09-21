package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.entity.DataCleanLog;

import java.util.Map;

public interface DataCleanLogService extends IService<DataCleanLog> {

    Map<String, Object> getSummary();

    Map<String, Object> getChartsData();
}
