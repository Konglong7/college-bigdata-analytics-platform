package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.dto.RecommendQueryDto;
import com.univ.bigdata.service.RecommendService;
import com.univ.bigdata.vo.RecommendResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/match")
    public Result<RecommendResultVo> matchVolunteers(@RequestBody RecommendQueryDto queryDto) {
        RecommendResultVo result = recommendService.recommendVolunteers(queryDto);
        return Result.success(result);
    }
}
