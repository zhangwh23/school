package com.school.teacher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.PageResult;
import com.school.common.Result;
import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.service.TeacherService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public Result<PageResult<Teacher>> pageTeachers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Teacher> result = teacherService.pageTeachers(new Page<>(page, size), keyword);
        PageResult<Teacher> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Teacher> getTeacherById(@PathVariable Long id) {
        return Result.success(teacherService.getTeacherById(id));
    }

    @PostMapping
    public Result<Void> createTeacher(@Valid @RequestBody TeacherDTO dto) {
        teacherService.createTeacher(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherDTO dto) {
        teacherService.updateTeacher(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return Result.success();
    }
}
