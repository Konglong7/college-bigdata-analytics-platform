package com.univ.bigdata.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionMetricsVo {

    private String modelName;
    private String trainWindow;
    private String predictPeriod;
    private Double mae;
    private Double rmse;
    private String r2;
    private String dataSource;
    private LocalDateTime generatedAt;
}
