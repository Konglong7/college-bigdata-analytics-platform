package com.univ.bigdata.service;

import com.univ.bigdata.dto.RecommendQueryDto;
import com.univ.bigdata.vo.RecommendResultVo;

public interface RecommendService {

    /**
     * 智能志愿梯度推荐算法 (冲·稳·保)
     *
     * @param queryDto 考生考分、省份与偏好参数
     * @return 冲刺、稳妥、保底三档推荐方案
     */
    RecommendResultVo recommendVolunteers(RecommendQueryDto queryDto);
}
