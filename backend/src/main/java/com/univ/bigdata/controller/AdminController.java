package com.univ.bigdata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.dto.UniversityDto;
import com.univ.bigdata.dto.UniversityQueryDto;
import com.univ.bigdata.service.UniversityService;
import com.univ.bigdata.vo.UniversityCardVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UniversityService universityService;

    @GetMapping("/university/page")
    public Result<Page<UniversityCardVo>> pageUniversities(@Valid UniversityQueryDto queryDto) {
        Page<UniversityCardVo> page = universityService.pageUniversities(queryDto);
        return Result.success(page);
    }

    @PostMapping("/university")
    public Result<Void> addUniversity(@Valid @RequestBody UniversityDto dto) {
        universityService.addUniversity(dto);
        return Result.success("新增高校成功", null);
    }

    @PutMapping("/university")
    public Result<Void> updateUniversity(@Valid @RequestBody UniversityDto dto) {
        universityService.updateUniversity(dto);
        return Result.success("更新高校成功", null);
    }

    @DeleteMapping("/university/{id}")
    public Result<Void> deleteUniversity(@PathVariable("id") @Positive(message = "高校ID必须为正数") Long id) {
        universityService.deleteUniversity(id);
        return Result.success("删除高校成功", null);
    }
}
