package com.school.dashboard.service.impl;

import com.school.clazz.service.ClazzService;
import com.school.course.service.CourseService;
import com.school.dashboard.dto.DashboardStatsVO;
import com.school.dashboard.service.DashboardService;
import com.school.student.service.StudentService;
import com.school.teacher.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ClazzService clazzService;
    private final CourseService courseService;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setStudentCount(studentService.count());
        vo.setTeacherCount(teacherService.count());
        vo.setClassCount(clazzService.count());
        vo.setCourseCount(courseService.count());
        return vo;
    }
}
