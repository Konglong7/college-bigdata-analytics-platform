package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnivCompareVo {
    /** 参与对比的高校基础画像列表 */
    private List<UnivDetailVo> schools;

    /** 六维办学实力同屏雷达对比 */
    private Map<String, Object> radarComparison;

    /** 历年录取调档分数线同屏折线走势对比 */
    private Map<String, Object> scoreComparison;

    /** 核心量化指标矩阵对齐表 */
    private List<Map<String, Object>> metricMatrix;
}
