package com.school.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.student.convert.StudentConvert;
import com.school.student.dto.StudentDTO;
import com.school.student.entity.Student;
import com.school.student.mapper.StudentMapper;
import com.school.student.service.StudentService;
import com.school.student.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public Page<StudentVO> pageStudents(Page<Student> page, String keyword, Long classId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(Student::getName, keyword).or().like(Student::getStudentNo, keyword)
        );
        wrapper.eq(classId != null, Student::getClassId, classId);
        wrapper.orderByDesc(Student::getCreateTime);
        Page<Student> result = page(page, wrapper);
        Page<StudentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(StudentConvert.INSTANCE::entityToVo)
                .toList());
        return voPage;
    }

    @Override
    public StudentVO getStudentById(Long id) {
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        return StudentConvert.INSTANCE.entityToVo(student);
    }

    @Override
    public void createStudent(StudentDTO dto) {
        Student student = StudentConvert.INSTANCE.dtoToEntity(dto);
        save(student);
    }

    @Override
    public void updateStudent(Long id, StudentDTO dto) {
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        StudentConvert.INSTANCE.updateEntityFromDto(student, dto);
        updateById(student);
    }

    @Override
    public void deleteStudent(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("学生不存在");
        }
        removeById(id);
    }
}