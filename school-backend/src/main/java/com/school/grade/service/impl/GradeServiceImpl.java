package com.school.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.course.entity.Course;
import com.school.course.mapper.CourseMapper;
import com.school.grade.convert.GradeConvert;
import com.school.grade.dto.GradeDTO;
import com.school.grade.dto.GradeStatistics;
import com.school.grade.entity.Grade;
import com.school.grade.mapper.GradeMapper;
import com.school.grade.service.GradeService;
import com.school.grade.vo.GradeVO;
import com.school.student.entity.Student;
import com.school.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    @Override
    public Page<GradeVO> pageGrades(Page<Grade> page, Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(studentId != null, Grade::getStudentId, studentId);
        wrapper.eq(courseId != null, Grade::getCourseId, courseId);
        wrapper.eq(StringUtils.hasText(semester), Grade::getSemester, semester);
        wrapper.orderByDesc(Grade::getCreateTime);
        Page<Grade> result = page(page, wrapper);
        Page<GradeVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        List<Grade> records = result.getRecords();
        Set<Long> studentIds = records.stream()
                .map(Grade::getStudentId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> studentNameMap = studentIds.isEmpty() ? Map.of() :
                studentMapper.selectList(new LambdaQueryWrapper<Student>()
                                .in(Student::getId, studentIds))
                        .stream().collect(Collectors.toMap(Student::getId, Student::getName));

        Set<Long> courseIds = records.stream()
                .map(Grade::getCourseId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> courseNameMap = courseIds.isEmpty() ? Map.of() :
                courseMapper.selectList(new LambdaQueryWrapper<Course>()
                                .in(Course::getId, courseIds))
                        .stream().collect(Collectors.toMap(Course::getId, Course::getCourseName));

        voPage.setRecords(records.stream()
                .map(grade -> {
                    GradeVO vo = GradeConvert.INSTANCE.entityToVo(grade);
                    if (grade.getStudentId() != null) {
                        vo.setStudentName(studentNameMap.get(grade.getStudentId()));
                    }
                    if (grade.getCourseId() != null) {
                        vo.setCourseName(courseNameMap.get(grade.getCourseId()));
                    }
                    return vo;
                })
                .toList());
        return voPage;
    }

    @Override
    public void createGrade(GradeDTO dto) {
        Grade grade = GradeConvert.INSTANCE.dtoToEntity(dto);
        save(grade);
    }

    @Override
    public void updateGrade(Long id, GradeDTO dto) {
        Grade grade = getById(id);
        if (grade == null) {
            throw new BusinessException("成绩记录不存在");
        }
        GradeConvert.INSTANCE.updateEntityFromDto(grade, dto);
        updateById(grade);
    }

    @Override
    public List<GradeStatistics> getStatistics(String semester) {
        return baseMapper.selectStatistics(semester);
    }
}