package com.school.teacher.convert;

import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 教师对象转换器
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeacherConvert {

    TeacherConvert INSTANCE = Mappers.getMapper(TeacherConvert.class);

    Teacher convert(TeacherDTO dto);

    void updateEntity(@MappingTarget Teacher entity, TeacherDTO dto);
}
