package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsVo {
    private Long totalUniversity;
    private Long undergraduateCount;
    private Long majorCount;
    private Integer provinceCount;
}
