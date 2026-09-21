package com.univ.bigdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("prediction_result")
public class PredictionResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private Integer year;

    private BigDecimal predictValue;
}
