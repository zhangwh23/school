package com.school.clazz.convert;

import com.school.clazz.dto.ClazzDTO;
import com.school.clazz.entity.Clazz;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 班级对象转换器
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClazzConvert {

    ClazzConvert INSTANCE = Mappers.getMapper(ClazzConvert.class);

    Clazz convert(ClazzDTO dto);

    void updateEntity(@MappingTarget Clazz entity, ClazzDTO dto);
}
