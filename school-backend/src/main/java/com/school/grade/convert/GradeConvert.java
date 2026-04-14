package com.school.grade.convert;

import com.school.grade.dto.GradeDTO;
import com.school.grade.entity.Grade;
import com.school.grade.vo.GradeVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeConvert {

    GradeConvert INSTANCE = Mappers.getMapper(GradeConvert.class);

    Grade dtoToEntity(GradeDTO dto);

    void updateEntityFromDto(@MappingTarget Grade entity, GradeDTO dto);

    GradeVO entityToVo(Grade entity);

    List<GradeVO> entityToVoList(List<Grade> list);
}