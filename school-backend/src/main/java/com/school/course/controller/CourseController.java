package com.school.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.PageResult;
import com.school.common.Result;
import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import com.school.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<PageResult<Course>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long classId) {
        Page<Course> pageResult = courseService.pageCourses(new Page<>(page, size), keyword, teacherId, classId);
        return Result.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Course> detail(@PathVariable Long id) {
        return Result.success(courseService.getCourseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> create(@Validated @RequestBody CourseDTO dto) {
        courseService.createCourse(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@PathVariable Long id, @Validated @RequestBody CourseDTO dto) {
        courseService.updateCourse(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }
}
