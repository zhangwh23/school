package com.school.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.course.convert.CourseConvert;
import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import com.school.course.mapper.CourseMapper;
import com.school.course.service.CourseService;
import com.school.course.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public Page<CourseVO> pageCourses(Page<Course> page, String keyword, Long teacherId, Long classId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword)
        );
        wrapper.eq(teacherId != null, Course::getTeacherId, teacherId);
        wrapper.eq(classId != null, Course::getClassId, classId);
        wrapper.orderByDesc(Course::getCreateTime);
        Page<Course> result = page(page, wrapper);
        Page<CourseVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(CourseConvert.INSTANCE::entityToVo)
                .toList());
        return voPage;
    }

    @Override
    public CourseVO getCourseById(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return CourseConvert.INSTANCE.entityToVo(course);
    }

    @Override
    public void createCourse(CourseDTO dto) {
        Course course = CourseConvert.INSTANCE.dtoToEntity(dto);
        save(course);
    }

    @Override
    public void updateCourse(Long id, CourseDTO dto) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        CourseConvert.INSTANCE.updateEntityFromDto(course, dto);
        updateById(course);
    }

    @Override
    public void deleteCourse(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("课程不存在");
        }
        removeById(id);
    }
}