package com.school.clazz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.clazz.convert.ClazzConvert;
import com.school.clazz.dto.ClazzDTO;
import com.school.clazz.entity.Clazz;
import com.school.clazz.mapper.ClazzMapper;
import com.school.clazz.service.ClazzService;
import com.school.clazz.vo.ClazzVO;
import com.school.common.BusinessException;
import com.school.student.entity.Student;
import com.school.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClazzServiceImpl extends ServiceImpl<ClazzMapper, Clazz> implements ClazzService {

    private final StudentMapper studentMapper;

    @Override
    public Page<ClazzVO> pageClasses(Page<Clazz> page, String keyword) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w ->
                w.like(Clazz::getClassName, keyword).or().like(Clazz::getGradeLevel, keyword)
        );
        wrapper.orderByDesc(Clazz::getCreateTime);
        Page<Clazz> result = page(page, wrapper);
        Page<ClazzVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(ClazzConvert.INSTANCE::entityToVo)
                .toList());
        return voPage;
    }

    @Override
    public ClazzVO getClazzById(Long id) {
        Clazz clazz = getById(id);
        if (clazz == null) {
            throw new BusinessException("班级不存在");
        }
        return ClazzConvert.INSTANCE.entityToVo(clazz);
    }

    @Override
    public void createClazz(ClazzDTO dto) {
        Clazz clazz = ClazzConvert.INSTANCE.dtoToEntity(dto);
        save(clazz);
    }

    @Override
    public void updateClazz(Long id, ClazzDTO dto) {
        Clazz clazz = getById(id);
        if (clazz == null) {
            throw new BusinessException("班级不存在");
        }
        ClazzConvert.INSTANCE.updateEntityFromDto(clazz, dto);
        updateById(clazz);
    }

    @Override
    public void deleteClazz(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("班级不存在");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignStudents(Long classId, List<Long> studentIds) {
        getById(classId);

        LambdaUpdateWrapper<Student> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Student::getId, studentIds)
                .set(Student::getClassId, classId);
        studentMapper.update(null, updateWrapper);

        LambdaQueryWrapper<Student> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Student::getClassId, classId);
        long count = studentMapper.selectCount(countWrapper);

        Clazz clazz = new Clazz();
        clazz.setId(classId);
        clazz.setStudentCount((int) count);
        updateById(clazz);
    }
}