package com.school.teacher.convert;

import com.school.teacher.dto.TeacherDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.vo.TeacherVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeacherConvert {

    TeacherConvert INSTANCE = Mappers.getMapper(TeacherConvert.class);

    Teacher dtoToEntity(TeacherDTO dto);

    void updateEntityFromDto(@MappingTarget Teacher entity, TeacherDTO dto);

    TeacherVO entityToVo(Teacher entity);

    List<TeacherVO> entityToVoList(List<Teacher> list);
}