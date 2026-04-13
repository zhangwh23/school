package com.school.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;

public interface CourseService extends IService<Course> {

    Page<Course> pageCourses(Page<Course> page, String keyword, Long teacherId, Long classId);

    Course getCourseById(Long id);

    void createCourse(CourseDTO dto);

    void updateCourse(Long id, CourseDTO dto);

    void deleteCourse(Long id);
}
