package com.univ.bigdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.univ.bigdata.entity.DataCollectLog;
import com.univ.bigdata.mapper.DataCollectLogMapper;
import com.univ.bigdata.service.DataCollectLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataCollectLogServiceImpl extends ServiceImpl<DataCollectLogMapper, DataCollectLog> implements DataCollectLogService {

    @Override
    public List<Map<String, Object>> getNodes() {
        return List.of(
                Map.of(
                        "name", "教育部公开数据网",
                        "status", "运行中",
                        "type", "全国高校名录 / 资质认证",
                        "updateTime", "10分钟前",
                        "throughput", "420 条/分"
                ),
                Map.of(
                        "name", "各高校官方门户",
                        "status", "运行中",
                        "type", "师资 / 重点实验室 / 就业报告",
                        "updateTime", "25分钟前",
                        "throughput", "850 条/分"
                ),
                Map.of(
                        "name", "全国招生阳光高考网",
                        "status", "运行中",
                        "type", "历年分数线 / 招生计划指标",
                        "updateTime", "2小时前",
                        "throughput", "1200 条/分"
                ),
                Map.of(
                        "name", "专业学科数据库",
                        "status", "运行中",
                        "type", "学科评估等级 / 专业目录代码",
                        "updateTime", "1天前",
                        "throughput", "180 条/分"
                )
        );
    }

    @Override
    public List<Map<String, Object>> getRecentLogs() {
        List<DataCollectLog> list = this.list(new LambdaQueryWrapper<DataCollectLog>()
                .orderByDesc(DataCollectLog::getCollectTime)
                .last("LIMIT 10"));

        if (list.isEmpty()) {
            return List.of(
                    Map.of("time", "2026-09-20 10:20:15", "msg", "高校基础数据采集任务 #1092 已完成", "badge", "+320 条", "level", "success"),
                    Map.of("time", "2026-09-20 10:18:42", "msg", "全国高校新增专业审批名录抓取入库", "badge", "+158 条", "level", "success"),
                    Map.of("time", "2026-09-20 10:14:02", "msg", "阳光高考网反爬限频重试调度成功", "badge", "重试成功", "level", "warn"),
                    Map.of("time", "2026-09-20 10:05:33", "msg", "教育部直属高校双一流动态名录全量更新同步", "badge", "+147 所", "level", "success")
            );
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> result = new ArrayList<>();
        for (DataCollectLog log : list) {
            String level = "SUCCESS".equalsIgnoreCase(log.getStatus()) ? "success" : ("WARN".equalsIgnoreCase(log.getStatus()) ? "warn" : "error");
            result.add(Map.of(
                    "time", log.getCollectTime().format(formatter),
                    "msg", log.getSourceName() + " 采集调度执行",
                    "badge", "+" + log.getDataCount() + " 条",
                    "level", level
            ));
        }
        return result;
    }

    @Override
    public Map<String, Object> getTrafficTrend() {
        return Map.of(
                "times", List.of("00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"),
                "series", List.of(
                        Map.of("name", "教育部官网", "data", List.of(120, 80, 70, 430, 520, 480, 390, 210)),
                        Map.of("name", "高校门户", "data", List.of(220, 180, 150, 780, 950, 820, 710, 430)),
                        Map.of("name", "阳光高考网", "data", List.of(310, 120, 90, 1100, 1340, 1200, 980, 600)),
                        Map.of("name", "学科库", "data", List.of(50, 30, 20, 160, 210, 190, 170, 80))
                )
        );
    }
}
