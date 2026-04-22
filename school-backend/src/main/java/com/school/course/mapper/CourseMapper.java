package com.school.course.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 根据条件分页查询课程
     */
    default Page<Course> pageByCondition(Page<Course> page, String keyword, Long teacherId, Long classId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword)
        );
        wrapper.eq(teacherId != null, Course::getTeacherId, teacherId);
        wrapper.eq(classId != null, Course::getClassId, classId);
        wrapper.orderByDesc(Course::getCreateTime);
        return selectPage(page, wrapper);
    }

    /**
     * 根据ID列表批量查询课程
     */
    default List<Course> getListByIds(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapper<Course>()
                .in(Course::getId, ids));
    }
}
