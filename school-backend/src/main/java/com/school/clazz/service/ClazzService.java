package com.school.clazz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.clazz.dto.ClazzDTO;
import com.school.clazz.entity.Clazz;

import java.util.List;

public interface ClazzService extends IService<Clazz> {

    Page<Clazz> pageClasses(Page<Clazz> page, String keyword);

    Clazz getClazzById(Long id);

    void createClazz(ClazzDTO dto);

    void updateClazz(Long id, ClazzDTO dto);

    void deleteClazz(Long id);

    void assignStudents(Long classId, List<Long> studentIds);
}
