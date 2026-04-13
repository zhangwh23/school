package com.school.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.grade.convert.GradeConvert;
import com.school.grade.dto.GradeDTO;
import com.school.grade.dto.GradeStatistics;
import com.school.grade.entity.Grade;
import com.school.grade.mapper.GradeMapper;
import com.school.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    @Override
    public Page<Grade> pageGrades(Page<Grade> page, Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(studentId != null, Grade::getStudentId, studentId);
        wrapper.eq(courseId != null, Grade::getCourseId, courseId);
        wrapper.eq(StringUtils.hasText(semester), Grade::getSemester, semester);
        wrapper.orderByDesc(Grade::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void createGrade(GradeDTO dto) {
        Grade grade = GradeConvert.INSTANCE.convert(dto);
        save(grade);
    }

    @Override
    public void updateGrade(Long id, GradeDTO dto) {
        Grade grade = getById(id);
        if (grade == null) {
            throw new BusinessException("成绩记录不存在");
        }
        GradeConvert.INSTANCE.updateEntity(grade, dto);
        updateById(grade);
    }

    @Override
    public List<GradeStatistics> getStatistics(String semester) {
        return baseMapper.selectStatistics(semester);
    }
}
