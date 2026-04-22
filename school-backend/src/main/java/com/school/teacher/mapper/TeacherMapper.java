package com.school.teacher.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.teacher.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    default List<Teacher> getListByIds(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapper<Teacher>()
                .in(Teacher::getId, ids));
    }
}
