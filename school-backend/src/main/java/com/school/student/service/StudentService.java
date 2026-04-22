package com.school.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.student.dto.StudentDTO;
import com.school.student.entity.Student;
import com.school.student.vo.StudentVO;

public interface StudentService extends IService<Student> {

    Page<StudentVO> pageStudents(Page<Student> page, String keyword, Long classId);

    StudentVO getStudentById(Long id);

    void createStudent(StudentDTO dto);

    void updateStudent(Long id, StudentDTO dto);

    void deleteStudent(Long id);
}