package com.school.course.convert;

import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import com.school.course.vo.CourseVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseConvert {

    CourseConvert INSTANCE = Mappers.getMapper(CourseConvert.class);

    Course dtoToEntity(CourseDTO dto);

    void updateEntityFromDto(@MappingTarget Course entity, CourseDTO dto);

    CourseVO entityToVo(Course entity);

    List<CourseVO> entityToVoList(List<Course> list);
}