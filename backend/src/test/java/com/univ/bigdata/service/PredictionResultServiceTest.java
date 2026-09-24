package com.univ.bigdata.service;

import com.univ.bigdata.entity.PredictionResult;
import com.univ.bigdata.mapper.PredictionResultMapper;
import com.univ.bigdata.service.impl.PredictionResultServiceImpl;
import com.univ.bigdata.vo.PredictionMetricsVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionResultServiceTest {

    @Mock
    private PredictionResultMapper predictionResultMapper;

    @InjectMocks
    private PredictionResultServiceImpl service;

    @BeforeEach
    void setBaseMapper() {
        ReflectionTestUtils.setField(service, "baseMapper", predictionResultMapper);
    }

    @Test
    void missingPredictionTypeShouldRemainNull() {
        when(predictionResultMapper.selectList(any()))
                .thenReturn(List.of(PredictionResult.builder()
                        .type("UNIV_COUNT")
                        .year(2028)
                        .predictValue(new BigDecimal("3165.0"))
                        .build()));

        Map<String, Object> trends = service.getTrendsData();
        Map<String, Object> enroll = castMap(trends.get("enroll"));

        assertTrue(((List<?>) enroll.get("enrollTotal")).stream().anyMatch(Objects::isNull));
    }

    @Test
    void modelMetricsShouldIdentifyPredictionDataSource() {
        when(predictionResultMapper.selectList(any())).thenReturn(List.of());

        PredictionMetricsVo metrics = service.getModelMetrics();

        assertEquals("prediction_result", metrics.getDataSource());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
