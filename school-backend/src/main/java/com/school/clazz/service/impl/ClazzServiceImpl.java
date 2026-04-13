package com.school.clazz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.clazz.dto.ClazzDTO;
import com.school.clazz.entity.Clazz;
import com.school.clazz.mapper.ClazzMapper;
import com.school.clazz.service.ClazzService;
import com.school.common.BusinessException;
import com.school.student.entity.Student;
import com.school.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClazzServiceImpl extends ServiceImpl<ClazzMapper, Clazz> implements ClazzService {

    private final StudentMapper studentMapper;

    @Override
    public Page<Clazz> pageClasses(Page<Clazz> page, String keyword) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(Clazz::getClassName, keyword).or().like(Clazz::getGradeLevel, keyword)
        );
        wrapper.orderByDesc(Clazz::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Clazz getClazzById(Long id) {
        Clazz clazz = getById(id);
        if (clazz == null) {
            throw new BusinessException("班级不存在");
        }
        return clazz;
    }

    @Override
    public void createClazz(ClazzDTO dto) {
        Clazz clazz = new Clazz();
        BeanUtils.copyProperties(dto, clazz);
        save(clazz);
    }

    @Override
    public void updateClazz(Long id, ClazzDTO dto) {
        Clazz clazz = getClazzById(id);
        BeanUtils.copyProperties(dto, clazz);
        clazz.setId(id);
        updateById(clazz);
    }

    @Override
    public void deleteClazz(Long id) {
        getClazzById(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignStudents(Long classId, List<Long> studentIds) {
        // 确认班级存在
        getClazzById(classId);

        // 批量更新学生的classId
        LambdaUpdateWrapper<Student> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Student::getId, studentIds)
                .set(Student::getClassId, classId);
        studentMapper.update(null, updateWrapper);

        // 更新班级的studentCount
        LambdaQueryWrapper<Student> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Student::getClassId, classId);
        long count = studentMapper.selectCount(countWrapper);

        Clazz clazz = new Clazz();
        clazz.setId(classId);
        clazz.setStudentCount((int) count);
        updateById(clazz);
    }
}
