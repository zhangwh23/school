package com.school.grade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.grade.dto.GradeDTO;
import com.school.grade.dto.GradeStatistics;
import com.school.grade.entity.Grade;
import com.school.grade.vo.GradeVO;

import java.util.List;

public interface GradeService extends IService<Grade> {

    Page<GradeVO> pageGrades(Page<Grade> page, Long studentId, Long courseId, String semester);

    void createGrade(GradeDTO dto);

    void updateGrade(Long id, GradeDTO dto);

    List<GradeStatistics> getStatistics(String semester);
}