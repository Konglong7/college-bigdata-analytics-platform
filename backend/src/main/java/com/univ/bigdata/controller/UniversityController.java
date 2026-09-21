package com.univ.bigdata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.dto.UniversityQueryDto;
import com.univ.bigdata.service.UniversityService;
import com.univ.bigdata.vo.UnivDetailVo;
import com.univ.bigdata.vo.UniversityCardVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping("/page")
    public Result<Page<UniversityCardVo>> pageUniversities(UniversityQueryDto queryDto) {
        Page<UniversityCardVo> page = universityService.pageUniversities(queryDto);
        return Result.success(page);
    }

    @GetMapping("/{id}/detail")
    public Result<UnivDetailVo> getUniversityDetail(@PathVariable("id") Long id) {
        UnivDetailVo detail = universityService.getUniversityDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/compare")
    public Result<com.univ.bigdata.vo.UnivCompareVo> getUniversityCompare(@RequestParam(value = "ids", required = false) java.util.List<Long> ids) {
        com.univ.bigdata.vo.UnivCompareVo compareVo = universityService.getCompareData(ids);
        return Result.success(compareVo);
    }
}
