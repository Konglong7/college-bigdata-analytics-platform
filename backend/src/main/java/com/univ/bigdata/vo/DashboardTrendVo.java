package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendVo {
    private List<String> years;
    private List<Integer> values;
    // 扩展建校年代演变与时期新增/累计分布
    private List<String> decadeLabels;
    private List<Integer> decadeCounts;
    private List<Integer> decadeAccum;
}

