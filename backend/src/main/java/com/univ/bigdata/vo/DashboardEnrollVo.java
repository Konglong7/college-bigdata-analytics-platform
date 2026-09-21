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
public class DashboardEnrollVo {
    private List<String> years;
    private List<Integer> undergraduate;
    private List<Integer> juniorCollege;
}
