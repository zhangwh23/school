package com.school.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import com.school.course.vo.CourseVO;

public interface CourseService extends IService<Course> {

    Page<CourseVO> pageCourses(Page<Course> page, String keyword, Long teacherId, Long classId);

    CourseVO getCourseById(Long id);

    void createCourse(CourseDTO dto);

    void updateCourse(Long id, CourseDTO dto);

    void deleteCourse(Long id);
}