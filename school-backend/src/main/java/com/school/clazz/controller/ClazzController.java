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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 班级管理接口
 */
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
        Page<Clazz> result = clazzService.pageClasses(new Page<>(page, size), keyword);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Clazz> detail(@PathVariable Long id) {
        return Result.success(clazzService.getClazzById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Validated @RequestBody ClazzDTO dto) {
        clazzService.createClazz(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody ClazzDTO dto) {
        clazzService.updateClazz(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        clazzService.deleteClazz(id);
        return Result.success();
    }

    @PostMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignStudents(@PathVariable Long id, @RequestBody List<Long> studentIds) {
        clazzService.assignStudents(id, studentIds);
        return Result.success();
    }
}
