package com.school.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.student.dto.StudentDTO;
import com.school.student.entity.Student;

public interface StudentService extends IService<Student> {

    Page<Student> pageStudents(Page<Student> page, String keyword, Long classId);

    Student getStudentById(Long id);

    void createStudent(StudentDTO dto);

    void updateStudent(Long id, StudentDTO dto);

    void deleteStudent(Long id);
}
