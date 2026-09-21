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
@TableName("major")
public class Major {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long universityId;

    private String majorName;

    private String category;

    private BigDecimal employmentRate;
}
