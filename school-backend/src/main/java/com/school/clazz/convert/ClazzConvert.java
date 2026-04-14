package com.school.clazz.convert;

import com.school.clazz.dto.ClazzDTO;
import com.school.clazz.entity.Clazz;
import com.school.clazz.vo.ClazzVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClazzConvert {

    ClazzConvert INSTANCE = Mappers.getMapper(ClazzConvert.class);

    Clazz dtoToEntity(ClazzDTO dto);

    void updateEntityFromDto(@MappingTarget Clazz entity, ClazzDTO dto);

    ClazzVO entityToVo(Clazz entity);

    List<ClazzVO> entityToVoList(List<Clazz> list);
}