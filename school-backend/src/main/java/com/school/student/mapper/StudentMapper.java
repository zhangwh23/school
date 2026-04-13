package com.school.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
