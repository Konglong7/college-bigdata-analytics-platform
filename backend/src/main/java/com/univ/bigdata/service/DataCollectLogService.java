package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.entity.DataCollectLog;

import java.util.List;
import java.util.Map;

public interface DataCollectLogService extends IService<DataCollectLog> {

    List<Map<String, Object>> getNodes();

    List<Map<String, Object>> getRecentLogs();

    Map<String, Object> getTrafficTrend();
}
