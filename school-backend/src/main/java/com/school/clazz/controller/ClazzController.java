package com.school.clazz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.clazz.dto.ClazzDTO;
import com.school.clazz.entity.Clazz;
import com.school.clazz.service.ClazzService;
import com.school.common.PageResult;
import com.school.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClazzController {

    private final ClazzService clazzService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<PageResult<Clazz>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Clazz> pageResult = clazzService.pageClasses(new Page<>(page, size), keyword);
        return Result.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Clazz> detail(@PathVariable Long id) {
        return Result.success(clazzService.getClazzById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> create(@Validated @RequestBody ClazzDTO dto) {
        clazzService.createClazz(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@PathVariable Long id, @Validated @RequestBody ClazzDTO dto) {
        clazzService.updateClazz(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        clazzService.deleteClazz(id);
        return Result.success();
    }

    @PostMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> assignStudents(@PathVariable Long id, @RequestBody List<Long> studentIds) {
        clazzService.assignStudents(id, studentIds);
        return Result.success();
    }
}
