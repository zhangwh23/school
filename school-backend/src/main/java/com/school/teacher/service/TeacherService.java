package com.school.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.vo.TeacherVO;

public interface TeacherService extends IService<Teacher> {

    Page<TeacherVO> pageTeachers(Page<Teacher> page, String keyword);

    TeacherVO getTeacherById(Long id);

    void createTeacher(TeacherDTO dto);

    void updateTeacher(Long id, TeacherDTO dto);

    void deleteTeacher(Long id);
}