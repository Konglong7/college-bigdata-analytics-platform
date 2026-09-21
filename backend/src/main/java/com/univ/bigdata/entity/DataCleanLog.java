package com.univ.bigdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_clean_log")
public class DataCleanLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer rawCount;

    private Integer cleanCount;

    private Integer errorCount;

    private LocalDateTime cleanTime;
}
