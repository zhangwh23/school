package com.school.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.teacher.convert.TeacherConvert;
import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.mapper.TeacherMapper;
import com.school.teacher.service.TeacherService;
import com.school.teacher.vo.TeacherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Override
    public Page<TeacherVO> pageTeachers(Page<Teacher> page, String keyword) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Teacher::getName, keyword)
                    .or()
                    .like(Teacher::getTeacherNo, keyword);
        }
        wrapper.orderByDesc(Teacher::getCreateTime);
        Page<Teacher> result = page(page, wrapper);
        Page<TeacherVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(TeacherConvert.INSTANCE::entityToVo)
                .toList());
        return voPage;
    }

    @Override
    public TeacherVO getTeacherById(Long id) {
        Teacher teacher = getById(id);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }
        return TeacherConvert.INSTANCE.entityToVo(teacher);
    }

    @Override
    public void createTeacher(TeacherDTO dto) {
        Teacher teacher = TeacherConvert.INSTANCE.dtoToEntity(dto);
        save(teacher);
    }

    @Override
    public void updateTeacher(Long id, TeacherDTO dto) {
        Teacher teacher = getById(id);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }
        TeacherConvert.INSTANCE.updateEntityFromDto(teacher, dto);
        updateById(teacher);
    }

    @Override
    public void deleteTeacher(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("教师不存在");
        }
        removeById(id);
    }
}