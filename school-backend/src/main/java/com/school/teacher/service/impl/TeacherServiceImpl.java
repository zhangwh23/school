package com.school.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.mapper.TeacherMapper;
import com.school.teacher.service.TeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Override
    public Page<Teacher> pageTeachers(Page<Teacher> page, String keyword) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Teacher::getName, keyword)
                    .or()
                    .like(Teacher::getTeacherNo, keyword);
        }
        return page(page, wrapper);
    }

    @Override
    public Teacher getTeacherById(Long id) {
        Teacher teacher = getById(id);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }
        return teacher;
    }

    @Override
    public void createTeacher(TeacherDTO dto) {
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(dto, teacher);
        save(teacher);
    }

    @Override
    public void updateTeacher(Long id, TeacherDTO dto) {
        Teacher teacher = getTeacherById(id);
        BeanUtils.copyProperties(dto, teacher);
        updateById(teacher);
    }

    @Override
    public void deleteTeacher(Long id) {
        getTeacherById(id);
        removeById(id);
    }
}
