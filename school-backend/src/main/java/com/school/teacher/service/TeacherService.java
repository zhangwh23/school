package com.school.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;

public interface TeacherService extends IService<Teacher> {

    Page<Teacher> pageTeachers(Page<Teacher> page, String keyword);

    Teacher getTeacherById(Long id);

    void createTeacher(TeacherDTO dto);

    void updateTeacher(Long id, TeacherDTO dto);

    void deleteTeacher(Long id);
}
