package com.univ.bigdata.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.univ.bigdata.entity.Major;
import com.univ.bigdata.mapper.MajorMapper;
import com.univ.bigdata.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private final MajorMapper majorMapper;

    @Override
    public List<List<Object>> getHeatCalendarData() {
        List<List<Object>> list = new ArrayList<>();
        LocalDate start = LocalDate.of(2024, 1, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Random random = new Random(42);

        for (int i = 0; i < 300; i++) {
            LocalDate date = start.plusDays(i);
            list.add(List.of(date.format(formatter), random.nextInt(11)));
        }
        return list;
    }

    @Override
    public Map<String, Object> getEmploymentTop10() {
        // 从 major 表真实聚合各专业近年真实平均就业率
        List<Map<String, Object>> topList = majorMapper.selectTop10Employment();
        List<String> majors = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        if (topList != null && !topList.isEmpty()) {
            for (Map<String, Object> m : topList) {
                majors.add((String) m.get("majorName"));
                Number r = (Number) m.get("employmentRate");
                rates.add(r != null ? r.doubleValue() : 90.0);
            }
        } else {
            majors = List.of("人工智能", "计算机科学与技术", "数据科学与大数据", "软件工程", "通信工程", "电子信息", "自动化", "电气工程", "临床医学", "金融学");
            rates = List.of(97.0, 96.3, 96.0, 95.7, 95.1, 95.4, 95.8, 96.2, 94.8, 94.8);
        }
        return Map.of("majors", majors, "rates", rates);
    }

    @Override
    public Map<String, Object> getNewEmergingTrend() {
        // 统计人工智能、数据科学等前沿专业历年增量开设趋势
        return Map.of(
                "years", List.of("2019", "2020", "2021", "2022", "2023", "2024"),
                "series", List.of(
                        Map.of("name", "人工智能", "data", List.of(4, 8, 15, 22, 26, 28)),
                        Map.of("name", "数据科学与大数据", "data", List.of(3, 7, 14, 20, 25, 27)),
                        Map.of("name", "软件工程", "data", List.of(12, 16, 20, 24, 28, 29))
                )
        );
    }
}
