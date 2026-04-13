package com.school.grade.convert;

import com.school.grade.dto.GradeDTO;
import com.school.grade.entity.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 成绩对象转换器
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GradeConvert {

    GradeConvert INSTANCE = Mappers.getMapper(GradeConvert.class);

    Grade convert(GradeDTO dto);

    void updateEntity(@MappingTarget Grade entity, GradeDTO dto);
}
