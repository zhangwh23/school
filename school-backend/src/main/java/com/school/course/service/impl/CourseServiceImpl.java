package com.school.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import com.school.course.mapper.CourseMapper;
import com.school.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public Page<Course> pageCourses(Page<Course> page, String keyword, Long teacherId, Long classId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword)
        );
        wrapper.eq(teacherId != null, Course::getTeacherId, teacherId);
        wrapper.eq(classId != null, Course::getClassId, classId);
        wrapper.orderByDesc(Course::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Course getCourseById(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return course;
    }

    @Override
    public void createCourse(CourseDTO dto) {
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        save(course);
    }

    @Override
    public void updateCourse(Long id, CourseDTO dto) {
        Course course = getCourseById(id);
        BeanUtils.copyProperties(dto, course);
        course.setId(id);
        updateById(course);
    }

    @Override
    public void deleteCourse(Long id) {
        getCourseById(id);
        removeById(id);
    }
}
