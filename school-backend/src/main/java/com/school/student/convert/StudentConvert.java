package com.school.student.convert;

import com.school.student.dto.StudentDTO;
import com.school.student.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 学生对象转换器
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentConvert {

    StudentConvert INSTANCE = Mappers.getMapper(StudentConvert.class);

    Student convert(StudentDTO dto);

    void updateEntity(@MappingTarget Student entity, StudentDTO dto);
}
