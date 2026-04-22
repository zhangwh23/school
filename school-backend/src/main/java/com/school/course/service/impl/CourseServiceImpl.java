package com.school.course.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.clazz.entity.Clazz;
import com.school.clazz.mapper.ClazzMapper;
import com.school.common.BusinessException;
import com.school.course.convert.CourseConvert;
import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import com.school.course.mapper.CourseMapper;
import com.school.course.service.CourseService;
import com.school.course.vo.CourseVO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final ClazzMapper clazzMapper;

    @Override
    public Page<CourseVO> pageCourses(Page<Course> page, String keyword, Long teacherId, Long classId) {
        // 1. 分页查询课程
        Page<Course> result = courseMapper.pageByCondition(page, keyword, teacherId, classId);

        // 2. 查询关联的教师名称
        Map<Long, String> teacherNameMap = getTeacherNameMap(result.getRecords());

        // 3. 查询关联的班级名称
        Map<Long, String> classNameMap = getClassNameMap(result.getRecords());

        // 4. 转换并填充关联名称
        Page<CourseVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toVoList(result.getRecords(), teacherNameMap, classNameMap));
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

    private Map<Long, String> getTeacherNameMap(List<Course> records) {
        Set<Long> teacherIds = extractField(records, Course::getTeacherId);
        if (teacherIds.isEmpty()) {
            return Map.of();
        }
        return teacherMapper.getListByIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, Teacher::getName));
    }

    private Map<Long, String> getClassNameMap(List<Course> records) {
        Set<Long> classIds = extractField(records, Course::getClassId);
        if (classIds.isEmpty()) {
            return Map.of();
        }
        return clazzMapper.getListByIds(classIds).stream()
                .collect(Collectors.toMap(Clazz::getId, Clazz::getClassName));
    }

    private <T> Set<Long> extractField(List<T> records, Function<T, Long> extractor) {
        return records.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<CourseVO> toVoList(List<Course> records, Map<Long, String> teacherNameMap, Map<Long, String> classNameMap) {
        return records.stream()
                .map(course -> {
                    CourseVO vo = CourseConvert.INSTANCE.entityToVo(course);
                    if (course.getTeacherId() != null) {
                        vo.setTeacherName(teacherNameMap.get(course.getTeacherId()));
                    }
                    if (course.getClassId() != null) {
                        vo.setClassName(classNameMap.get(course.getClassId()));
                    }
                    return vo;
                })
                .toList();
    }
}