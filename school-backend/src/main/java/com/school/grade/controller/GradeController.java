package com.school.grade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.PageResult;
import com.school.common.Result;
import com.school.grade.dto.GradeDTO;
import com.school.grade.dto.GradeStatistics;
import com.school.grade.entity.Grade;
import com.school.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public Result<PageResult<Grade>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String semester) {
        Page<Grade> pageResult = gradeService.pageGrades(new Page<>(page, size), studentId, courseId, semester);
        return Result.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> create(@Validated @RequestBody GradeDTO dto) {
        gradeService.createGrade(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> update(@PathVariable Long id, @Validated @RequestBody GradeDTO dto) {
        gradeService.updateGrade(id, dto);
        return Result.success();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<List<GradeStatistics>> statistics(@RequestParam String semester) {
        return Result.success(gradeService.getStatistics(semester));
    }
}
