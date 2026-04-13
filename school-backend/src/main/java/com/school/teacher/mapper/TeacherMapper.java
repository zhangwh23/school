package com.school.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.teacher.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
