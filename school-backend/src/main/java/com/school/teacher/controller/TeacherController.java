package com.school.teacher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.PageResult;
import com.school.common.Result;
import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 教师管理接口
 */
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public Result<PageResult<Teacher>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Teacher> result = teacherService.pageTeachers(new Page<>(page, size), keyword);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/{id}")
    public Result<Teacher> detail(@PathVariable Long id) {
        return Result.success(teacherService.getTeacherById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody TeacherDTO dto) {
        teacherService.createTeacher(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherDTO dto) {
        teacherService.updateTeacher(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return Result.success();
    }
}
