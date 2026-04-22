package com.school.student.convert;

import com.school.student.dto.StudentDTO;
import com.school.student.entity.Student;
import com.school.student.vo.StudentVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentConvert {

    StudentConvert INSTANCE = Mappers.getMapper(StudentConvert.class);

    Student dtoToEntity(StudentDTO dto);

    void updateEntityFromDto(@MappingTarget Student entity, StudentDTO dto);

    StudentVO entityToVo(Student entity);

    List<StudentVO> entityToVoList(List<Student> list);
}